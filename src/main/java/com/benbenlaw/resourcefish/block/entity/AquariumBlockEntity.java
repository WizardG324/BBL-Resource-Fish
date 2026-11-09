package com.benbenlaw.resourcefish.block.entity;


import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.InputOutputItemHandler;
import com.benbenlaw.resourcefish.block.AquariumBlock;
import com.benbenlaw.resourcefish.entities.ResourceFishEntities;
import com.benbenlaw.resourcefish.entities.ResourceFishEntity;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import com.benbenlaw.resourcefish.util.ResourceType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
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
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AquariumBlockEntity extends SyncableBlockEntity {
    public static final int SLOTS = 9;
    public static final int FISH_PER_ROW = 9;

    private final List<ResourceLocation> fish = new ArrayList<>();
    private final List<Integer> progress = new ArrayList<>(); // one counter per fish
    private ItemStack upgrade = ItemStack.EMPTY;
    @Nullable
    private ResourceType[] resolved; // cache of fishArray(), cleared whenever the fish list changes

    public AquariumBlockEntity(BlockPos pos, BlockState state) {
        super(ResourceFishBlockEntities.AQUARIUM_BLOCK_ENTITY.get(), pos, state);
    }

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }
    };

    private final InputOutputItemHandler buffer = new InputOutputItemHandler(
            inventory, (slot, stack) -> false, (slot) -> true);

    public IItemHandler getItemHandlerCapability(@Nullable Direction side) {
        // the outer blocks of a formed tank all lead to the center one
        if (!isMaster() && AquariumBlock.isFormed(getBlockState()) && master() instanceof AquariumBlockEntity master)
            return master.buffer;
        return buffer;
    }

    public boolean isMaster() {
        return AquariumBlock.isMaster(getBlockState());
    }

    @Nullable
    public AquariumBlockEntity master() {
        if (level == null || !AquariumBlock.isFormed(getBlockState()))
            return this;
        return level.getBlockEntity(AquariumBlock.masterPos(worldPosition, getBlockState())) instanceof AquariumBlockEntity m
                ? m : null;
    }

    // ---- fish ----

    public List<ResourceType> getFish() {
        return List.of(fishArray());
    }

    /** Resolved fish types, rebuilt only when the fish change (read every frame by the renderer and every tick). */
    public ResourceType[] fishArray() {
        if (resolved == null)
            resolved = fish.stream().map(id -> ResourceType.REGISTRY.getOrDefault(id, ResourceType.NONE))
                    .toArray(ResourceType[]::new);
        return resolved;
    }

    public ResourceType getFishType() {
        return fish.isEmpty() ? ResourceType.NONE : ResourceType.REGISTRY.getOrDefault(fish.getFirst(), ResourceType.NONE);
    }

    public void setFishType(ResourceType type) {
        fish.clear();
        progress.clear();
        if (type != ResourceType.NONE)
            addFishUnchecked(type);
        changed();
    }

    public int capacity() {
        return isMaster() ? FISH_PER_ROW * (1 + upgradeTier(upgrade)) : 1;
    }

    public boolean addFish(ResourceType type) {
        if (type == ResourceType.NONE || fish.size() >= capacity())
            return false;
        addFishUnchecked(type);
        changed();
        return true;
    }

    private void addFishUnchecked(ResourceType type) {
        fish.add(type.getId());
        progress.add(0);
    }

    // takes the most recently added fish
    public ResourceType takeFish() {
        while (!fish.isEmpty()) {
            ResourceType type = ResourceType.REGISTRY.getOrDefault(fish.removeLast(), ResourceType.NONE);
            progress.removeLast();
            changed();
            if (type != ResourceType.NONE)
                return type;
        }
        return ResourceType.NONE;
    }

    // ---- depth upgrade (master only) ----

    public ItemStack getUpgrade() {
        return upgrade;
    }

    public static int upgradeTier(ItemStack stack) {
        if (stack.is(ResourceFishItems.DEPTH_UPGRADE_1.get())) return 1;
        if (stack.is(ResourceFishItems.DEPTH_UPGRADE_2.get())) return 2;
        if (stack.is(ResourceFishItems.DEPTH_UPGRADE_3.get())) return 3;
        return 0;
    }

    @Nullable
    public ItemStack swapUpgrade(ItemStack stack) {
        if (fish.size() > FISH_PER_ROW * (1 + upgradeTier(stack)))
            return null;
        ItemStack old = upgrade;
        upgrade = stack.copyWithCount(1);
        if (stack.isEmpty())
            upgrade = ItemStack.EMPTY;
        changed();
        return old;
    }

    // ---- items ----

    public ItemStackHandler getInventory() {
        return inventory;
    }

    // a single aquarium only fills its first slot, as it always did
    private IItemHandler outputs() {
        return isMaster() ? inventory : new RangedWrapper(inventory, 0, 1);
    }

    public void tick(RandomSource random) {
        ResourceType[] types = fishArray();
        for (int i = 0; i < types.length; i++) {
            ResourceType type = types[i];
            if (type == ResourceType.NONE)
                continue;
            int p = progress.get(i) + 1;
            if (p < type.getDropIntervalTicks()) {
                progress.set(i, p);
                continue;
            }
            progress.set(i, 0);
            for (ItemStack stack : type.rollResults(random))
                ItemHandlerHelper.insertItem(outputs(), stack, false); // discard on leftover
        }
        if (!fish.isEmpty() && level != null)
            level.blockEntityChanged(worldPosition); // save progress without a neighbor update every tick
    }

    // ---- forming / breaking ----

    /** Empties this aquarium into {@code into} when a tank forms, the tank only forms when every fish fits. */
    public void moveInto(AquariumBlockEntity into) {
        for (ResourceType type : getFish())
            into.addFish(type);
        fish.clear();
        progress.clear();
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack rest = ItemHandlerHelper.insertItem(into.inventory, inventory.getStackInSlot(slot), false);
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
            if (!rest.isEmpty() && level != null)
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), rest);
        }
        changed();
    }

    /** Clears the master when its tank breaks, returning its fish. Items and the upgrade drop at {@code at}. */
    public List<ResourceType> dissolve(BlockPos at) {
        List<ResourceType> all = getFish();
        fish.clear();
        progress.clear();
        if (level != null) {
            dropInventory(at);
            Containers.dropItemStack(level, at.getX(), at.getY(), at.getZ(), upgrade);
        }
        upgrade = ItemStack.EMPTY;
        changed();
        return all;
    }

    public void onBroken() {
        if (this.level == null || this.level.isClientSide)
            return;
        dropInventory(worldPosition);
        releaseFish(level, worldPosition, getFish().toArray(ResourceType[]::new));
    }

    private void dropInventory(BlockPos at) {
        SimpleContainer contents = new SimpleContainer(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            contents.setItem(i, inventory.getStackInSlot(i));
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        Containers.dropContents(level, at, contents);
    }

    public static void releaseFish(Level level, BlockPos pos, ResourceType... types) {
        boolean placedWater = false;
        for (ResourceType type : types) {
            if (type == ResourceType.NONE)
                continue;
            if (!placedWater && level.getServer() != null) {
                MinecraftServer server = level.getServer();
                server.tell(new TickTask(server.getTickCount(), () -> {
                    if (level.getBlockState(pos).isAir())
                        level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                }));
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

    private void changed() {
        resolved = null;
        setChanged();
        sync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        fish.forEach(id -> list.add(StringTag.valueOf(id.toString())));
        tag.put("Fish", list);
        tag.putIntArray("FishProgress", progress);
        if (!upgrade.isEmpty())
            tag.put("Upgrade", upgrade.save(registries));
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        resolved = null; // also how the client hears about new fish
        fish.clear();
        progress.clear();
        if (tag.contains("Fish", Tag.TAG_LIST)) {
            int[] saved = tag.getIntArray("FishProgress");
            ListTag list = tag.getList("Fish", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
                if (id != null) {
                    fish.add(id);
                    progress.add(i < saved.length ? saved[i] : 0);
                }
            }
        } else { // aquariums saved before the multiblock: one fish
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("FishType"));
            if (id != null && !id.equals(ResourceType.NONE.getId())) {
                fish.add(id);
                progress.add(tag.getInt("Progress"));
            }
        }
        upgrade = tag.contains("Upgrade") ? ItemStack.parseOptional(registries, tag.getCompound("Upgrade")) : ItemStack.EMPTY;

        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (inventory.getSlots() < SLOTS) { // old saves had a single slot, deserialize shrinks the handler
            List<ItemStack> old = new ArrayList<>();
            for (int i = 0; i < inventory.getSlots(); i++)
                old.add(inventory.getStackInSlot(i));
            inventory.setSize(SLOTS);
            for (int i = 0; i < old.size(); i++)
                inventory.setStackInSlot(i, old.get(i));
        }
    }
}
