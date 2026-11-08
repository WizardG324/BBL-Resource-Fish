package com.benbenlaw.resourcefish.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.InputOutputItemHandler;
import com.benbenlaw.resourcefish.recipe.FishBreedingRecipe;
import com.benbenlaw.resourcefish.util.ResourceType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BreedingTankBlockEntity extends SyncableBlockEntity implements FishTank {
    public static final int FOOD_SLOT = 0;
    public static final int FIRST_OUTPUT_SLOT = 1;
    private static final int OUTPUT_SLOTS = 3;
    private static final int SLOTS = FIRST_OUTPUT_SLOT + OUTPUT_SLOTS;

    private final ResourceLocation[] parents = { ResourceType.NONE.getId(), ResourceType.NONE.getId() };
    private final List<ResourceLocation> offspring = new ArrayList<>();
    private int progress;
    // Re-picked after a reload, which is harmless since progress is kept.
    @Nullable
    private RecipeHolder<FishBreedingRecipe> current;
    // Set when food or parents change, so the recipe list is only scanned then.
    private boolean dirty = true;
    @Nullable
    private ResourceType[] rendered; // cache for the renderer, rebuilt when the fish change

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            dirty = true;
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != FOOD_SLOT || level != null && level.getRecipeManager()
                    .getAllRecipesFor(FishBreedingRecipe.Type.INSTANCE).stream()
                    .anyMatch(holder -> holder.value().breedingIngredient().ingredient().test(stack)); // item only, not the count
        }
    };

    // Pipes and hoppers may only add food, or take bones out
    private final InputOutputItemHandler automation = new InputOutputItemHandler(
            inventory, (slot, stack) -> slot == FOOD_SLOT, slot -> slot >= FIRST_OUTPUT_SLOT);

    public BreedingTankBlockEntity(BlockPos pos, BlockState state) {
        super(ResourceFishBlockEntities.BREEDING_TANK_BLOCK_ENTITY.get(), pos, state);
    }

    public IItemHandler getItemHandlerCapability(@Nullable Direction side) {
        return automation;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ResourceType getParent(int index) {
        return ResourceType.REGISTRY.getOrDefault(parents[index], ResourceType.NONE);
    }

    public int getOffspringCount() {
        return offspring.size();
    }

    // Parents first, then the bred fish.
    public ResourceType[] getRenderedFish() {
        if (rendered != null)
            return rendered;
        List<ResourceType> fish = new ArrayList<>(List.of(getParent(0), getParent(1)));
        offspring.forEach(id -> fish.add(ResourceType.REGISTRY.getOrDefault(id, ResourceType.NONE)));
        return rendered = fish.toArray(ResourceType[]::new);
    }

    @Override
    public int firstOutputSlot() {
        return FIRST_OUTPUT_SLOT;
    }

    @Override
    public boolean addFish(ResourceType type) {
        for (int i = 0; i < parents.length; i++) {
            if (getParent(i) == ResourceType.NONE) {
                setParent(i, type);
                return true;
            }
        }
        return false;
    }

    public ResourceType takeFish() {
        while (!offspring.isEmpty()) {
            ResourceType type = ResourceType.REGISTRY.getOrDefault(offspring.removeFirst(), ResourceType.NONE);
            rendered = null;
            setChanged();
            sync();
            if (type != ResourceType.NONE)
                return type;
        }
        for (int i = parents.length - 1; i >= 0; i--) {
            ResourceType type = getParent(i);
            if (type != ResourceType.NONE) {
                setParent(i, ResourceType.NONE);
                return type;
            }
        }
        return ResourceType.NONE;
    }

    private void setParent(int index, ResourceType type) {
        parents[index] = type.getId();
        progress = 0;
        current = null;
        dirty = true;
        rendered = null;
        setChanged();
        sync();
    }

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

        if (current == null || usedSlots() >= OUTPUT_SLOTS)
            return;

        FishBreedingRecipe recipe = current.value();
        if (++progress < recipe.duration()) {
            level.blockEntityChanged(worldPosition); // save progress without a neighbor update every tick
            return;
        }

        progress = 0;
        current = null; // re-pick next cycle, so every output sharing these parents gets its turn
        dirty = true;
        inventory.extractItem(FOOD_SLOT, recipe.breedingIngredient().count(), false);
        if (level.random.nextDouble() < recipe.chance()) {
            offspring.add(recipe.createdFish());
            rendered = null;
            sync();
        } else
            ItemHandlerHelper.insertItemStacked(outputs(), new ItemStack(Items.BONE), false);
        setChanged();
    }

    @Nullable
    private RecipeHolder<FishBreedingRecipe> pickRecipe() {
        if (getParent(0) == ResourceType.NONE || getParent(1) == ResourceType.NONE)
            return null;
        List<RecipeHolder<FishBreedingRecipe>> matches = level.getRecipeManager()
                .getAllRecipesFor(FishBreedingRecipe.Type.INSTANCE).stream()
                .filter(holder -> matches(holder.value()))
                .toList();
        return matches.isEmpty() ? null : matches.get(level.random.nextInt(matches.size()));
    }

    private boolean matches(FishBreedingRecipe recipe) {
        ResourceLocation a = getParent(0).getId(), b = getParent(1).getId();
        boolean parentsMatch = (recipe.parentIngredientA().equals(a) && recipe.parentIngredientB().equals(b))
                || (recipe.parentIngredientA().equals(b) && recipe.parentIngredientB().equals(a));
        ItemStack food = inventory.getStackInSlot(FOOD_SLOT);
        // a recipe breeding a fish type that doesn't exist would only fill a slot with nothing
        return parentsMatch && ResourceType.REGISTRY.containsKey(recipe.createdFish())
                && recipe.breedingIngredient().test(food)
                && food.getCount() >= recipe.breedingIngredient().count();
    }

    private int usedSlots() {
        int used = offspring.size();
        for (int slot = FIRST_OUTPUT_SLOT; slot < SLOTS; slot++)
            if (!inventory.getStackInSlot(slot).isEmpty())
                used++;
        return used;
    }

    private IItemHandler outputs() {
        return new RangedWrapper(inventory, FIRST_OUTPUT_SLOT, SLOTS);
    }

    public void onBroken() {
        if (level == null || level.isClientSide)
            return;
        SimpleContainer contents = new SimpleContainer(SLOTS);
        for (int i = 0; i < SLOTS; i++)
            contents.setItem(i, inventory.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, contents);

        AquariumBlockEntity.releaseFish(level, worldPosition, getRenderedFish());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("ParentA", parents[0].toString());
        tag.putString("ParentB", parents[1].toString());
        ListTag bred = new ListTag();
        offspring.forEach(id -> bred.add(StringTag.valueOf(id.toString())));
        tag.put("Offspring", bred);
        tag.putInt("Progress", progress);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        parents[0] = parseOrNone(tag.getString("ParentA"));
        parents[1] = parseOrNone(tag.getString("ParentB"));
        offspring.clear();
        rendered = null;
        dirty = true;
        for (Tag id : tag.getList("Offspring", Tag.TAG_STRING))
            offspring.add(parseOrNone(id.getAsString()));
        progress = tag.getInt("Progress");
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    private static ResourceLocation parseOrNone(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        return parsed == null ? ResourceType.NONE.getId() : parsed;
    }
}
