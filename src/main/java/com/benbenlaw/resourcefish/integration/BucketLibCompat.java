package com.benbenlaw.resourcefish.integration;

import com.benbenlaw.resourcefish.entities.ResourceFishEntities;

import de.cech12.bucketlib.api.item.UniversalBucketItem;
import de.cech12.bucketlib.util.BucketLibUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

public final class BucketLibCompat {
    private BucketLibCompat() {}

    public static boolean canCatch(ItemStack stack, boolean water) {
        return stack.getItem() instanceof UniversalBucketItem bucket
                && bucket.canHoldEntity(ResourceFishEntities.RESOURCE_FISH.get())
                && !BucketLibUtil.containsEntityType(stack)
                && (water ? BucketLibUtil.getFluid(stack) == Fluids.WATER : BucketLibUtil.isEmpty(stack));
    }

    public static boolean isBucket(ItemStack stack) {
        return stack.getItem() instanceof UniversalBucketItem;
    }

    public static ItemStack withFish(ItemStack used) {
        ItemStack bucket = used.copyWithCount(1);
        if (BucketLibUtil.isEmpty(bucket))
            bucket = BucketLibUtil.addFluid(bucket, Fluids.WATER);
        return BucketLibUtil.addEntityType(bucket, ResourceFishEntities.RESOURCE_FISH.get());
    }
}
