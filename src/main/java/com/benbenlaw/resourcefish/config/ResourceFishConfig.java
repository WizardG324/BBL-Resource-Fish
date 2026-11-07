package com.benbenlaw.resourcefish.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ResourceFishConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue NATURAL_SPAWNING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        NATURAL_SPAWNING = builder
                .comment("Whether Resource Fish spawn naturally in the world")
                .define("naturalSpawning", true);
        SPEC = builder.build();
    }
}
