package com.benbenlaw.resourcefish.block;

import com.benbenlaw.resourcefish.block.entity.InfusingTankBlockEntity;
import com.benbenlaw.resourcefish.util.ResourceType;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class InfusingTankBlock extends BreedingTankBlock {
    public static final MapCodec<InfusingTankBlock> CODEC = simpleCodec(InfusingTankBlock::new);

    public InfusingTankBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusingTankBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        ResourceType held = AquariumBlock.bucketedFishType(item);
        if (held == ResourceType.NONE || !(level.getBlockEntity(pos) instanceof InfusingTankBlockEntity tank)
                || tank.getFish() == ResourceType.NONE)
            return super.useItemOn(item, state, level, pos, player, hand, hit);
        if (level.isClientSide)
            return ItemInteractionResult.sidedSuccess(true);

        ResourceType old = tank.takeFish();
        tank.addFish(held);
        swap(player, hand, item, AquariumBlock.filledBucketFor(old, AquariumBlock.emptyBucketFor(item)), level, pos);
        return ItemInteractionResult.SUCCESS;
    }
}
