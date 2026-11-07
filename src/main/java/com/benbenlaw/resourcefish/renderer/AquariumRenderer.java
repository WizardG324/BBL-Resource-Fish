package com.benbenlaw.resourcefish.renderer;

import com.benbenlaw.resourcefish.entities.ResourceFishEntities;
import com.benbenlaw.resourcefish.entities.ResourceFishEntity;
import com.benbenlaw.resourcefish.util.ResourceType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Function;

/**
 * Renders a tank's fish (one for the aquarium, two for the breeding tank) by reusing the entity's own renderer: a client-only dummy
 * {@link ResourceFishEntity} per fish is configured to its {@link ResourceType} and drawn through the
 * entity dispatcher, so all colour/pattern logic is inherited for free. The dummy is never added to
 * a level, so it never ticks - we drive its swim animation by hand from the world time.
 */
@OnlyIn(Dist.CLIENT)
public class AquariumRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    private final Function<T, ResourceType[]> fishOf;
    private final ResourceFishEntity[] dummies;
    private final ResourceLocation[] renderedTypeIds;

    private final int adults;

    public AquariumRenderer(int maxFish, int adults, Function<T, ResourceType[]> fishOf) {
        this.adults = adults;
        this.fishOf = fishOf;
        this.dummies = new ResourceFishEntity[maxFish];
        this.renderedTypeIds = new ResourceLocation[maxFish];
    }

    @Override
    public void render(T be, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null)
            return;
        ResourceType[] types = fishOf.apply(be);
        for (int i = 0; i < types.length && i < dummies.length; i++) {
            if (types[i] != ResourceType.NONE)
                renderFish(be, level, i, types, partialTick, pose, buffers, packedLight);
        }
    }

    private void renderFish(T be, Level level, int index, ResourceType[] types, float partialTick, PoseStack pose,
                            MultiBufferSource buffers, int packedLight) {
        ResourceFishEntity fish = getOrCreate(level, index, types[index]);

        long time = level.getGameTime();
        // Per-block phase offset so neighbouring tanks don't swim in lockstep.
        BlockPos pos = be.getBlockPos();
        float phase = ((pos.getX() * 7 + pos.getZ() * 13) & 0xFF);
        float age = time + phase + partialTick;

        // tickCount feeds the fish's built-in sinusoidal sway (see ResourceFishRenderer#setupRotations).
        fish.tickCount = (int) (time + phase);

        boolean fry = index >= adults;
        // each group (adults, fry) spreads evenly around its own ring
        int groupSize = fry ? Math.min(types.length, dummies.length) - adults : Math.min(types.length, adults);
        int slot = fry ? index - adults : index;

        pose.pushPose();
        pose.translate(0.5D, fry ? 0.08D : 0.45D, 0.5D);
        float speed = fry ? -3.0F : 2.0F; // fry circle the other way, a bit quicker
        pose.mulPose(Axis.YP.rotationDegrees((age * speed + 360.0F * slot / groupSize) % 360.0F));
        float scale = fry ? 0.25F : groupSize == 1 ? 0.6F : 0.45F;
        double radius = fry ? 0.28D : groupSize == 1 ? 0.0D : 0.2D;
        pose.translate(radius, Mth.sin(age * 0.1F + index) * (fry ? 0.02F : 0.05F), 0.0D); // gentle bob
        pose.scale(scale, scale, scale);

        EntityRenderDispatcher erd = Minecraft.getInstance().getEntityRenderDispatcher();
        erd.setRenderShadow(false);
        erd.render(fish, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, pose, buffers, packedLight);
        erd.setRenderShadow(true);

        pose.popPose();
    }

    private ResourceFishEntity getOrCreate(Level level, int index, ResourceType type) {
        if (dummies[index] == null || dummies[index].level() != level) {
            dummies[index] = new ResourceFishEntity(ResourceFishEntities.RESOURCE_FISH.get(), level) {
                @Override
                public boolean isInWater() {
                    return true; // keep the fish upright; it lives in the tank
                }
            };
            renderedTypeIds[index] = null;
        }
        if (!type.getId().equals(renderedTypeIds[index])) {
            dummies[index].setResourceType(type);
            renderedTypeIds[index] = type.getId();
        }
        return dummies[index];
    }
}
