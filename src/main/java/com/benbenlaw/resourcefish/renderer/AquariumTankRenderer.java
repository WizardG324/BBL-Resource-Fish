package com.benbenlaw.resourcefish.renderer;

import com.benbenlaw.resourcefish.block.entity.AquariumBlockEntity;
import com.benbenlaw.resourcefish.util.ResourceType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AquariumTankRenderer extends AquariumRenderer<AquariumBlockEntity> {
    private static final int MAX_FISH = AquariumBlockEntity.FISH_PER_ROW * 4;

    public AquariumTankRenderer() {
        super(MAX_FISH, MAX_FISH, AquariumBlockEntity::fishArray);
    }

    @Override
    protected void place(AquariumBlockEntity be, int index, ResourceType[] types, float age, PoseStack pose) {
        if (!be.isMaster()) {
            super.place(be, index, types, age, pose);
            return;
        }
        // each fish gets its own circle, spread by the golden ratio so no two share a path
        float a = Mth.frac(index * 0.618F), b = Mth.frac(index * 0.382F + 0.1F), c = Mth.frac(index * 0.271F);
        // the water spans about -0.94-1.94 across and -0.94-1.88 up from the center block's corner
        // sqrt spreads the circles evenly over the floor area instead of bunching them in the middle
        float radius = 0.15F + 1.05F * Mth.sqrt(a);
        float height = -1.2F + 2.3F * b;
        float speed = (index % 2 == 0 ? 1.0F : -1.0F) * (1.0F + c);
        pose.translate(0.5D, 0.5D + height + Mth.sin(age * 0.08F + index) * 0.05F, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees((age * speed + index * 137.5F) % 360.0F));
        pose.translate(radius, 0.0D, 0.0D);
        pose.scale(0.6F, 0.6F, 0.6F);
    }

    @Override
    public AABB getRenderBoundingBox(AquariumBlockEntity be) {
        // the center block draws fish across the whole tank, so don't cull it when only an edge is on screen
        return be.isMaster() ? new AABB(be.getBlockPos()).inflate(1.0D) : new AABB(be.getBlockPos());
    }
}
