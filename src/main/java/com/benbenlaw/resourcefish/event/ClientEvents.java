package com.benbenlaw.resourcefish.event;

import com.benbenlaw.core.block.colored.util.IColored;
import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import com.benbenlaw.resourcefish.entities.ResourceFishEntity;
import com.benbenlaw.resourcefish.item.ResourceFishDataComponents;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import com.benbenlaw.resourcefish.item.ResourceFishSpawnEgg;
import com.benbenlaw.resourcefish.util.ResourceType;
import com.mojang.datafixers.util.Either;
import de.cech12.ceramicbucket.CeramicBucketMod;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.checkerframework.checker.signature.qual.SignatureBottom;

import java.util.List;
import java.util.Map;


@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> {
                    int fallback = 0xAAAAAA;

                    ResourceLocation typeId = stack.get(ResourceFishDataComponents.FISH_TYPE);
                    int mainColor = fallback;
                    int patternColor = fallback;

                    if (typeId != null) {
                        ResourceType type = ResourceType.get(typeId);
                        if (type != null) {
                            mainColor = type.getColor();
                            patternColor = type.getPatternColor();
                        }
                    }

                    // Base
                    if (tintIndex == 0) {
                        //return 0xFF394F89; // Blue-ish color
                        return (0xFF << 24) | (mainColor & 0xFFFFFF);
                    }

                    // Pattern
                    if (tintIndex == 1) {
                        return (0xFF << 24) | (patternColor & 0xFFFFFF);
                    }

                    return 0xFFFFFFFF; // Default for unexpected indexes
                },

                ResourceFishItems.CAVIAR.get(),
                ResourceFishItems.RESOURCE_FISH_SPAWN_EGG.get()
        );
        event.register(
                (stack, tintIndex) -> {
                    int fallback = 0xAAAAAA;

                    ResourceLocation typeId = stack.get(ResourceFishDataComponents.FISH_TYPE);
                    int mainColor = fallback;
                    int patternColor = fallback;

                    if (typeId != null) {
                        ResourceType type = ResourceType.get(typeId);
                        if (type != null) {
                            mainColor = type.getColor();
                            patternColor = type.getPatternColor();
                        }
                    }

                    // Base
                    if (tintIndex == 0) {
                        return 0xFFFFFFFF;
                    }

                    // Fish Part
                    if (tintIndex == 1) {
                        return (0xFF << 24) | (patternColor & 0xFFFFFF);
                    }

                    // Fish Part 2
                    if (tintIndex == 2) {
                        return (0xFF << 24) | (mainColor & 0xFFFFFF);
                    }

                    return 0xFFFFFFFF;
                },

                ResourceFishItems.RESOURCE_FISH_BUCKET.get()
        );
    }

    // Default water blue, used when there's no biome context (e.g. the item in an inventory).
    private static final int DEFAULT_WATER_COLOR = 0x3F76E4;

    @SubscribeEvent
    public void registerAquariumBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> (level != null && pos != null)
                        ? BiomeColors.getAverageWaterColor(level, pos)
                        : DEFAULT_WATER_COLOR,
                ResourceFishBlocks.AQUARIUM.get(), ResourceFishBlocks.BREEDING_TANK.get(), ResourceFishBlocks.INFUSING_TANK.get());
    }

    @SubscribeEvent
    public void registerAquariumItemColors(RegisterColorHandlersEvent.Item event) {
        // Block colours don't apply to the item form, so tint the aquarium's water in inventories too.
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? DEFAULT_WATER_COLOR : -1,
                ResourceFishBlocks.AQUARIUM.get(), ResourceFishBlocks.BREEDING_TANK.get(), ResourceFishBlocks.INFUSING_TANK.get());
    }

}
