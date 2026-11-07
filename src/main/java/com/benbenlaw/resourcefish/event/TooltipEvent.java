package com.benbenlaw.resourcefish.event;

import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import com.benbenlaw.resourcefish.item.ResourceFishDataComponents;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

@EventBusSubscriber(modid = ResourceFish.MOD_ID, value = Dist.CLIENT)
public class TooltipEvent {

    @SubscribeEvent
    public static void onTooltipEvent(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();

        String key = stack.is(ResourceFishBlocks.BREEDING_TANK.get().asItem()) ? "tooltip.resourcefish.breeding_tank"
                : stack.is(ResourceFishBlocks.INFUSING_TANK.get().asItem()) ? "tooltip.resourcefish.infusing_tank"
                : stack.is(ResourceFishBlocks.AQUARIUM.get().asItem()) ? "tooltip.resourcefish.aquarium" : null;
        if (key != null) {
            if (Screen.hasShiftDown()) {
                event.getTooltipElements().add(1, Either.left(Component.translatable(key).withStyle(ChatFormatting.YELLOW)));
            } else {
                event.getTooltipElements().add(1, Either.left(Component.translatable("tooltips.bblcore.shift").withStyle(ChatFormatting.YELLOW)));
            }
        }

        if (stack.is(ResourceFishItems.RESOURCE_FISH_SPAWN_EGG) ||
                stack.is(ResourceFishItems.CAVIAR)||
                stack.is(ResourceFishItems.RESOURCE_FISH_BUCKET)) return;

        if (stack.has(ResourceFishDataComponents.FISH_TYPE.get())) {
            ResourceLocation resourceType = stack.get(ResourceFishDataComponents.FISH_TYPE);
            if (resourceType == null) return;

            String[] parts = resourceType.getPath().split("_");
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
                }
            }
            String formattedName = String.join(" ", parts);

            Component newTitle = Component.literal(stack.getHoverName().getString() + " (" + formattedName + ")");

            event.getTooltipElements().set(0, Either.left(newTitle));
        }
    }



}
