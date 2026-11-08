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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.ModList;

public class AquariumBlock extends BaseEntityBlock {
    public static final MapCodec<AquariumBlock> CODEC = simpleCodec(AquariumBlock::new);
    private static final boolean BUCKETLIB = ModList.get().isLoaded("bucketlib");

    public AquariumBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AquariumBlockEntity aquarium) {
            aquarium.onBroken();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        AquariumBlockEntity entity = level.getBlockEntity(pos, ResourceFishBlockEntities.AQUARIUM_BLOCK_ENTITY.get())
                .orElse(null);
        if (entity == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ResourceType current = entity.getFishType();
        ResourceType held = bucketedFishType(item);

        if (current == ResourceType.NONE && isWaterBucket(item))
            return ItemInteractionResult.FAIL;

        if (held == ResourceType.NONE
                && (current == ResourceType.NONE || !catchesFish(item)))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide)
            return ItemInteractionResult.sidedSuccess(true);

        // the old fish goes back in the same kind of bucket that was used (vanilla or ceramic)
        ItemStack result = current == ResourceType.NONE ? emptyBucketFor(item)
                : filledBucketFor(current, held == ResourceType.NONE ? item : emptyBucketFor(item));
        entity.setFishType(held);
        player.setItemInHand(hand, ItemUtils.createFilledResult(item, player, result));
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        return ItemInteractionResult.sidedSuccess(false);
    }

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
        return createTickerHelper(blockEntityType, ResourceFishBlockEntities.AQUARIUM_BLOCK_ENTITY.get(),
                (world, blockPos, thisBlockState, blockEntity) -> blockEntity.tick(level.getRandom()));
    }
}
