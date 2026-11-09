package com.benbenlaw.resourcefish.data;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.data.builders.ResourceFishBuilder;
import com.benbenlaw.resourcefish.entities.ResourceFishEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ResourceFish.MOD_ID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {

        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ResourceFishRecipes(packOutput, event.getLookupProvider()));

        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(ResourceFishLootTableProvider::new, LootContextParamSets.BLOCK)), event.getLookupProvider()));

        ResourceFishBlockTags blockTags = new ResourceFishBlockTags(packOutput, lookupProvider, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);

        ResourceFishItemTags itemTags = new ResourceFishItemTags(packOutput, lookupProvider, blockTags, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), itemTags);

        generator.addProvider(event.includeClient(), new ResourceFishBlockStateProvider(packOutput, event.getExistingFileHelper()));

        generator.addProvider(event.includeClient(), new ResourceFishItemModelsProvider(packOutput, event.getExistingFileHelper()));

        generator.addProvider(event.includeClient(), new ResourceFishLangProvider(packOutput, event.getExistingFileHelper()));

        generator.addProvider(event.includeClient(), new ResourceFishFishes(packOutput));



    }
}

