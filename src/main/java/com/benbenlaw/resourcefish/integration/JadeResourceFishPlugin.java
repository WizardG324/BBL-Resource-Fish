package com.benbenlaw.resourcefish.integration;

import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.block.BreedingTankBlock;
import com.benbenlaw.resourcefish.block.entity.BreedingTankBlockEntity;
import com.benbenlaw.resourcefish.block.entity.InfusingTankBlockEntity;
import com.benbenlaw.resourcefish.util.ResourceType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.Arrays;
import java.util.Locale;

@WailaPlugin
public class JadeResourceFishPlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // also covers InfusingTankBlock, which extends BreedingTankBlock
        registration.registerBlockComponent(TankProvider.INSTANCE, BreedingTankBlock.class);
    }

    enum TankProvider implements IBlockComponentProvider {
        INSTANCE;

        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ResourceFish.MOD_ID, "fish_tanks");

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getBlockEntity() instanceof InfusingTankBlockEntity tank) {
                line(tooltip, "jade.resourcefish.fish", tank.getFish());
            } else if (accessor.getBlockEntity() instanceof BreedingTankBlockEntity tank) {
                ResourceType[] fish = tank.getRenderedFish(); // parents first, then the bred fish
                line(tooltip, "jade.resourcefish.parents", fish[0], fish[1]);
                line(tooltip, "jade.resourcefish.bred", Arrays.copyOfRange(fish, 2, fish.length));
            }
        }

        /** "Label: A, B", skipping empty spots, nothing when there are no fish. */
        private static void line(ITooltip tooltip, String key, ResourceType... types) {
            MutableComponent names = Component.empty();
            for (ResourceType type : types) {
                if (type == ResourceType.NONE)
                    continue;
                if (!names.getSiblings().isEmpty())
                    names.append(", ");
                names.append(name(type));
            }
            if (!names.getSiblings().isEmpty())
                tooltip.add(Component.translatable(key, names.withStyle(ChatFormatting.AQUA)));
        }

        // Same name the fish entity shows.
        private static Component name(ResourceType type) {
            String path = type.getId().getPath();
            return Component.translatable("entity.resourcefish.resource_fish",
                    path.substring(0, 1).toUpperCase(Locale.ROOT) + path.substring(1).toLowerCase(Locale.ROOT));
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
