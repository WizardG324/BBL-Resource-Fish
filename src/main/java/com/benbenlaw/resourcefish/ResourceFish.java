package com.benbenlaw.resourcefish;

import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import com.benbenlaw.resourcefish.block.entity.CaviarProcessorBlockEntity;
import com.benbenlaw.resourcefish.block.entity.ResourceFishBlockEntities;
import com.benbenlaw.resourcefish.entities.ResourceFishEntities;
import com.benbenlaw.resourcefish.entities.ResourceFishEntity;
import com.benbenlaw.resourcefish.event.ClientEvents;
import com.benbenlaw.resourcefish.item.ResourceFishCreativeTab;
import com.benbenlaw.resourcefish.item.ResourceFishDataComponents;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import com.benbenlaw.resourcefish.network.ResourceFishNetworking;
import com.benbenlaw.resourcefish.recipe.ResourceFishRecipes;
import com.benbenlaw.resourcefish.block.entity.BreedingTankBlockEntity;
import com.benbenlaw.resourcefish.renderer.AquariumRenderer;
import com.benbenlaw.resourcefish.util.ResourceType;
import com.benbenlaw.resourcefish.renderer.ResourceFishRenderer;
import com.benbenlaw.resourcefish.screen.CaviarProcessorScreen;
import com.benbenlaw.resourcefish.screen.ResourceFishMenuTypes;
import com.benbenlaw.resourcefish.screen.TankControllerScreen;
import com.benbenlaw.resourcefish.util.ResourceTypeLoader;
import com.google.gson.Gson;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import com.benbenlaw.resourcefish.config.ResourceFishConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;

@Mod(ResourceFish.MOD_ID)
public class ResourceFish{

    public static final String MOD_ID = "resourcefish";
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    public ResourceFish(final IEventBus modEventBus, final ModContainer modContainer) {

        modContainer.registerConfig(ModConfig.Type.SERVER, ResourceFishConfig.SPEC, "bbl/resourcefish-server.toml");

        ResourceFishCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);

        ResourceFishBlocks.BLOCKS.register(modEventBus);
        ResourceFishBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        ResourceFishItems.ITEMS.register(modEventBus);
        ResourceFishDataComponents.COMPONENTS.register(modEventBus);
        ResourceFishEntities.ENTITIES.register(modEventBus);

        ResourceFishMenuTypes.MENUS.register(modEventBus);

        ResourceFishRecipes.TYPES.register(modEventBus);
        ResourceFishRecipes.SERIALIZER.register(modEventBus);

        modEventBus.addListener(ResourceFish::registerAttributes);
        modEventBus.addListener(ResourceFish::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(ResourceFish::onAddReloadListener);

        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(ResourceFishEntities::registerSpawnPlacements);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.register(new ClientEvents());
        }

    }

    public void commonSetup(RegisterPayloadHandlersEvent event) {
        ResourceFishNetworking.registerNetworking(event);
    }

    @SubscribeEvent
    private static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new ResourceTypeLoader(new Gson(), "fish"));
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ResourceFishEntities.RESOURCE_FISH.get(), ResourceFishEntity.createAttributes().build());
    }

    @EventBusSubscriber(modid = ResourceFish.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ResourceFishEntities.RESOURCE_FISH.get(), ResourceFishRenderer::new);
            event.registerBlockEntityRenderer(ResourceFishBlockEntities.AQUARIUM_BLOCK_ENTITY.get(),
                    context -> new AquariumRenderer<>(1, 1, aquarium -> new ResourceType[] { aquarium.getFishType() }));
            event.registerBlockEntityRenderer(ResourceFishBlockEntities.BREEDING_TANK_BLOCK_ENTITY.get(),
                    context -> new AquariumRenderer<>(5, 2, BreedingTankBlockEntity::getRenderedFish));
            event.registerBlockEntityRenderer(ResourceFishBlockEntities.INFUSING_TANK_BLOCK_ENTITY.get(),
                    context -> new AquariumRenderer<>(1, 1, tank -> new ResourceType[] { tank.getFish() }));
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ResourceFishMenuTypes.TANK_CONTROLLER_MENU.get(), TankControllerScreen::new);
            event.register(ResourceFishMenuTypes.CAVIAR_PROCESSOR_MENU.get(), CaviarProcessorScreen::new);
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        ResourceFishBlockEntities.registerCapabilities(event);
    }

}
