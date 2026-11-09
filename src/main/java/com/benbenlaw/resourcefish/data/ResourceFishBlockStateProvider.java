package com.benbenlaw.resourcefish.data;

import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.block.AquariumBlock;
import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Arrays;

public class ResourceFishBlockStateProvider extends BlockStateProvider {
    public ResourceFishBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ResourceFish.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        getVariantBuilder(ResourceFishBlocks.AQUARIUM.get()).forAllStates(state -> {
            int part = state.getValue(AquariumBlock.PART);
            if (part == 0)
                return ConfiguredModel.builder().modelFile(models().getExistingFile(modLoc("block/aquarium"))).build();
            int i = part - 1;
            return ConfiguredModel.builder().modelFile(tankPart(i % 3, i / 3 % 3, i / 9)).build();
        });
    }

    /** One block of the formed 3x3x3: glass and water only on the outside, glass borders only on the outer edges. */
    private BlockModelBuilder tankPart(int x, int y, int z) {
        BlockModelBuilder model = models().getBuilder("block/aquarium_tank/" + x + "_" + y + "_" + z)
                .texture("particle", mcLoc("block/glass"));

        int[] pos = {x, y, z};
        Direction[] outside = Arrays.stream(Direction.values())
                .filter(d -> pos[d.getAxis().ordinal()] == (d.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 2 : 0))
                .toArray(Direction[]::new);
        if (outside.length == 0)
            return model; // the center block draws nothing

        // Glass and water are separate children so the glass can be cut out. In one translucent model the glass's
        // see-through pixels still write depth, and when the quad sort draws the glass first the water behind vanishes.
        BlockModelBuilder glass = models().nested().renderType("minecraft:cutout")
                .texture("glass", mcLoc("block/glass"));
        ModelBuilder<BlockModelBuilder>.ElementBuilder shell = glass.element();
        for (Direction d : outside) {
            float[] uv = uv(d, x, y, z);
            shell.face(d).texture("#glass").uvs(uv[0], uv[1], uv[2], uv[3]).cullface(d);
        }

        // water inset 1px from the outer glass, with its surface 2px below the top
        BlockModelBuilder water = models().nested().renderType("minecraft:translucent")
                .texture("water", mcLoc("block/water_still"));
        ModelBuilder<BlockModelBuilder>.ElementBuilder body = water.element()
                .from(x == 0 ? 1 : 0, y == 0 ? 1 : 0, z == 0 ? 1 : 0)
                .to(x == 2 ? 15 : 16, y == 2 ? 14 : 16, z == 2 ? 15 : 16);
        for (Direction d : outside)
            body.face(d).texture("#water").tintindex(0);

        return model.customLoader(CompositeModelBuilder::begin)
                .child("glass", glass)
                .child("water", water)
                .end();
    }

    private static float[] uv(Direction d, int x, int y, int z) {
        return switch (d) {
            case DOWN -> span(x, false, z, true);
            case UP -> span(x, false, z, false);
            case NORTH -> span(x, true, y, true);
            case SOUTH -> span(x, false, y, true);
            case WEST -> span(z, false, y, true);
            case EAST -> span(z, true, y, true);
        };
    }

    private static float[] span(int u, boolean flipU, int v, boolean flipV) {
        float[] us = tile(flipU ? 2 - u : u), vs = tile(flipV ? 2 - v : v);
        return new float[] {us[0], vs[0], us[1], vs[1]};
    }

    // trim the glass border off every edge that meets another block of the tank
    private static float[] tile(int i) {
        return new float[] {i == 0 ? 0 : 1, i == 2 ? 16 : 15};
    }
}
