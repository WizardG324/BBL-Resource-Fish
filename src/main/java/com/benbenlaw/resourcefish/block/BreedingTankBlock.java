package com.benbenlaw.resourcefish.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.benbenlaw.resourcefish.block.entity.BreedingTankBlockEntity;
import com.benbenlaw.resourcefish.block.entity.FishTank;
import com.benbenlaw.resourcefish.util.ResourceType;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BreedingTankBlock extends BaseEntityBlock {
    public static final MapCodec<BreedingTankBlock> CODEC = simpleCodec(BreedingTankBlock::new);

    public BreedingTankBlock(Properties props) {
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
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FishTank tank)
            tank.onBroken();
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        // Empty hand must fall through to useWithoutItem, or collecting never runs.
        if (item.isEmpty() || !(level.getBlockEntity(pos) instanceof FishTank tank))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        ResourceType held = AquariumBlock.bucketedFishType(item);
        boolean bucket = item.is(Items.BUCKET) || item.is(Items.WATER_BUCKET);
        // Not a fish, bucket, or recipe input.
        if (held == ResourceType.NONE && !bucket && !tank.getInventory().isItemValid(0, item))
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide)
            return ItemInteractionResult.sidedSuccess(true);

        if (held != ResourceType.NONE) {
            if (!tank.addFish(held))
                return ItemInteractionResult.FAIL;
            swap(player, hand, item, AquariumBlock.emptyBucketFor(item), level, pos);
            return ItemInteractionResult.SUCCESS;
        }

        if (bucket) { // Water bucket catches fish just like in vanilla
            ResourceType removed = tank.takeFish();
            if (removed == ResourceType.NONE)
                return ItemInteractionResult.FAIL;
            swap(player, hand, item, AquariumBlock.filledBucketFor(removed), level, pos);
            return ItemInteractionResult.SUCCESS;
        }

        ItemStack rest = ItemHandlerHelper.insertItem(tank.getItemHandlerCapability(null), item.copy(), false);
        if (rest.getCount() == item.getCount())
            return ItemInteractionResult.FAIL;
        if (!player.getAbilities().instabuild)
            player.setItemInHand(hand, rest);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FishTank tank))
            return InteractionResult.PASS;
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        ItemStackHandler inventory = tank.getInventory();
        // Sneaking takes back the inputs, otherwise the bones are taken.
        boolean inputs = player.isShiftKeyDown();
        int first = inputs ? 0 : tank.firstOutputSlot();
        int end = inputs ? tank.firstOutputSlot() : inventory.getSlots();
        for (int slot = first; slot < end; slot++) {
            ItemStack stack = inventory.extractItem(slot, Integer.MAX_VALUE, false);
            if (!stack.isEmpty())
                player.getInventory().placeItemBackInInventory(stack);
        }
        return InteractionResult.SUCCESS;
    }

    static void swap(Player player, InteractionHand hand, ItemStack used, ItemStack result, Level level,
            BlockPos pos) {
        player.setItemInHand(hand, ItemUtils.createFilledResult(used, player, result));
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BreedingTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
            @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, be) -> {
            if (be instanceof FishTank tank)
                tank.tick();
        };
    }
}
