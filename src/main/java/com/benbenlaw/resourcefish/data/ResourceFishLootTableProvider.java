package com.benbenlaw.resourcefish.data;

import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ResourceFishLootTableProvider extends VanillaBlockLoot {

    public ResourceFishLootTableProvider(HolderLookup.Provider p_344962_) {
        super(p_344962_);
    }
    @Override
    protected void generate() {
        this.dropSelf(ResourceFishBlocks.TANK_CONTROLLER.get());
        this.dropSelf(ResourceFishBlocks.CAVIAR_PROCESSOR.get());
        this.dropSelf(ResourceFishBlocks.AQUARIUM.get());
        this.dropSelf(ResourceFishBlocks.BREEDING_TANK.get());
        this.dropSelf(ResourceFishBlocks.INFUSING_TANK.get());
    }

    @Override
    protected void add(@NotNull Block block, @NotNull LootTable.Builder table) {
        super.add(block, table);
        knownBlocks.add(block);
    }
    private final Set<Block> knownBlocks = new ReferenceOpenHashSet<>();

    @NotNull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }
}
