package com.benbenlaw.resourcefish.data;

import com.benbenlaw.core.recipe.ChanceResult;
import com.benbenlaw.core.tag.CommonTags;
import com.benbenlaw.resourcefish.ResourceFish;
import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import com.benbenlaw.resourcefish.data.builders.CaviarProcessorRecipeBuilder;
import com.benbenlaw.resourcefish.data.builders.FishBreedingRecipeBuilder;
import com.benbenlaw.resourcefish.data.builders.FishInfusingRecipeBuilder;
import com.benbenlaw.resourcefish.item.CaviarItem;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import com.benbenlaw.resourcefish.util.ResourceFishTags;
import com.benbenlaw.resourcefish.util.SizedIngredientChanceResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ResourceFishRecipes extends RecipeProvider {

    public ResourceFishRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    record BreedingRecipe(String parent1, String parent2, SizedIngredient catalyst, int time, double successChance, String child, boolean withCondition) {}
    record InfusingRecipe(String fishToCovert, SizedIngredient input1, SizedIngredient input2, SizedIngredient input3, int time, double successChance, String result  ) {}
    record InfusingRecipeSingle(String fishToCovert, NonNullList<SizedIngredient> inputs, int time, double successChance, String result, boolean withCondition) {}
    record CaviarSimple(String fish, boolean withCondition, SizedIngredientChanceResult... chanceResults) {}


    @Override
    protected void buildRecipes(RecipeOutput consumer) {

        Item basicFishFood = ResourceFishItems.BASIC_FISH_FOOD.get();
        Item metallicFishFood = ResourceFishItems.METALLIC_FISH_FOOD.get();
        Item crystalFishFood = ResourceFishItems.CRYSTAL_FISH_FOOD.get();
        Item netherFishFood = ResourceFishItems.NETHER_FISH_FOOD.get();
        Item enderFishFood = ResourceFishItems.ENDER_FISH_FOOD.get();
        Item basicMobFishFood = ResourceFishItems.BASIC_MOB_FISH_FOOD.get();


        List<BreedingRecipe> breedingRecipes = new ArrayList<>();
        List<InfusingRecipeSingle> infusingRecipes = new ArrayList<>();
        List<CaviarSimple> caviarSimpleRecipes = new ArrayList<>();



        //Dirt
        breedingRecipes.add(new BreedingRecipe("dirt", "dirt", SizedIngredient.of(basicFishFood, 1), 100, 0.15, "dirt", false));
        caviarSimpleRecipes.add(new CaviarSimple("dirt", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.DIRT, 1), 0.4f)));

        //Water
        breedingRecipes.add(new BreedingRecipe("water", "water", SizedIngredient.of(basicFishFood, 1), 100, 0.15, "water", false));
        CaviarProcessorRecipeBuilder.caviarProcessorRecipeBuilder(CaviarItem.createCaviarStack("water"),
                        List.of()).withFluid(new FluidStack(Fluids.WATER, 25))
                .save(consumer, ResourceLocation.fromNamespaceAndPath(ResourceFish.MOD_ID, "caviar/water"));

        //Lava
        breedingRecipes.add(new BreedingRecipe("lava", "lava", SizedIngredient.of(basicFishFood, 1), 100, 0.15, "lava", false));
        CaviarProcessorRecipeBuilder.caviarProcessorRecipeBuilder(CaviarItem.createCaviarStack("lava"),
                        List.of()).withFluid(new FluidStack(Fluids.LAVA, 25))
                .save(consumer, ResourceLocation.fromNamespaceAndPath(ResourceFish.MOD_ID, "caviar/lava"));

        //Cobblestone
        breedingRecipes.add(new BreedingRecipe("cobblestone", "cobblestone", SizedIngredient.of(basicFishFood, 1), 100, 0.15, "cobblestone", false));
        caviarSimpleRecipes.add(new CaviarSimple("cobblestone", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.COBBLESTONE, 1), 0.4f)));

        //Wood
        breedingRecipes.add(new BreedingRecipe("wood", "wood", SizedIngredient.of(basicFishFood, 1), 100, 0.15, "wood", false));
        infusingRecipes.add(new InfusingRecipeSingle("wood", inputList(SizedIngredient.of(ItemTags.LOGS, 64)),100, 1.0, "wood", false));
        caviarSimpleRecipes.add(new CaviarSimple("wood", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.STICK, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.OAK_LOG,  1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.SPRUCE_LOG, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BIRCH_LOG, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.JUNGLE_LOG, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.ACACIA_LOG, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.DARK_OAK_LOG, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.CHERRY_LOG, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.MANGROVE_LOG, 1), 0.05f)));


        //Stone
        breedingRecipes.add(new BreedingRecipe("water", "lava", SizedIngredient.of(basicFishFood, 1), 100, 0.1, "stone", false));;
        infusingRecipes.add(new InfusingRecipeSingle("water", inputList(SizedIngredient.of(Items.STONE, 64)),100, 1.0, "stone", false));
        caviarSimpleRecipes.add(new CaviarSimple("stone",false, new SizedIngredientChanceResult(SizedIngredient.of(Items.STONE, 1), 0.4f)));

        //Granite
        breedingRecipes.add(new BreedingRecipe("stone", "red", SizedIngredient.of(basicFishFood, 1), 100, 0.15, "granite", false));
        infusingRecipes.add(new InfusingRecipeSingle("stone", inputList(SizedIngredient.of(Items.GRANITE, 64)),100, 1.0, "granite", false));
        caviarSimpleRecipes.add(new CaviarSimple("granite", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.GRANITE, 1), 0.4f)));

        //Diorite
        breedingRecipes.add(new BreedingRecipe("stone", "white", SizedIngredient.of(basicFishFood, 1), 100, 0.15, "diorite", false));
        infusingRecipes.add(new InfusingRecipeSingle("stone", inputList(SizedIngredient.of(Items.DIORITE, 64)),100, 1.0, "diorite", false));
        caviarSimpleRecipes.add(new CaviarSimple("diorite", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.DIORITE, 1), 0.4f)));

        //Andesite
        breedingRecipes.add(new BreedingRecipe("stone", "gray", SizedIngredient.of(basicFishFood, 1), 100, 0.15, "andesite", false));
        infusingRecipes.add(new InfusingRecipeSingle("stone", inputList(SizedIngredient.of(Items.ANDESITE, 64)),100, 1.0, "andesite", false));;
        caviarSimpleRecipes.add(new CaviarSimple("andesite", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.ANDESITE, 1), 0.4f)));

        //Sand
        breedingRecipes.add(new BreedingRecipe("dirt", "water", SizedIngredient.of(basicFishFood, 1), 100, 0.5, "sand", false));
        infusingRecipes.add(new InfusingRecipeSingle("dirt", inputList(SizedIngredient.of(Items.SAND, 64)),100, 1.0, "sand", false));
        caviarSimpleRecipes.add(new CaviarSimple("sand", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.SAND, 1), 0.4f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.RED_SAND, 1), 0.1f)));

        //Gravel
        breedingRecipes.add(new BreedingRecipe("stone", "water", SizedIngredient.of(basicFishFood, 1), 100, 0.5, "gravel", false));
        infusingRecipes.add(new InfusingRecipeSingle("stone", inputList(SizedIngredient.of(Items.GRAVEL, 64)),100, 1.0, "gravel", false));
        caviarSimpleRecipes.add(new CaviarSimple("gravel", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.GRAVEL, 1), 0.4f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.FLINT, 1), 0.05f)));

        //Coal - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("wood", "black", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "coal", false));
        infusingRecipes.add(new InfusingRecipeSingle("wood", oreInputList(SizedIngredient.of(Items.COAL, 64)),100, 1.0, "coal", false));
        caviarSimpleRecipes.add(new CaviarSimple("coal", false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("coal", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Lapis - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("coal", "blue", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "lapis", false));
        infusingRecipes.add(new InfusingRecipeSingle("coal", oreInputList(SizedIngredient.of(Items.LAPIS_LAZULI, 64)),100, 1.0, "lapis", false));
        caviarSimpleRecipes.add(new CaviarSimple("lapis", false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("lapis", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Redstone - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("lapis", "red", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "redstone", false));
        infusingRecipes.add(new InfusingRecipeSingle("lapis", oreInputList(SizedIngredient.of(Items.REDSTONE, 64)),100, 1.0, "redstone", false));;
        caviarSimpleRecipes.add(new CaviarSimple("redstone",false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("redstone", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Diamond - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("lapis", "light_blue", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "diamond", false));
        infusingRecipes.add(new InfusingRecipeSingle("lapis", oreInputList(SizedIngredient.of(Items.DIAMOND, 64)),100, 1.0, "diamond", false));
        caviarSimpleRecipes.add(new CaviarSimple("diamond", false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("diamond", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Emerald - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("lapis", "green", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "emerald", false));
        infusingRecipes.add(new InfusingRecipeSingle("lapis", oreInputList(SizedIngredient.of(Items.EMERALD, 64)),100, 1.0, "emerald", false));
        caviarSimpleRecipes.add(new CaviarSimple("emerald", false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("emerald", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Copper - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("stone", "orange", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "copper", false));
        infusingRecipes.add(new InfusingRecipeSingle("stone", oreInputList(SizedIngredient.of(Items.COPPER_INGOT, 64)), 100, 1.0, "copper", false));
        caviarSimpleRecipes.add(new CaviarSimple("copper", false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("copper", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Iron - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("copper", "white", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "iron", false));
        infusingRecipes.add(new InfusingRecipeSingle("copper", oreInputList(SizedIngredient.of(Items.IRON_INGOT, 64)),100, 2.0, "iron", false));
        caviarSimpleRecipes.add(new CaviarSimple("iron", false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("iron", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Gold - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("iron", "yellow", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "gold", false));
        infusingRecipes.add(new InfusingRecipeSingle("iron", oreInputList(SizedIngredient.of(Items.GOLD_INGOT, 64)),100, 2.0, "gold", false));
        caviarSimpleRecipes.add(new CaviarSimple("gold", false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("gold", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Netherrack - Nether Fish Food
        breedingRecipes.add(new BreedingRecipe("stone", "lava", SizedIngredient.of(netherFishFood, 1), 100, 0.2, "netherrack", false));
        infusingRecipes.add(new InfusingRecipeSingle("lava", inputList(SizedIngredient.of(Items.NETHERRACK, 64)),100, 0.5, "netherrack", false));
        caviarSimpleRecipes.add(new CaviarSimple("netherrack", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.NETHERRACK, 1), 0.4f)));

        //Soul Sand - Nether Fish Food
        breedingRecipes.add(new BreedingRecipe("sand", "lava", SizedIngredient.of(netherFishFood, 1), 100, 0.2, "soul_sand", false));
        infusingRecipes.add(new InfusingRecipeSingle("lava", inputList(SizedIngredient.of(Items.SOUL_SAND, 64)),100, 0.5, "soul_sand", false));
        caviarSimpleRecipes.add(new CaviarSimple("soul_sand", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.SOUL_SAND, 1), 0.4f)));

        //Quartz - Nether Fish Food
        breedingRecipes.add(new BreedingRecipe("netherrack", "white", SizedIngredient.of(netherFishFood, 1), 100, 0.2, "quartz", false));
        infusingRecipes.add(new InfusingRecipeSingle("netherrack", oreInputList(SizedIngredient.of(Items.QUARTZ, 32)),100, 1.0, "quartz", false));
        caviarSimpleRecipes.add(new CaviarSimple("quartz", false, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("quartz", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Glowstone - Nether Fish Food
        breedingRecipes.add(new BreedingRecipe("netherrack", "yellow", SizedIngredient.of(netherFishFood, 1), 100, 0.2, "glowstone", false));
        infusingRecipes.add(new InfusingRecipeSingle("netherrack", inputList(SizedIngredient.of(Items.GLOWSTONE_DUST, 16)),100, 0.25, "glowstone", false));
        caviarSimpleRecipes.add(new CaviarSimple("glowstone", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.GLOWSTONE_DUST, 1), 0.2f)));

        //Zombie - Basic Mob Fish Food
        breedingRecipes.add(new BreedingRecipe("dirt", "green", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "zombie", false));
        infusingRecipes.add(new InfusingRecipeSingle("dirt", inputList(SizedIngredient.of(Items.ROTTEN_FLESH, 64)),200, 0.5, "zombie", false));
        caviarSimpleRecipes.add(new CaviarSimple("zombie", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.ROTTEN_FLESH, 1), 0.2f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.POTATO, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.CARROT, 1), 0.05f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.ZOMBIE_HEAD, 1), 0.01f)));

        //Skeleton - Basic Mob Fish Food
        breedingRecipes.add(new BreedingRecipe("dirt", "white", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "skeleton", false));
        infusingRecipes.add(new InfusingRecipeSingle("stone", inputList(SizedIngredient.of(Items.BONE, 64)),200, 0.5, "skeleton", false));
        caviarSimpleRecipes.add(new CaviarSimple("skeleton", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BONE, 1), 0.2f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.SKELETON_SKULL, 1), 0.01f)));

        //Creeper - Basic Mob Fish Food
        breedingRecipes.add(new BreedingRecipe("dirt", "green", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "creeper", false));
        infusingRecipes.add(new InfusingRecipeSingle("stone", inputList(SizedIngredient.of(Items.GUNPOWDER, 64)),200, 0.5, "creeper", false));
        caviarSimpleRecipes.add(new CaviarSimple("creeper", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.GUNPOWDER, 1), 0.2f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.CREEPER_HEAD, 1), 0.01f)));

        //Spider - Basic Mob Fish Food
        breedingRecipes.add(new BreedingRecipe("dirt", "black", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "spider", false));
        infusingRecipes.add(new InfusingRecipeSingle("wood", inputList(SizedIngredient.of(Items.STRING, 64)),200, 0.5, "spider", false));
        caviarSimpleRecipes.add(new CaviarSimple("spider", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.STRING, 1), 0.2f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.SPIDER_EYE, 1), 0.2f)));

        //Colors
        for (DyeColor color : DyeColor.values()) {

            String colorName = color.getSerializedName();
            TagKey<Item> dyeTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dyes/" + colorName));
            Item dyeItem = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(color + "_dye"));

            infusingRecipes.add(new InfusingRecipeSingle("water", inputList(SizedIngredient.of(dyeTag, 1)),200, 0.5, colorName, false));
            caviarSimpleRecipes.add(new CaviarSimple(colorName, false, new SizedIngredientChanceResult(SizedIngredient.of(dyeItem, 1), 0.3f)));
        }

        //Tin - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("stone", "light_gray", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "tin", true));
        infusingRecipes.add(new InfusingRecipeSingle("stone", oreInputList(SizedIngredient.of(CommonTags.getTag("tin", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "tin", true));
        caviarSimpleRecipes.add(new CaviarSimple("tin", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("tin", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Aluminum - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("copper", "light_gray", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "aluminum", true));
        infusingRecipes.add(new InfusingRecipeSingle("copper", oreInputList(SizedIngredient.of(CommonTags.getTag("aluminum", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "aluminum", true));
        caviarSimpleRecipes.add(new CaviarSimple("aluminum", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("aluminum", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Lead - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("iron", "cyan", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "lead", true));
        infusingRecipes.add(new InfusingRecipeSingle("iron", oreInputList(SizedIngredient.of(CommonTags.getTag("lead", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "lead", true));
        caviarSimpleRecipes.add(new CaviarSimple("lead", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("lead", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Nickel - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("iron", "orange", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "nickel", true));
        infusingRecipes.add(new InfusingRecipeSingle("iron", oreInputList(SizedIngredient.of(CommonTags.getTag("nickel", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "nickel", true));
        caviarSimpleRecipes.add(new CaviarSimple("nickel", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("nickel", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Osmium - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("gold", "blue", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "osmium", true));
        infusingRecipes.add(new InfusingRecipeSingle("gold", oreInputList(SizedIngredient.of(CommonTags.getTag("osmium", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "osmium", true));
        caviarSimpleRecipes.add(new CaviarSimple("osmium", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("osmium", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Platinum - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("gold", "light_gray", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "platinum", true));
        infusingRecipes.add(new InfusingRecipeSingle("gold", oreInputList(SizedIngredient.of(CommonTags.getTag("platinum", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "platinum", true));
        caviarSimpleRecipes.add(new CaviarSimple("platinum", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("platinum", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Silver - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("iron", "light_gray", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "silver", true));
        infusingRecipes.add(new InfusingRecipeSingle("iron", oreInputList(SizedIngredient.of(CommonTags.getTag("silver", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "silver", true));
        caviarSimpleRecipes.add(new CaviarSimple("silver", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("silver", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Uranium - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("gold", "green", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "uranium", true));
        infusingRecipes.add(new InfusingRecipeSingle("gold", oreInputList(SizedIngredient.of(CommonTags.getTag("uranium", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "uranium", true));
        caviarSimpleRecipes.add(new CaviarSimple("uranium", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("uranium", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Zinc - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("copper", "gray", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "zinc", true));
        infusingRecipes.add(new InfusingRecipeSingle("copper", oreInputList(SizedIngredient.of(CommonTags.getTag("zinc", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "zinc", true));
        caviarSimpleRecipes.add(new CaviarSimple("zinc", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("zinc", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Iridium - Metallic Fish Food
        breedingRecipes.add(new BreedingRecipe("gold", "white", SizedIngredient.of(metallicFishFood, 1), 100, 0.15, "iridium", true));
        infusingRecipes.add(new InfusingRecipeSingle("iron", oreInputList(SizedIngredient.of(CommonTags.getTag("iridium", CommonTags.ResourceType.INGOTS), 64)),100, 1.0, "iridium", true));
        caviarSimpleRecipes.add(new CaviarSimple("iridium", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("iridium", CommonTags.ResourceType.ORES), 1), 0.2f)));



        //Ruby - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("emerald", "red", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "ruby", true));
        infusingRecipes.add(new InfusingRecipeSingle("emerald", oreInputList(SizedIngredient.of(CommonTags.getTag("ruby", CommonTags.ResourceType.GEMS), 64)),100, 1.0, "ruby", true));
        caviarSimpleRecipes.add(new CaviarSimple("ruby", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("ruby", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Sapphire - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("emerald", "blue", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "sapphire", true));
        infusingRecipes.add(new InfusingRecipeSingle("emerald", oreInputList(SizedIngredient.of(CommonTags.getTag("sapphire", CommonTags.ResourceType.GEMS), 64)),100, 1.0, "sapphire", true));
        caviarSimpleRecipes.add(new CaviarSimple("sapphire", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("sapphire", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Peridot - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("emerald", "green", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "peridot", true));
        infusingRecipes.add(new InfusingRecipeSingle("emerald", oreInputList(SizedIngredient.of(CommonTags.getTag("peridot", CommonTags.ResourceType.GEMS), 64)),100, 1.0, "peridot", true));
        caviarSimpleRecipes.add(new CaviarSimple("peridot", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("peridot", CommonTags.ResourceType.ORES), 1), 0.2f)));

        //Amethyst - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("lapis", "purple", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "amethyst", false));
        infusingRecipes.add(new InfusingRecipeSingle("lapis", inputList(SizedIngredient.of(Items.AMETHYST_SHARD, 64)),100, 0.25, "amethyst", false));
        caviarSimpleRecipes.add(new CaviarSimple("amethyst", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.AMETHYST_SHARD, 1), 0.2f)));

        /*

        //Sulfur - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("coal", "yellow", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "sulfur", true));
        infusingRecipes.add(new InfusingRecipeSingle("coal", SizedIngredient.of(CommonTags.getTag("sulfur", CommonTags.ResourceType.DUSTS), 64),100, 0.25, "sulfur", true));
        caviarSimpleRecipes.add(new CaviarSimple("sulfur", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("sulfur", CommonTags.ResourceType.DUSTS), 1), 0.2f)));

        //Fluorite - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("diamond", "magenta", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "fluorite", true));
        infusingRecipes.add(new InfusingRecipeSingle("diamond", SizedIngredient.of(CommonTags.getTag("fluorite", CommonTags.ResourceType.GEMS), 64),100, 0.25, "fluorite", true));
        caviarSimpleRecipes.add(new CaviarSimple("fluorite", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("fluorite", CommonTags.ResourceType.GEMS), 1), 0.2f)));

        //Cinnabar - Enriched Fish Food
        breedingRecipes.add(new BreedingRecipe("redstone", "red", SizedIngredient.of(crystalFishFood, 1), 100, 0.15, "cinnabar", true));
        infusingRecipes.add(new InfusingRecipeSingle("redstone", SizedIngredient.of(CommonTags.getTag("cinnabar", CommonTags.ResourceType.DUSTS), 64),100, 0.25, "cinnabar", true));
        caviarSimpleRecipes.add(new CaviarSimple("cinnabar", true, new SizedIngredientChanceResult(
                SizedIngredient.of(CommonTags.getTag("cinnabar", CommonTags.ResourceType.DUSTS), 1), 0.2f)));

        */

        //Basalt - Nether Fish Food
        breedingRecipes.add(new BreedingRecipe("lava", "black", SizedIngredient.of(netherFishFood, 1), 100, 0.2, "basalt", false));
        infusingRecipes.add(new InfusingRecipeSingle("lava", inputList(SizedIngredient.of(Items.BASALT, 64)),100, 0.5, "basalt", false));
        caviarSimpleRecipes.add(new CaviarSimple("basalt", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.BASALT, 1), 0.4f)));

        //Ancient Debris - Nether Fish Food
        breedingRecipes.add(new BreedingRecipe("netherrack", "basalt", SizedIngredient.of(netherFishFood, 1), 200, 0.1, "ancient", false));
        infusingRecipes.add(new InfusingRecipeSingle("netherrack", oreInputList(SizedIngredient.of(Items.ANCIENT_DEBRIS, 64)),200, 1.0, "ancient", false));
        caviarSimpleRecipes.add(new CaviarSimple("ancient", false, new SizedIngredientChanceResult(
                SizedIngredient.of(Items.ANCIENT_DEBRIS, 1), 0.05f)));

        //Obsidian - Crystal Fish Food
        breedingRecipes.add(new BreedingRecipe("lava", "water", SizedIngredient.of(crystalFishFood, 1), 200, 0.1, "obsidian", false));
        infusingRecipes.add(new InfusingRecipeSingle("lava", inputList(SizedIngredient.of(Items.OBSIDIAN, 64)),200, 0.1, "obsidian", false));
        caviarSimpleRecipes.add(new CaviarSimple("obsidian", false, new SizedIngredientChanceResult(
                SizedIngredient.of(Items.OBSIDIAN, 1), 0.05f)));

        //Blaze - Mob Fish Food
        breedingRecipes.add(new BreedingRecipe("lava", "netherrack", SizedIngredient.of(basicMobFishFood, 1), 300, 0.1, "blaze", false));
        infusingRecipes.add(new InfusingRecipeSingle("lava", inputList(SizedIngredient.of(Items.BLAZE_ROD, 64)),300, 0.1, "blaze", false));
        caviarSimpleRecipes.add(new CaviarSimple("blaze", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BLAZE_ROD, 1), 0.1f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BLAZE_POWDER, 1), 0.2f)));

        //End Stone - Ender Fish Food
        breedingRecipes.add(new BreedingRecipe("stone", "yellow", SizedIngredient.of(enderFishFood, 1), 300, 0.1, "end_stone", false));
        infusingRecipes.add(new InfusingRecipeSingle("stone", inputList(SizedIngredient.of(Items.END_STONE, 64)),300, 0.1, "end_stone", false));
        caviarSimpleRecipes.add(new CaviarSimple("end_stone", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.END_STONE, 1), 0.2f)));

        //Enderman Fish
        breedingRecipes.add(new BreedingRecipe("end_stone", "black", SizedIngredient.of(enderFishFood, 1), 300, 0.1, "enderman", false));
        infusingRecipes.add(new InfusingRecipeSingle("end_stone", inputList(SizedIngredient.of(Items.END_STONE, 64)),300, 0.1, "enderman", false));
        caviarSimpleRecipes.add(new CaviarSimple("enderman", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.ENDER_PEARL, 1), 0.2f)));

        //Wither Skeleton Fish
        breedingRecipes.add(new BreedingRecipe("skeleton", "coal", SizedIngredient.of(netherFishFood, 1), 300, 0.05, "wither_skeleton", false));
        infusingRecipes.add(new InfusingRecipeSingle("skeleton", inputList(SizedIngredient.of(Items.WITHER_SKELETON_SKULL, 3)),300, 0.05, "wither_skeleton", false));
        caviarSimpleRecipes.add(new CaviarSimple("wither_skeleton", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.WITHER_SKELETON_SKULL, 1), 0.02f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.COAL, 1), 0.1f)));

        //Ghast Fish
        breedingRecipes.add(new BreedingRecipe("skeleton", "white", SizedIngredient.of(netherFishFood, 1), 300, 0.05, "ghast", false));
        infusingRecipes.add(new InfusingRecipeSingle("skeleton", inputList(SizedIngredient.of(Items.GHAST_TEAR, 3)),300, 0.05, "ghast", false));
        caviarSimpleRecipes.add(new CaviarSimple("ghast", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.GHAST_TEAR, 1), 0.02f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.GUNPOWDER, 1), 0.1f)));

        //Slime Fish
        breedingRecipes.add(new BreedingRecipe("green", "dirt", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "slime", false));
        infusingRecipes.add(new InfusingRecipeSingle("green", inputList(SizedIngredient.of(Items.SLIME_BALL, 12)),200, 0.25, "slime", false));
        caviarSimpleRecipes.add(new CaviarSimple("slime", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.SLIME_BALL, 1), 0.2f)));

        //Magma Cube Fish
        breedingRecipes.add(new BreedingRecipe("lava", "orange", SizedIngredient.of(netherFishFood, 1), 200, 0.2, "magma_cube", false));
        infusingRecipes.add(new InfusingRecipeSingle("lava", inputList(SizedIngredient.of(Items.MAGMA_CREAM, 12)),200, 0.25, "magma_cube", false));
        caviarSimpleRecipes.add(new CaviarSimple("magma_cube", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.MAGMA_CREAM, 1), 0.2f)));

        //Cow Fish
        breedingRecipes.add(new BreedingRecipe("black", "white", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "cow", false));
        infusingRecipes.add(new InfusingRecipeSingle("black", inputList(SizedIngredient.of(Items.LEATHER, 12), SizedIngredient.of(Items.BEEF, 12)),200, 0.25, "cow", false));
        caviarSimpleRecipes.add(new CaviarSimple("cow", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.LEATHER, 1), 0.2f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BEEF, 1), 0.2f)));

        //Pig Fish
        breedingRecipes.add(new BreedingRecipe("pink", "pink", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "pig", false));
        infusingRecipes.add(new InfusingRecipeSingle("pink", inputList(SizedIngredient.of(Items.PORKCHOP, 12)),200, 0.25, "pig", false));
        caviarSimpleRecipes.add(new CaviarSimple("pig", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.PORKCHOP, 1), 0.2f)));

        //Sheep Fish
        breedingRecipes.add(new BreedingRecipe("white", "white", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "sheep", false));
        infusingRecipes.add(new InfusingRecipeSingle("white", inputList(SizedIngredient.of(ItemTags.WOOL, 12), SizedIngredient.of(Items.MUTTON, 12)),200, 0.25, "sheep", false));
        caviarSimpleRecipes.add(new CaviarSimple("sheep", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.WHITE_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BLACK_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BLUE_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BROWN_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.CYAN_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.GRAY_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.LIGHT_BLUE_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.LIGHT_GRAY_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.PINK_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.MAGENTA_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.ORANGE_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.RED_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.GREEN_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.LIME_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.YELLOW_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.PURPLE_WOOL, 1), 0.025f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.MUTTON, 1), 0.2f)));

        //Chicken Fish
        breedingRecipes.add(new BreedingRecipe("white", "yellow", SizedIngredient.of(basicMobFishFood, 1), 200, 0.2, "chicken", false));
        infusingRecipes.add(new InfusingRecipeSingle("white", inputList(SizedIngredient.of(Items.FEATHER, 12), SizedIngredient.of(Items.CHICKEN, 12)),200, 0.25, "chicken", false));
        caviarSimpleRecipes.add(new CaviarSimple("chicken", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.CHICKEN, 1), 0.2f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.EGG, 1), 0.1f)));

        //Clay
        breedingRecipes.add(new BreedingRecipe("sand", "water", SizedIngredient.of(basicFishFood, 1), 100, 0.2, "clay", false));
        infusingRecipes.add(new InfusingRecipeSingle("sand", inputList(SizedIngredient.of(Items.CLAY_BALL, 64)),100, 0.5, "clay", false));
        caviarSimpleRecipes.add(new CaviarSimple("clay", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.CLAY_BALL, 1), 0.4f)));

        //Snow
        breedingRecipes.add(new BreedingRecipe("water", "white", SizedIngredient.of(basicFishFood, 1), 100, 0.2, "snow", false));
        infusingRecipes.add(new InfusingRecipeSingle("water", inputList(SizedIngredient.of(Items.SNOWBALL, 64)),100, 0.5, "snow", false));
        caviarSimpleRecipes.add(new CaviarSimple("snow", false, new SizedIngredientChanceResult(SizedIngredient.of(Items.SNOWBALL, 1), 0.4f)));

        //Ice
        breedingRecipes.add(new BreedingRecipe("snow", "blue", SizedIngredient.of(basicFishFood, 1), 100, 0.2, "ice", false));
        infusingRecipes.add(new InfusingRecipeSingle("snow", inputList(SizedIngredient.of(Items.ICE, 64)),100, 0.5, "ice", false));
        caviarSimpleRecipes.add(new CaviarSimple("ice", false,
                new SizedIngredientChanceResult(SizedIngredient.of(Items.ICE, 1), 0.4f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.PACKED_ICE, 1), 0.1f),
                new SizedIngredientChanceResult(SizedIngredient.of(Items.BLUE_ICE, 1), 0.05f)));


        for (BreedingRecipe recipe : breedingRecipes) {
            var builder = FishBreedingRecipeBuilder.createFishBreedingRecipe(
                    createFish(recipe.parent1),
                    createFish(recipe.parent2),
                    recipe.catalyst,
                    recipe.time,
                    recipe.successChance,
                    createFish(recipe.child)
            )
            .unlockedBy("has_item", has(recipe.catalyst.ingredient().getItems()[0].getItem()));

            if (recipe.withCondition) {
                builder.save(consumer.withConditions((new NotCondition(new TagEmptyCondition(CommonTags.getTag(recipe.child, CommonTags.ResourceType.ORES))))));
            } else {
                builder.save(consumer);
            }
        }

        for (InfusingRecipeSingle recipe : infusingRecipes) {
            var builder = FishInfusingRecipeBuilder.createFishInfusingRecipe(
                    createFish(recipe.fishToCovert),
                    recipe.inputs,
                    recipe.time,
                    recipe.successChance,
                    createFish(recipe.result)
            )
            .unlockedBy("has_item", has(recipe.inputs.get(0).ingredient().getItems()[0].getItem()));

            if (recipe.withCondition) {
                builder.save(consumer.withConditions((new NotCondition(new TagEmptyCondition(CommonTags.getTag(recipe.result, CommonTags.ResourceType.ORES))))));
            } else {
                builder.save(consumer);
            }
        }

        for (CaviarSimple recipe : caviarSimpleRecipes) {
            var builder = CaviarProcessorRecipeBuilder.caviarProcessorRecipeBuilder(
                    CaviarItem.createCaviarStack(recipe.fish),
                    List.of(recipe.chanceResults));

            if (recipe.withCondition) {
                builder.save(consumer.withConditions((new NotCondition(new TagEmptyCondition(CommonTags.getTag(recipe.fish, CommonTags.ResourceType.ORES))))), recipe.fish);
            } else {
                builder.save(consumer, recipe.fish);
            }
        }


        // ***** Crafting Recipes *****
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishBlocks.AQUARIUM, 1)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', Tags.Items.GLASS_BLOCKS)
                .define('B', ResourceFishBlocks.TANK_CONTROLLER)
                .unlockedBy("has_item", has(ResourceFishBlocks.TANK_CONTROLLER))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishBlocks.BREEDING_TANK, 1)
                .pattern("ABA")
                .define('A', ResourceFishBlocks.AQUARIUM)
                .define('B', ResourceFishItems.BREEDING_UPGRADE)
                .unlockedBy("has_item", has(ResourceFishBlocks.AQUARIUM))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishBlocks.INFUSING_TANK, 1)
                .pattern("ABA")
                .define('A', ResourceFishBlocks.AQUARIUM)
                .define('B', ResourceFishItems.INFUSING_UPGRADE)
                .unlockedBy("has_item", has(ResourceFishBlocks.AQUARIUM))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.BASIC_FISH_FOOD, 6)
                .pattern(" A ")
                .pattern("A A")
                .pattern(" A ")
                .define('A', Items.KELP)
                .unlockedBy("has_item", has(Items.KELP))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.METALLIC_FISH_FOOD, 6)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', ResourceFishItems.BASIC_FISH_FOOD)
                .define('B', Tags.Items.INGOTS)
                .unlockedBy("has_item", has(ResourceFishItems.BASIC_FISH_FOOD))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.CRYSTAL_FISH_FOOD, 6)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', ResourceFishItems.METALLIC_FISH_FOOD)
                .define('B', Tags.Items.GEMS)
                .unlockedBy("has_item", has(ResourceFishItems.METALLIC_FISH_FOOD))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.BASIC_MOB_FISH_FOOD, 6)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', ResourceFishItems.METALLIC_FISH_FOOD)
                .define('B', Items.BONE_BLOCK)
                .unlockedBy("has_item", has(ResourceFishItems.METALLIC_FISH_FOOD))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.NETHER_FISH_FOOD, 6)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', ResourceFishItems.CRYSTAL_FISH_FOOD)
                .define('B', Items.GLOWSTONE)
                .unlockedBy("has_item", has(ResourceFishItems.CRYSTAL_FISH_FOOD))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.ENDER_FISH_FOOD, 6)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', ResourceFishItems.NETHER_FISH_FOOD)
                .define('B', Items.END_STONE)
                .unlockedBy("has_item", has(ResourceFishItems.NETHER_FISH_FOOD))
                .save(consumer);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishBlocks.TANK_CONTROLLER)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', Items.IRON_INGOT)
                .define('B', ResourceFishItems.RESOURCE_FISH_BUCKET)
                .unlockedBy("has_item", has(ResourceFishItems.RESOURCE_FISH_BUCKET))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishBlocks.CAVIAR_PROCESSOR)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', Items.IRON_INGOT)
                .define('B', CaviarItem.createCaviarStack("lava").getItem())
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.DEPTH_UPGRADE_1)
                .pattern("AAA")
                .pattern(" B ")
                .pattern("AAA")
                .define('A', Items.IRON_INGOT)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.DEPTH_UPGRADE_2)
                .pattern("AAA")
                .pattern(" B ")
                .pattern("AAA")
                .define('A', Items.GOLD_INGOT)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.DEPTH_UPGRADE_3)
                .pattern("AAA")
                .pattern(" B ")
                .pattern("AAA")
                .define('A', Items.DIAMOND)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.WIDTH_UPGRADE_1)
                .pattern("A A")
                .pattern("ABA")
                .pattern("A A")
                .define('A', Items.IRON_INGOT)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.WIDTH_UPGRADE_2)
                .pattern("A A")
                .pattern("ABA")
                .pattern("A A")
                .define('A', Items.GOLD_INGOT)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.WIDTH_UPGRADE_3)
                .pattern("A A")
                .pattern("ABA")
                .pattern("A A")
                .define('A', Items.DIAMOND)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.SPEED_UPGRADE_1)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', Items.SUGAR)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.SPEED_UPGRADE_2)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', Items.GLOWSTONE_DUST)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.SPEED_UPGRADE_3)
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', Items.END_ROD)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.TANK_UPGRADE)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', Tags.Items.GLASS_BLOCKS)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishItems.RESOURCE_FISH_BUCKET))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.BREEDING_UPGRADE)
                .pattern("AAA")
                .pattern("B B")
                .pattern("AAA")
                .define('A', Items.IRON_INGOT)
                .define('B', ResourceFishItems.RESOURCE_FISH_BUCKET)
                .unlockedBy("has_item", has(ResourceFishItems.RESOURCE_FISH_BUCKET))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.INFUSING_UPGRADE)
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', Items.BONE_MEAL)
                .define('B', ResourceFishItems.RESOURCE_FISH_BUCKET)
                .define('C', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishItems.INFUSING_UPGRADE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ResourceFishItems.ROUND_ROBIN_UPGRADE)
                .pattern("BAB")
                .pattern("ABA")
                .pattern("BAB")
                .define('A', Items.REDSTONE)
                .define('B', ResourceFishTags.Items.CAVIAR)
                .unlockedBy("has_item", has(ResourceFishTags.Items.CAVIAR))
                .save(consumer);
















    }

    public static NonNullList<SizedIngredient> inputList(SizedIngredient... stacks) {
        NonNullList<SizedIngredient> list = NonNullList.create();
        list.addAll(Arrays.asList(stacks));
        return list;
    }

    public static NonNullList<SizedIngredient> oreInputList(SizedIngredient resource) {
        NonNullList<SizedIngredient> list = NonNullList.create();
        list.add(resource);
        list.add(new SizedIngredient(Ingredient.of(Items.STONE), resource.count()));
        list.add(resource);
        return list;
    }

    public static ResourceLocation createFish(String resource) {
        return ResourceLocation.fromNamespaceAndPath(ResourceFish.MOD_ID, resource);
    }

}
