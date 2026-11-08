package com.benbenlaw.resourcefish.item;

import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import com.benbenlaw.resourcefish.entities.ResourceFishEntities;
import com.benbenlaw.resourcefish.util.ResourceType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ResourceFishCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ResourceFish.MOD_ID);

    public static final Supplier<CreativeModeTab> RESOURCE_FISH_TAB = CREATIVE_MODE_TABS.register("resource_fish", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ResourceFishItems.RESOURCE_FISH_BUCKET.get().asItem().getDefaultInstance())
            .title(Component.translatable("itemGroup.resourcefish"))
            .displayItems((parameters, output) -> {

                //Items and Blocks
                output.accept(ResourceFishBlocks.NEST.get().asItem().getDefaultInstance());
                output.accept(ResourceFishBlocks.AQUARIUM.get().asItem().getDefaultInstance());
                output.accept(ResourceFishBlocks.BREEDING_TANK.get().asItem().getDefaultInstance());
                output.accept(ResourceFishBlocks.INFUSING_TANK.get().asItem().getDefaultInstance());
                output.accept(ResourceFishBlocks.TANK_CONTROLLER.get().asItem().getDefaultInstance());
                output.accept(ResourceFishBlocks.CAVIAR_PROCESSOR.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.DEPTH_UPGRADE_1.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.DEPTH_UPGRADE_2.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.DEPTH_UPGRADE_3.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.WIDTH_UPGRADE_1.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.WIDTH_UPGRADE_2.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.WIDTH_UPGRADE_3.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.SPEED_UPGRADE_1.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.SPEED_UPGRADE_2.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.SPEED_UPGRADE_3.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.TANK_UPGRADE.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.INFUSING_UPGRADE.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.BREEDING_UPGRADE.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.ROUND_ROBIN_UPGRADE.get().asItem().getDefaultInstance());

                output.accept(ResourceFishItems.BASIC_FISH_FOOD.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.METALLIC_FISH_FOOD.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.CRYSTAL_FISH_FOOD.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.NETHER_FISH_FOOD.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.ENDER_FISH_FOOD.get().asItem().getDefaultInstance());
                output.accept(ResourceFishItems.BASIC_MOB_FISH_FOOD.get().asItem().getDefaultInstance());

                //Fish Bucket
                for (ResourceType type : ResourceType.getAll()) {
                    ItemStack stack = new ItemStack(ResourceFishItems.RESOURCE_FISH_BUCKET.get());

                    stack.set(ResourceFishDataComponents.FISH_TYPE, type.getId());

                    CompoundTag tag = new CompoundTag();
                    tag.putString("id", type.getId().toString());
                    tag.putString("BucketResourceType", type.getId().toString());
                    tag.putInt("BucketPattern", 0); // or pick defaults
                    tag.putInt("BucketModel", 0);
                    tag.putBoolean("allowedToDrop", true);

                    stack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(tag));

                    output.accept(stack);
                }

                //Spawn Eggs
                for (ResourceType type : ResourceType.getAll()) {
                    ItemStack stack = new ItemStack(ResourceFishItems.RESOURCE_FISH_SPAWN_EGG.get());
                    stack.set(ResourceFishDataComponents.FISH_TYPE, type.getId());

                    output.accept(stack);
                }

                //Caviar
                for (ResourceType type : ResourceType.getAll()) {
                    ItemStack stack = new ItemStack(ResourceFishItems.CAVIAR.get());
                    stack.set(ResourceFishDataComponents.FISH_TYPE, type.getId());

                    output.accept(stack);
                }


            }).build());
}
