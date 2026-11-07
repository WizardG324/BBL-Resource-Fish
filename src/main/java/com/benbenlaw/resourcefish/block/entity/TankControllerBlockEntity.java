package com.benbenlaw.resourcefish.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.InputOutputItemHandler;
import com.benbenlaw.resourcefish.entities.ResourceFishEntities;
import com.benbenlaw.resourcefish.entities.ResourceFishEntity;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import com.benbenlaw.resourcefish.recipe.ActiveRecipeType;
import com.benbenlaw.resourcefish.recipe.FishBreedingRecipe;
import com.benbenlaw.resourcefish.recipe.FishInfusingRecipe;
import com.benbenlaw.resourcefish.recipe.TankRecipeInput;
import com.benbenlaw.resourcefish.util.ResourceFishTags;
import com.benbenlaw.resourcefish.util.ResourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TankControllerBlockEntity extends SyncableBlockEntity {
    public final ContainerData data;
    private ActiveRecipeType activeRecipe = ActiveRecipeType.NONE;
    public static final int RECIPE_SLOT_1 = 12;
    public static final int RECIPE_SLOT_2 = 13;
    public static final int RECIPE_SLOT_3 = 14;
    public static final int UPGRADE_SLOT_1 = 15;
    public static final int UPGRADE_SLOT_2 = 16;
    public static final int UPGRADE_SLOT_3 = 17;
    public static final int UPGRADE_SLOT_4 = 18;
    public int progress = 0;
    public int maxProgress = Integer.MAX_VALUE; //
    private boolean inventoryChanged = true;
    public List<ResourceFishEntity> fishPool = new ArrayList<>();
    private final Set<UUID> previouslyAllowedFish = new HashSet<>();
    public int fishCount = 0;
    public int maxFishCount = 0;
    private boolean showRange = false;
    public List<String> fishNames = new ArrayList<>();
    private Optional<RecipeHolder<FishBreedingRecipe>> currentBreedingMatch = Optional.empty();
    private Optional<RecipeHolder<FishInfusingRecipe>> currentInfusingMatch = Optional.empty();
    private TankRecipeInput cachedInventory = getRecipeInput();

    private @NotNull TankRecipeInput getRecipeInput() {
        return new TankRecipeInput(itemHandler, getBlockPos()) {
            @Override
            public @NotNull ItemStack getItem(int index) {
                return itemHandler.getStackInSlot(index);
            }

            @Override
            public int size() {
                return itemHandler.getSlots();
            }

            @Override
            public BlockPos getPos() {
                return getBlockPos();
            }
        };
    }

    public TankControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ResourceFishBlockEntities.TANK_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> fishCount;
                    case 3 -> maxFishCount;
                    default -> 0; // Default case if index is out of bounds
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                    case 2 -> fishCount = value;
                    case 3 -> maxFishCount = value;
                }
            }

            @Override
            public int getCount() {
                return 4; // Placeholder for actual count
            }
        };
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(19) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
            inventoryChanged = true;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot >= 15 && slot <= 18) {
                return 1;
            }
            return 64;
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            if (slot >= UPGRADE_SLOT_1 && slot <= UPGRADE_SLOT_4) {
                if (stack.is(ResourceFishTags.Items.UPGRADES)) {
                    return 1;
                } else {
                    return 0;
                }
            }
            if (slot >= RECIPE_SLOT_1 && slot <= RECIPE_SLOT_3) {
                if (stack.is(ResourceFishTags.Items.UPGRADES)) {
                    return 0;
                } else {
                    return stack.getMaxStackSize();
                }
            }

            return stack.getMaxStackSize();
        }
    };

    private final IItemHandler tankControllerItemHandlerSide = new InputOutputItemHandler(
            itemHandler,
            (slot, stack) -> slot >= 12 && slot <= 15,
            (slot) -> slot >= 0 && slot <= 11
    );

    public IItemHandler getTankControllerItemHandlerSide(Direction side) {
        return tankControllerItemHandlerSide;
    }

    public ItemStackHandler getItemStackHandler() {
        return itemHandler;
    }


    public int getMaxFish(AABB box) {

        BlockPos min = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
        BlockPos max = new BlockPos((int) box.maxX, (int) box.maxY, (int) box.maxZ);

        int blocksOfWaterPerFish = 3; //Required number of water blocks per fish
        int waterBlocks = 0;

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    assert level != null;
                    if (level.getBlockState(currentPos).getFluidState().isSource() && !level.getBlockState(currentPos).getFluidState().isEmpty()) {
                        waterBlocks++;
                    }
                }
            }
        }

        maxFishCount = waterBlocks / blocksOfWaterPerFish;
        return maxFishCount;

    }

    public void tick() {
        if (level == null) return;

        Direction facing = this.getBlockState().getValue(BlockStateProperties.FACING);
        Direction opposite = facing.getOpposite();
        BlockPos centerPos = this.worldPosition.relative(opposite, 3);
        AABB box = calculateBox(centerPos, opposite);

        // --- CLIENT SIDE ---
        if (level.isClientSide()) {
            if (showRange) spawnBoxOutlineParticles(box);
            return;
        }



        // --- RECIPE CHECKING ---
        // Only check recipes if the inventory changed since last time
        if (inventoryChanged) {
            cachedInventory = getRecipeInput();

            Optional<RecipeHolder<FishBreedingRecipe>> breedingMatch = randomMatch(FishBreedingRecipe.Type.INSTANCE);
            Optional<RecipeHolder<FishInfusingRecipe>> infusingMatch = randomMatch(FishInfusingRecipe.Type.INSTANCE);

            if (breedingMatch.isPresent()) {
                activeRecipe = ActiveRecipeType.BREEDING;
                this.currentBreedingMatch = breedingMatch;
                this.currentInfusingMatch = Optional.empty();
            } else if (infusingMatch.isPresent()) {
                activeRecipe = ActiveRecipeType.INFUSING;
                this.currentInfusingMatch = infusingMatch;
                this.currentBreedingMatch = Optional.empty();
            } else {
                activeRecipe = ActiveRecipeType.NONE;
                this.currentBreedingMatch = Optional.empty();
                this.currentInfusingMatch = Optional.empty();
            }

            inventoryChanged = false;
        }

        // --- RECIPE PROGRESS ---
        switch (activeRecipe) {
            case BREEDING -> {
                if (currentBreedingMatch != null && currentBreedingMatch.isPresent()) {
                    breedingRecipe(currentBreedingMatch, centerPos, itemHandler, level);
                }
            }
            case INFUSING -> {
                if (currentInfusingMatch != null && currentInfusingMatch.isPresent()) {
                    infusingRecipe(currentInfusingMatch, centerPos, itemHandler, level);
                }
            }
            case NONE -> {
                progress = 0; // no recipe running
                maxProgress = Integer.MAX_VALUE;
            }
        }

        // --- ENTITY COLLECTION & FISH MANAGEMENT ---
        // Run only once per second to reduce lag
        if (level.getGameTime() % 20 == 0) {
            List<Entity> entityList = level.getEntities(null, box);

            fishPool.clear();
            Set<UUID> currentAllowedFish = new HashSet<>();

            boolean tooManyFish = fishCount > maxFishCount;
            boolean breedingBlocked = hasBreedingUpgrade(); // ✅ FIX

            for (Entity entity : entityList) {
                if (entity instanceof ResourceFishEntity fish) {
                    fishPool.add(fish);

                    // ✅ FINAL authoritative drop control
                    boolean canDrop = !tooManyFish && !breedingBlocked;
                    fish.setAllowedToDrop(canDrop);

                    if (fish.getTankHome() != worldPosition) {
                        fish.setTankHome(worldPosition);
                    }

                    if (canDrop) {
                        currentAllowedFish.add(fish.getUUID());
                    }
                }
            }

            // --- UPDATE COUNTS ---
            fishCount = fishPool.size();
            maxFishCount = getMaxFish(box);

            Map<String, Integer> fishCountMap = new HashMap<>();
            for (ResourceFishEntity fish : fishPool) {
                String name = fish.getDisplayName().getString();
                fishCountMap.put(name, fishCountMap.getOrDefault(name, 0) + 1);
            }

            fishNames.clear();
            for (Map.Entry<String, Integer> entry : fishCountMap.entrySet()) {
                int count = entry.getValue();
                String display = (count > 1 ? count + "x " : "") + entry.getKey();
                fishNames.add(display);
            }

            setChanged();
            sync();

            previouslyAllowedFish.clear();
            previouslyAllowedFish.addAll(currentAllowedFish);
        }
    }

    private <T extends Recipe<RecipeInput>> Optional<RecipeHolder<T>> randomMatch(RecipeType<T> type) {
        List<RecipeHolder<T>> matches = level.getRecipeManager().getRecipesFor(type, cachedInventory, level);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(level.random.nextInt(matches.size())));
    }

    private boolean hasBreedingUpgrade() {
        return itemHandler.getStackInSlot(UPGRADE_SLOT_1).is(ResourceFishItems.BREEDING_UPGRADE)
                || itemHandler.getStackInSlot(UPGRADE_SLOT_2).is(ResourceFishItems.BREEDING_UPGRADE)
                || itemHandler.getStackInSlot(UPGRADE_SLOT_3).is(ResourceFishItems.BREEDING_UPGRADE)
                || itemHandler.getStackInSlot(UPGRADE_SLOT_4).is(ResourceFishItems.BREEDING_UPGRADE)
                || itemHandler.getStackInSlot(UPGRADE_SLOT_1).is(ResourceFishItems.INFUSING_UPGRADE)
                || itemHandler.getStackInSlot(UPGRADE_SLOT_2).is(ResourceFishItems.INFUSING_UPGRADE)
                || itemHandler.getStackInSlot(UPGRADE_SLOT_3).is(ResourceFishItems.INFUSING_UPGRADE)
                || itemHandler.getStackInSlot(UPGRADE_SLOT_4).is(ResourceFishItems.INFUSING_UPGRADE)
                ;
    }

    private AABB calculateBox(BlockPos centerPos, Direction direction) {

        int widthUpgradeAmount = 0;
        int depthUpgradeAmount = 0;


        for (int i = UPGRADE_SLOT_1; i <= UPGRADE_SLOT_4; i++) {
            ItemStack upgradeStack = itemHandler.getStackInSlot(i);
            if (upgradeStack.isEmpty()) continue;

            Item item = upgradeStack.getItem();

            if (item == ResourceFishItems.DEPTH_UPGRADE_1.get()) {
                depthUpgradeAmount+= 1;
            } else if (item == ResourceFishItems.DEPTH_UPGRADE_2.get()) {
                depthUpgradeAmount+= 2;
            } else if (item == ResourceFishItems.DEPTH_UPGRADE_3.get()) {
                depthUpgradeAmount+= 3;
            } else if (item == ResourceFishItems.WIDTH_UPGRADE_1.get()) {
                widthUpgradeAmount+= 1;
            }  else if (item == ResourceFishItems.WIDTH_UPGRADE_2.get()) {
                widthUpgradeAmount+= 2;
            } else if (item == ResourceFishItems.WIDTH_UPGRADE_3.get()) {
                widthUpgradeAmount+= 3;
            }
        }

        BlockPos newCenterPositionWidth = centerPos.relative(direction, widthUpgradeAmount);
        BlockPos newCenterPositionDepth = centerPos.relative(direction, depthUpgradeAmount);


        int minX = newCenterPositionWidth.getX() - 1 - widthUpgradeAmount;
        int minZ = newCenterPositionWidth.getZ() - 1 - widthUpgradeAmount;
        int maxX = newCenterPositionWidth.getX() + 2 + widthUpgradeAmount;
        int maxZ = newCenterPositionWidth.getZ() + 2 + widthUpgradeAmount;
        int minY = newCenterPositionDepth.getY() - 1 - depthUpgradeAmount;
        int maxY = newCenterPositionDepth.getY() + 2 + depthUpgradeAmount;

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void breedingRecipe(Optional<RecipeHolder<FishBreedingRecipe>> match, BlockPos centerPos, ItemStackHandler itemHandler, Level level) {
        // If there's no cached match, bail out and make sure the active state is cleared
        if (match.isEmpty()) {
            activeRecipe = ActiveRecipeType.NONE;
            currentBreedingMatch = Optional.empty();
            progress = 0;
            maxProgress = Integer.MAX_VALUE;
            return;
        }

        RecipeHolder<FishBreedingRecipe> recipeHolder = match.get();
        maxProgress = recipeHolder.value().duration();
        progress++;

        if (progress >= maxProgress) {
            // Try to find and consume an ingredient from the three recipe slots
            boolean consumed = false;
            for (int slot = RECIPE_SLOT_1; slot <= RECIPE_SLOT_3; slot++) {
                ItemStack stack = itemHandler.getStackInSlot(slot);
                if (recipeHolder.value().breedingIngredient().test(stack)
                        && stack.getCount() >= recipeHolder.value().breedingIngredient().count()) {

                    // Shrink the stack and mark inventory changed so we re-evaluate recipes next tick
                    stack.shrink(recipeHolder.value().breedingIngredient().count());
                    inventoryChanged = true; // important — we changed inventory without setStackInSlot
                    consumed = true;
                    break;
                }
            }

            // If none of the slots could be consumed, we must stop the recipe here
            if (!consumed) {
                // Clean reset and clear active recipe so it doesn't keep trying
                progress = 0;
                maxProgress = Integer.MAX_VALUE;
                activeRecipe = ActiveRecipeType.NONE;
                currentBreedingMatch = Optional.empty();
                return;
            }

            // Apply chance result
            boolean chanceSuccess = level.random.nextDouble() < recipeHolder.value().chance();
            if (chanceSuccess) {
                ResourceFishEntity fish = ResourceFishEntities.RESOURCE_FISH.get().create(level);
                fish.setResourceType(ResourceType.get(recipeHolder.value().createdFish()));
                fish.setPos(Vec3.atCenterOf(centerPos));
                level.addFreshEntity(fish);
            } else {
                ItemEntity bones = new ItemEntity(level,
                        centerPos.getX() + 0.5, centerPos.getY() + 1, centerPos.getZ() + 0.5,
                        new ItemStack(Items.BONE));
                level.addFreshEntity(bones);
            }

            // DONE: reset and clear active recipe & cached match so we won't immediately restart
            progress = 0;
            maxProgress = Integer.MAX_VALUE;
            activeRecipe = ActiveRecipeType.NONE;
            currentBreedingMatch = Optional.empty();
            // inventoryChanged already set when we shrank; sync() will be triggered by onContentsChanged if setStackInSlot used elsewhere
        }
    }


    private void infusingRecipe(Optional<RecipeHolder<FishInfusingRecipe>> match, BlockPos centerPos, ItemStackHandler itemHandler, Level level) {

        if (match.isEmpty()) {
            activeRecipe = ActiveRecipeType.NONE;
            currentInfusingMatch = Optional.empty();
            progress = 0;
            maxProgress = Integer.MAX_VALUE;
            return;
        }

        RecipeHolder<FishInfusingRecipe> recipeHolder = match.get();
        FishInfusingRecipe recipe = recipeHolder.value();

        maxProgress = recipe.duration();
        progress++;

        if (progress < maxProgress) return;

        List<SizedIngredient> ingredients = recipe.inputs();

        boolean[] usedSlots = new boolean[3];

        for (SizedIngredient ingredient : ingredients) {
            if (ingredient.ingredient() == Ingredient.EMPTY) continue;

            boolean found = false;
            for (int slot = 0; slot < 3; slot++) {
                if (usedSlots[slot]) continue;

                ItemStack stack = itemHandler.getStackInSlot(RECIPE_SLOT_1 + slot);
                if (ingredient.test(stack) && stack.getCount() >= ingredient.count()) {
                    usedSlots[slot] = true;
                    found = true;
                    break;
                }
            }

            if (!found) {
                // Required ingredient not found → reset recipe
                progress = 0;
                maxProgress = Integer.MAX_VALUE;
                activeRecipe = ActiveRecipeType.NONE;
                currentInfusingMatch = Optional.empty();
                return;
            }
        }

        for (int i = 0; i < ingredients.size(); i++) {
            SizedIngredient ingredient = ingredients.get(i);
            if (ingredient.ingredient() == Ingredient.EMPTY) continue;

            for (int slot = 0; slot < 3; slot++) {
                ItemStack stack = itemHandler.getStackInSlot(RECIPE_SLOT_1 + slot);
                if (ingredient.test(stack) && stack.getCount() >= ingredient.count()) {
                    stack.shrink(ingredient.count());
                    usedSlots[slot] = true;
                    break;
                }
            }
        }

        inventoryChanged = true;

        if (level.random.nextDouble() < recipe.chance()) {
            for (ResourceFishEntity fish : this.fishPool) {
                if (recipe.fish().equals(fish.getResourceType().getId())) {
                    fish.setResourceType(ResourceType.get(recipe.createdFish()));
                    break;
                }
            }
        } else {
            ItemEntity bones = new ItemEntity(
                    level,
                    centerPos.getX() + 0.5,
                    centerPos.getY() + 1,
                    centerPos.getZ() + 0.5,
                    new ItemStack(Items.BONE)
            );
            level.addFreshEntity(bones);
        }

        progress = 0;
        maxProgress = Integer.MAX_VALUE;
        activeRecipe = ActiveRecipeType.NONE;
        currentInfusingMatch = Optional.empty();
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.put("inventory", itemHandler.serializeNBT(provider));
        compoundTag.putInt("progress", progress);
        compoundTag.putInt("maxProgress", maxProgress);
        compoundTag.putInt("fishCount", fishCount);
        compoundTag.putInt("maxFishCount", maxFishCount);
        compoundTag.putBoolean("showRange", showRange);

        ListTag fishList = new ListTag();
        for (String fishName : fishNames) {
            fishList.add(StringTag.valueOf(fishName));
        }
        compoundTag.put("fishNames", fishList);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.itemHandler.deserializeNBT(provider, compoundTag.getCompound("inventory"));
        super.loadAdditional(compoundTag, provider);
        progress = compoundTag.getInt("progress");
        maxProgress = compoundTag.getInt("maxProgress");
        fishCount = compoundTag.getInt("fishCount");
        maxFishCount = compoundTag.getInt("maxFishCount");
        showRange = compoundTag.getBoolean("showRange");

        fishNames.clear();
        ListTag fishList = compoundTag.getList("fishNames", 8); // 8 is the ID for StringTag
        for (int i = 0; i < fishList.size(); i++) {
            fishNames.add(fishList.getString(i));
        }
    }

    public void onRightClick(ServerPlayer player) {

        if (showRange) {
            showRange = false;
        }
        else {
            showRange = true;
            player.sendSystemMessage(Component.translatable("block.resourcefish.tank_controller_range"));
        }

        sync();
    }

    private int simulateInsertStack(ItemStackHandler itemHandler, ItemStack stack, int[] slots) {
        ItemStack copy = stack.copy();

        // Check stacking into existing stacks
        for (int slot : slots) {
            ItemStack slotStack = itemHandler.getStackInSlot(slot);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, copy)) {
                int spaceLeft = slotStack.getMaxStackSize() - slotStack.getCount();
                int toAdd = Math.min(spaceLeft, copy.getCount());
                copy.shrink(toAdd);
                if (copy.isEmpty()) return 0; // fully fits
            }
        }

        // Check empty slots
        for (int slot : slots) {
            ItemStack slotStack = itemHandler.getStackInSlot(slot);
            if (slotStack.isEmpty()) {
                int toAdd = Math.min(copy.getMaxStackSize(), copy.getCount());
                copy.shrink(toAdd);
                if (copy.isEmpty()) return 0; // fully fits
            }
        }

        return copy.getCount(); // leftover if it wouldn't fit
    }


    private void spawnBoxOutlineParticles(AABB box) {
        if (level.isClientSide) {
            for (double x = box.minX; x <= box.maxX; x += 0.25) {
                for (double y = box.minY; y <= box.maxY; y += 0.25) {
                    for (double z = box.minZ; z <= box.maxZ; z += 0.25) {
                        // Spawn particles only at the edges of the box
                        boolean onXEdge = x == box.minX || x == box.maxX;
                        boolean onYEdge = y == box.minY || y == box.maxY;
                        boolean onZEdge = z == box.minZ || z == box.maxZ;

                        if ((onXEdge && onYEdge) || (onXEdge && onZEdge) || (onYEdge && onZEdge)) {
                            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0, 0);
                        }
                    }
                }
            }
        }
    }

    public void insertStackIntoSlots(ItemStackHandler itemHandler, ItemStack stack, int[] slots) {
        // Try to merge into existing stacks first
        for (int slot : slots) {
            ItemStack slotStack = itemHandler.getStackInSlot(slot);
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, stack)) {
                int maxStackSize = slotStack.getMaxStackSize();
                int spaceLeft = maxStackSize - slotStack.getCount();
                if (spaceLeft > 0) {
                    int toAdd = Math.min(spaceLeft, stack.getCount());
                    slotStack.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) return; // fully inserted
                }
            }
        }
        // Then try empty slots
        for (int slot : slots) {
            ItemStack slotStack = itemHandler.getStackInSlot(slot);
            if (slotStack.isEmpty()) {
                int toAdd = Math.min(stack.getMaxStackSize(), stack.getCount());
                itemHandler.setStackInSlot(slot, stack.copyWithCount(toAdd));
                stack.shrink(toAdd);
                if (stack.isEmpty()) return; // fully inserted
            }
        }
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

}