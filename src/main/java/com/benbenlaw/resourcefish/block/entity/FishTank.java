package com.benbenlaw.resourcefish.block.entity;

import com.benbenlaw.resourcefish.util.ResourceType;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

public interface FishTank {
    // Adds a fish if there is room, false when full.
    boolean addFish(ResourceType type);

    // Takes a fish out, NONE when empty.
    ResourceType takeFish();

    // Slots before this are inputs, the rest are outputs.
    int firstOutputSlot();

    ItemStackHandler getInventory();

    IItemHandler getItemHandlerCapability(@Nullable Direction side);

    void tick();

    void onBroken();
}
