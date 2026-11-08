package com.benbenlaw.resourcefish.block.entity;


import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.InputOutputItemHandler;
import com.benbenlaw.resourcefish.entities.ResourceFishEntities;
import com.benbenlaw.resourcefish.entities.ResourceFishEntity;
import com.benbenlaw.resourcefish.util.ResourceType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class AquariumBlockEntity extends SyncableBlockEntity {
    public AquariumBlockEntity(BlockPos pos, BlockState state) {
        super(ResourceFishBlockEntities.AQUARIUM_BLOCK_ENTITY.get(), pos, state);
    }

    private ResourceLocation fishTypeId = ResourceType.NONE.getId();

    public ResourceType getFishType() {
        return ResourceType.REGISTRY.getOrDefault(fishTypeId, ResourceType.NONE);
    }

    private int progress;

    public void setFishType(ResourceType fishType) {
        this.fishTypeId = fishType.getId();
        this.progress = 0;
        setChanged();
        sync();
    }

    private final ItemStackHandler inventory = new ItemStackHandler() {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        };
    };

    private final InputOutputItemHandler buffer = new InputOutputItemHandler(
            inventory, (slot, stack) -> false, (slot) -> true);

    public IItemHandler getItemHandlerCapability(Direction side) {
        return this.buffer;
    }

    public void tick(RandomSource random) {
        ResourceType type = this.getFishType();
        if (type == ResourceType.NONE) return;

        this.progress++;
        if (this.progress < type.getDropIntervalTicks())
            return;

        this.progress = 0;
        for (ItemStack stack : type.rollResults(random)) 
            ItemHandlerHelper.insertItem(inventory, stack, false);  // discard on leftover
    }

    public void onBroken() {
        if (this.level == null || this.level.isClientSide)
            return;

        SimpleContainer contents = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++)
            contents.setItem(i, inventory.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, contents);

        releaseFish(level, worldPosition, getFishType());
    }

    public static void releaseFish(Level level, BlockPos pos, ResourceType... types) {
        boolean placedWater = false;
        for (ResourceType type : types) {
            if (type == ResourceType.NONE)
                continue;
            if (!placedWater) {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                placedWater = true;
            }
            ResourceFishEntity fish = new ResourceFishEntity(ResourceFishEntities.RESOURCE_FISH.get(), level);
            fish.setResourceType(type);
            fish.setFromBucket(true);
            fish.moveTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(fish);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("FishType", fishTypeId.toString());
        tag.putInt("Progress", progress);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fishTypeId = ResourceLocation.parse(tag.getString("FishType"));
        this.progress = tag.getInt("Progress");
        this.inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }
}
