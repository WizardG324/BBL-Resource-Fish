package com.benbenlaw.resourcefish.data;

import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ResourceFishItemModelsProvider extends ItemModelProvider {
    public ResourceFishItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ResourceFish.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        //Misc Items
        //simpleItem(ResourceFishItems.RESOURCE_FISH_BUCKET);
        simpleItem(ResourceFishItems.DEPTH_UPGRADE_1);
        simpleItem(ResourceFishItems.DEPTH_UPGRADE_2);
        simpleItem(ResourceFishItems.DEPTH_UPGRADE_3);
        simpleItem(ResourceFishItems.WIDTH_UPGRADE_1);
        simpleItem(ResourceFishItems.WIDTH_UPGRADE_2);
        simpleItem(ResourceFishItems.WIDTH_UPGRADE_3);
        simpleItem(ResourceFishItems.SPEED_UPGRADE_1);
        simpleItem(ResourceFishItems.SPEED_UPGRADE_2);
        simpleItem(ResourceFishItems.SPEED_UPGRADE_3);
        simpleItem(ResourceFishItems.INFUSING_UPGRADE);
        simpleItem(ResourceFishItems.BREEDING_UPGRADE);
        simpleItem(ResourceFishItems.TANK_UPGRADE);
        simpleItem(ResourceFishItems.ROUND_ROBIN_UPGRADE);

        simpleItem(ResourceFishItems.BASIC_FISH_FOOD);
        simpleItem(ResourceFishItems.METALLIC_FISH_FOOD);
        simpleItem(ResourceFishItems.CRYSTAL_FISH_FOOD);
        simpleItem(ResourceFishItems.NETHER_FISH_FOOD);
        simpleItem(ResourceFishItems.BASIC_MOB_FISH_FOOD);
        simpleItem(ResourceFishItems.ENDER_FISH_FOOD);

        simpleBlockItem(ResourceFishBlocks.AQUARIUM.get());
        simpleBlockItem(ResourceFishBlocks.BREEDING_TANK.get());
        simpleBlockItem(ResourceFishBlocks.INFUSING_TANK.get());
    }

    private void simpleItem(DeferredItem<Item> item) {
        withExistingParent(item.getId().getPath(),
                ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(ResourceFish.MOD_ID, "item/" + item.getId().getPath()));
    }

    @Override
    public String getName() {
        return ResourceFish.MOD_ID + " Item Models";
    }
}
