package com.benbenlaw.resourcefish.block;

import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ResourceFishBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(ResourceFish.MOD_ID);

    public static final DeferredBlock<Block> TANK_CONTROLLER = registerBlock("tank_controller",
            () -> new TankControllerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).sound(SoundType.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<Block> CAVIAR_PROCESSOR = registerBlock("caviar_processor",
            () -> new CaviarProcessorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).sound(SoundType.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<Block> NEST = registerBlock("nest",
            () -> new NestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).sound(SoundType.SAND)
                    .lightLevel(state -> 7)
                    .noOcclusion()));
    
    public static final DeferredBlock<Block> AQUARIUM = registerBlock("aquarium", 
            () -> new AquariumBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .noOcclusion()));

    public static final DeferredBlock<Block> BREEDING_TANK = registerBlock("breeding_tank",
            () -> new BreedingTankBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .noOcclusion()));

    public static final DeferredBlock<Block> INFUSING_TANK = registerBlock("infusing_tank",
            () -> new InfusingTankBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .noOcclusion()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = (DeferredBlock<T>) BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ResourceFishItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));

    }
}
