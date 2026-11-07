package com.benbenlaw.resourcefish.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.InputOutputItemHandler;
import com.benbenlaw.resourcefish.recipe.FishInfusingRecipe;
import com.benbenlaw.resourcefish.util.ResourceType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InfusingTankBlockEntity extends SyncableBlockEntity implements FishTank {
    private static final int INPUT_SLOTS = 3; // infusing recipes take at most 3 inputs
    public static final int BONE_SLOT = INPUT_SLOTS;
    private static final int SLOTS = INPUT_SLOTS + 1;

    private ResourceLocation fish = ResourceType.NONE.getId();
    private int progress;

    @Nullable
    private RecipeHolder<FishInfusingRecipe> current;
    // set when inputs or the fish change, so the recipe list is only scanned then
    private boolean dirty = true;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            dirty = true;
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // only something an infusing recipe uses, anything else would just sit there
            return slot == BONE_SLOT || level != null && level.getRecipeManager()
                    .getAllRecipesFor(FishInfusingRecipe.Type.INSTANCE).stream()
                    .anyMatch(holder -> holder.value().inputs().stream()
                            .anyMatch(input -> input.ingredient().test(stack))); // item only, not the count
        }
    };

    private final InputOutputItemHandler automation = new InputOutputItemHandler(
            inventory, (slot, stack) -> slot < INPUT_SLOTS, slot -> slot == BONE_SLOT);

    public InfusingTankBlockEntity(BlockPos pos, BlockState state) {
        super(ResourceFishBlockEntities.INFUSING_TANK_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public IItemHandler getItemHandlerCapability(@Nullable Direction side) {
        return automation;
    }

    @Override
    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public int firstOutputSlot() {
        return BONE_SLOT;
    }

    public ResourceType getFish() {
        return ResourceType.REGISTRY.getOrDefault(fish, ResourceType.NONE);
    }

    @Override
    public boolean addFish(ResourceType type) {
        if (getFish() != ResourceType.NONE)
            return false;
        setFish(type);
        return true;
    }

    @Override
    public ResourceType takeFish() {
        ResourceType type = getFish();
        if (type != ResourceType.NONE)
            setFish(ResourceType.NONE);
        return type;
    }

    private void setFish(ResourceType type) {
        fish = type.getId();
        progress = 0;
        current = null;
        dirty = true;
        setChanged();
        sync();
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide)
            return;
        if (dirty) {
            dirty = false;
            if (current == null || !matches(current.value())) {
                current = pickRecipe();
                progress = 0;
            }
        }

        ItemStack bones = inventory.getStackInSlot(BONE_SLOT);
        if (current == null || bones.getCount() >= bones.getMaxStackSize())
            return;

        FishInfusingRecipe recipe = current.value();
        if (++progress < recipe.duration()) {
            level.blockEntityChanged(worldPosition); // save progress without a neighbor update every tick
            return;
        }

        consumeInputs(recipe);
        progress = 0;
        current = null;
        dirty = true;
        if (level.random.nextDouble() < recipe.chance())
            setFish(ResourceType.get(recipe.createdFish()));
        else
            inventory.insertItem(BONE_SLOT, new ItemStack(Items.BONE), false);
        setChanged();
    }

    @Nullable
    private RecipeHolder<FishInfusingRecipe> pickRecipe() {
        if (getFish() == ResourceType.NONE)
            return null;
        List<RecipeHolder<FishInfusingRecipe>> matches = level.getRecipeManager()
                .getAllRecipesFor(FishInfusingRecipe.Type.INSTANCE).stream()
                .filter(holder -> matches(holder.value()))
                .toList();
        return matches.isEmpty() ? null : matches.get(level.random.nextInt(matches.size()));
    }

    // FishInfusingRecipe.matches only works on the tank controller, so check fish + inputs here.
    private boolean matches(FishInfusingRecipe recipe) {
        // a result type that doesn't exist would turn the fish into nothing
        return recipe.fish().equals(fish) && ResourceType.REGISTRY.containsKey(recipe.createdFish())
                && matchSlots(recipe) != null;
    }

    // Each ingredient on its own input slot, just as the tank controller does, null when they don't all fit.
    @Nullable
    private int[] matchSlots(FishInfusingRecipe recipe) {
        int[] slots = new int[recipe.inputs().size()];
        return assign(recipe.inputs(), 0, slots, new boolean[INPUT_SLOTS]) ? slots : null;
    }

    // Backtracks, so a broad ingredient (a tag) can't take the only slot a narrower one fits.
    private boolean assign(List<SizedIngredient> inputs, int i, int[] slots, boolean[] used) {
        if (i == inputs.size())
            return true;
        SizedIngredient ingredient = inputs.get(i);
        slots[i] = -1;
        if (ingredient.ingredient().isEmpty())
            return assign(inputs, i + 1, slots, used);
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            if (used[slot] || !ingredient.test(inventory.getStackInSlot(slot))) // test checks the count too
                continue;
            used[slot] = true;
            slots[i] = slot;
            if (assign(inputs, i + 1, slots, used))
                return true;
            used[slot] = false;
        }
        slots[i] = -1;
        return false;
    }

    private void consumeInputs(FishInfusingRecipe recipe) {
        int[] slots = matchSlots(recipe);
        if (slots == null)
            return;
        for (int i = 0; i < slots.length; i++)
            if (slots[i] >= 0)
                inventory.extractItem(slots[i], recipe.inputs().get(i).count(), false);
    }

    @Override
    public void onBroken() {
        if (level == null || level.isClientSide)
            return;
        SimpleContainer contents = new SimpleContainer(SLOTS);
        for (int i = 0; i < SLOTS; i++)
            contents.setItem(i, inventory.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, contents);
        AquariumBlockEntity.releaseFish(level, worldPosition, getFish());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Fish", fish.toString());
        tag.putInt("Progress", progress);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("Fish"));
        fish = parsed == null ? ResourceType.NONE.getId() : parsed;
        progress = tag.getInt("Progress");
        dirty = true;
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }
}
