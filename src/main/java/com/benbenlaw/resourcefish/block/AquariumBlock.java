package com.benbenlaw.resourcefish.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.benbenlaw.resourcefish.block.entity.AquariumBlockEntity;
import com.benbenlaw.resourcefish.block.entity.ResourceFishBlockEntities;
import com.benbenlaw.resourcefish.integration.BucketLibCompat;
import com.benbenlaw.resourcefish.item.ResourceFishDataComponents;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import com.benbenlaw.resourcefish.util.ResourceType;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class AquariumBlock extends BaseEntityBlock {
    public static final MapCodec<AquariumBlock> CODEC = simpleCodec(AquariumBlock::new);
    private static final boolean BUCKETLIB = ModList.get().isLoaded("bucketlib");

    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 27);
    private static final int MASTER_PART = part(1, 1, 1);

    public AquariumBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(PART, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ---- multiblock geometry ----

    private static int part(int x, int y, int z) {
        return 1 + x + 3 * y + 9 * z;
    }

    public static boolean isFormed(BlockState state) {
        return state.hasProperty(PART) && state.getValue(PART) != 0;
    }

    public static boolean isMaster(BlockState state) {
        return state.hasProperty(PART) && state.getValue(PART) == MASTER_PART;
    }

    private static BlockPos origin(BlockPos pos, BlockState state) {
        int i = state.getValue(PART) - 1;
        return pos.offset(-(i % 3), -(i / 3 % 3), -(i / 9));
    }

    public static BlockPos masterPos(BlockPos pos, BlockState state) {
        return origin(pos, state).offset(1, 1, 1);
    }

    private boolean isLoneAquarium(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(this) && !isFormed(state);
    }

    // ---- forming ----

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(this))
            tryForm(level, pos);
    }

    /** Forms the first complete 3x3x3 of lone aquariums that includes {@code pos}. */
    private void tryForm(Level level, BlockPos pos) {
        for (int ox = 0; ox < 3; ox++)
            for (int oy = 0; oy < 3; oy++)
                for (int oz = 0; oz < 3; oz++)
                    if (complete(level, pos.offset(-ox, -oy, -oz))) {
                        form(level, pos.offset(-ox, -oy, -oz));
                        return;
                    }
    }

    // also refuses while the aquariums hold more fish than a base tank
    private boolean complete(Level level, BlockPos origin) {
        int fish = 0;
        for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(2, 2, 2))) {
            if (!isLoneAquarium(level, p))
                return false;
            if (level.getBlockEntity(p) instanceof AquariumBlockEntity aquarium)
                fish += aquarium.fishArray().length;
        }
        return fish <= AquariumBlockEntity.FISH_PER_ROW;
    }

    private void form(Level level, BlockPos origin) {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                for (int z = 0; z < 3; z++) {
                    BlockPos p = origin.offset(x, y, z);
                    level.setBlock(p, level.getBlockState(p).setValue(PART, part(x, y, z)), Block.UPDATE_ALL);
                    level.invalidateCapabilities(p);
                }

        // the aquariums' fish (at most 9, see complete) and items move into the center block, which already kept its own
        if (!(level.getBlockEntity(origin.offset(1, 1, 1)) instanceof AquariumBlockEntity master))
            return;
        for (BlockPos p : BlockPos.betweenClosed(origin, origin.offset(2, 2, 2)))
            if (!p.equals(master.getBlockPos()) && level.getBlockEntity(p) instanceof AquariumBlockEntity part)
                part.moveInto(master);
    }

    // ---- breaking ----

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AquariumBlockEntity aquarium) {
            if (isFormed(state))
                unform(level, pos, state);
            else
                aquarium.onBroken();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void unform(Level level, BlockPos broken, BlockState state) {
        if (level.isClientSide)
            return;
        BlockPos origin = origin(broken, state);
        List<ResourceType> fish = level.getBlockEntity(masterPos(broken, state)) instanceof AquariumBlockEntity master
                ? new ArrayList<>(master.dissolve(broken)) : new ArrayList<>();

        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                for (int z = 0; z < 3; z++) {
                    BlockPos p = origin.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (p.equals(broken) || !s.is(this) || s.getValue(PART) != part(x, y, z))
                        continue;
                    level.setBlock(p, s.setValue(PART, 0), Block.UPDATE_ALL);
                    level.invalidateCapabilities(p);
                    if (!fish.isEmpty() && level.getBlockEntity(p) instanceof AquariumBlockEntity aquarium)
                        aquarium.setFishType(fish.removeFirst());
                }
        AquariumBlockEntity.releaseFish(level, broken, fish.toArray(ResourceType[]::new));
    }

    // ---- clicks ----

    @Override
    protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        AquariumBlockEntity entity = level.getBlockEntity(pos, ResourceFishBlockEntities.AQUARIUM_BLOCK_ENTITY.get())
                .orElse(null);
        if (entity == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (isFormed(state))
            return useOnTank(item, entity.master(), level, pos, player, hand);

        ResourceType current = entity.getFishType();
        ResourceType held = bucketedFishType(item);

        // a water bucket only acts when there's a fish to catch
        if (current == ResourceType.NONE && isWaterBucket(item))
            return ItemInteractionResult.FAIL;

        // an empty or water bucket takes the fish out, like the tanks
        if (held == ResourceType.NONE
                && (current == ResourceType.NONE || !catchesFish(item)))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide)
            return ItemInteractionResult.sidedSuccess(true);

        // the old fish goes back in the same kind of bucket that was used (vanilla or ceramic)
        ItemStack result = current == ResourceType.NONE ? emptyBucketFor(item)
                : filledBucketFor(current, held == ResourceType.NONE ? item : emptyBucketFor(item));
        entity.setFishType(held);
        BreedingTankBlock.swap(player, hand, item, result, level, pos);
        return ItemInteractionResult.sidedSuccess(false);
    }

    private ItemInteractionResult useOnTank(ItemStack item, @Nullable AquariumBlockEntity tank, Level level,
            BlockPos pos, Player player, InteractionHand hand) {
        ResourceType held = bucketedFishType(item);
        boolean upgrade = AquariumBlockEntity.upgradeTier(item) > 0;
        if (item.isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; // sneak + empty hand takes the upgrade out
        if (held == ResourceType.NONE && !upgrade && !catchesFish(item))
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION; // let blocks be placed against it
        if (tank == null)
            return ItemInteractionResult.FAIL;
        if (level.isClientSide)
            return ItemInteractionResult.sidedSuccess(true);

        if (upgrade) {
            if (AquariumBlockEntity.upgradeTier(item) == AquariumBlockEntity.upgradeTier(tank.getUpgrade()))
                return ItemInteractionResult.FAIL;
            ItemStack old = tank.swapUpgrade(item);
            if (old == null) // too many fish for a smaller tank
                return ItemInteractionResult.FAIL;
            item.consume(1, player);
            if (!old.isEmpty())
                player.getInventory().placeItemBackInInventory(old);
            return ItemInteractionResult.SUCCESS;
        }

        if (held != ResourceType.NONE) {
            if (!tank.addFish(held))
                return ItemInteractionResult.FAIL;
            BreedingTankBlock.swap(player, hand, item, emptyBucketFor(item), level, pos);
            return ItemInteractionResult.SUCCESS;
        }

        ResourceType removed = tank.takeFish();
        if (removed == ResourceType.NONE)
            return ItemInteractionResult.FAIL;
        BreedingTankBlock.swap(player, hand, item, filledBucketFor(removed, item), level, pos);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!isFormed(state) || !player.isShiftKeyDown()
                || !(level.getBlockEntity(pos) instanceof AquariumBlockEntity entity))
            return InteractionResult.PASS;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        AquariumBlockEntity tank = entity.master();
        if (tank == null || tank.getUpgrade().isEmpty())
            return InteractionResult.PASS;
        ItemStack old = tank.swapUpgrade(ItemStack.EMPTY);
        if (old == null)
            return InteractionResult.FAIL; // the fish wouldn't fit without it
        player.getInventory().placeItemBackInInventory(old);
        return InteractionResult.SUCCESS;
    }

    // ---- buckets ----

    static ItemStack emptyBucketFor(ItemStack usedBucket) {
        ItemStack empty = usedBucket.getCraftingRemainingItem();
        if (empty.isEmpty())
            return new ItemStack(Items.BUCKET);

        empty.remove(ResourceFishDataComponents.FISH_TYPE.get());
        return empty;
    }

    static ResourceType bucketedFishType(ItemStack stack) {
        @Nullable
        ResourceLocation id = stack.get(ResourceFishDataComponents.FISH_TYPE.get());
        return id == null ? ResourceType.NONE : ResourceType.REGISTRY.getOrDefault(id, ResourceType.NONE);
    }

    static boolean isWaterBucket(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || BUCKETLIB && BucketLibCompat.canCatch(stack, true);
    }

    static boolean catchesFish(ItemStack stack) {
        return stack.is(Items.BUCKET) || isWaterBucket(stack) || BUCKETLIB && BucketLibCompat.canCatch(stack, false);
    }

    public static ItemStack filledBucketFor(ResourceType type, ItemStack used) {
        ItemStack bucket = BUCKETLIB && BucketLibCompat.isBucket(used) // even if the config prevents catching, give the player's bucket back
                ? BucketLibCompat.withFish(used)
                : new ItemStack(ResourceFishItems.RESOURCE_FISH_BUCKET.get());
        bucket.set(ResourceFishDataComponents.FISH_TYPE.get(), type.getId());
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, bucket,
                tag -> tag.putString("BucketResourceType", type.getId().toString()));
        return bucket;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AquariumBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState blockState,
            @NotNull BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || (isFormed(blockState) && !isMaster(blockState)))
            return null;
        return createTickerHelper(blockEntityType, ResourceFishBlockEntities.AQUARIUM_BLOCK_ENTITY.get(),
                (world, blockPos, thisBlockState, blockEntity) -> blockEntity.tick(level.getRandom()));
    }
}
