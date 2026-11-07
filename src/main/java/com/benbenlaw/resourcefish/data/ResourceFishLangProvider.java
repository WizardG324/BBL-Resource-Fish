package com.benbenlaw.resourcefish.data;

import com.benbenlaw.resourcefish.ResourceFish;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ResourceFishLangProvider extends LanguageProvider {

    public ResourceFishLangProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ResourceFish.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        //Creative Tab
        add("itemGroup.resourcefish", "Resource Fish");

        //JEI
        addJEITranslation("spawn_biome", "Spawn Biome(s):");
        addJEITranslation("chance", "Chance: ");
        addJEITranslation("fish_in_tank_area", "Fish must be in tank area!");
        addJEITranslation("resource_fish", "Resource Fish");
        addJEITranslation("duration", "Duration: %s");
        addJEITranslation("fluid_amount", "Amount: ");
        addJEITranslation("needs_tank_upgrade", "Caviar Processor needs a Tank Upgrade to produce fluids!");

        //Items
        addItemTranslation("caviar", "Caviar");
        addItemTranslation("resource_fish_spawn_egg", "%s Resource Fish Spawn Egg");
        addItemTranslation("resource_fish_bucket", "%s Resource Fish Bucket");
        addItemTranslation("depth_upgrade_1", "Depth Upgrade 1");
        addItemTranslation("depth_upgrade_2", "Depth Upgrade 2");
        addItemTranslation("depth_upgrade_3", "Depth Upgrade 3");
        addItemTranslation("width_upgrade_1", "Width Upgrade 1");
        addItemTranslation("width_upgrade_2", "Width Upgrade 2");
        addItemTranslation("width_upgrade_3", "Width Upgrade 3");
        addItemTranslation("speed_upgrade_1", "Speed Upgrade 1");
        addItemTranslation("speed_upgrade_2", "Speed Upgrade 2");
        addItemTranslation("speed_upgrade_3", "Speed Upgrade 3");
        addItemTranslation("tank_upgrade", "Tank Upgrade");
        addItemTranslation("breeding_upgrade", "Breeding Upgrade");
        addItemTranslation("infusing_upgrade", "Infusing Upgrade");
        addItemTranslation("round_robin_upgrade", "Round Robin Upgrade");
        addItemTranslation("basic_fish_food", "Basic Fish Food");
        addItemTranslation("metallic_fish_food", "Metallic Fish Food");
        addItemTranslation("crystal_fish_food", "Crystal Fish Food");
        addItemTranslation("nether_fish_food", "Nether Fish Food");
        addItemTranslation("basic_mob_fish_food", "Basic Mob Fish Food");
        addItemTranslation("ender_fish_food", "Ender Fish Food");

        //Blocks
        addBlockTranslation("tank_controller", "Resource Fish Tank");
        addBlockTranslation("caviar_processor", "Caviar Processor");
        addBlockTranslation("tank_controller_range", "Tank Range");
        addBlockTranslation("nest", "Resource Fish Nest");
        addBlockTranslation("aquarium", "Aquarium");
        addBlockTranslation("breeding_tank", "Fish Breeding Tank");
        addBlockTranslation("infusing_tank", "Fish Infusing Tank");

        //Recipes
        addRecipeTranslation("caviar_processor", "Caviar Processor");
        addRecipeTranslation("fish_breeding", "Fish Breeding");
        addRecipeTranslation("fish_infusing", "Fish Infusing");
        addRecipeTranslation("fish_drops", "Fish Drops");

        //Tooltips
        addTooltipTranslation("depth_upgrade_1", "Increases tank depth by 1 (above and below)");
        addTooltipTranslation("depth_upgrade_2", "Increases tank depth by 2 (above and below)");
        addTooltipTranslation("depth_upgrade_3", "Increases tank depth by 3 (above and below)");
        addTooltipTranslation("width_upgrade_1", "Increases tank width by 1 (all sides)");
        addTooltipTranslation("width_upgrade_2", "Increases tank width by 2 (all sides)");
        addTooltipTranslation("width_upgrade_3", "Increases tank width by 3 (all sides)");
        addTooltipTranslation("speed_upgrade_1", "Reduces time to process in the Caviar Processor by 20 ticks");
        addTooltipTranslation("speed_upgrade_2", "Reduces time to process in the Caviar Processor by 40 ticks");
        addTooltipTranslation("speed_upgrade_3", "Reduces time to process in the Caviar Processor by 60 ticks");
        addTooltipTranslation("tank_upgrade", "Allows the Caviar Processor to produce fluids");
        addTooltipTranslation("breeding_upgrade", "Allows the Tank to breed fish. When installed fish will not produce caviar");
        addTooltipTranslation("infusing_upgrade", "Allows the Tank to infuse fish. When installed fish will not produce caviar");
        addTooltipTranslation("round_robin_upgrade", "Items inserted into the Caviar Processor will be distributed evenly across all input slots");
        addTooltipTranslation("aquarium", "Use a Bucket with a Resource Fish inside to store and produce Caviar");
        addTooltipTranslation("breeding_tank", "Holds 2 Resource Fish to breed. Right click with breeding food to feed, and with a bucket to take out a bred fish(or parents). Empty hand collects bones.");
        addTooltipTranslation("infusing_tank", "Holds 1 Resource Fish to infuse. Right click with the infusing items to input them, and with a bucket to take the fish out. Empty hand collects bones, sneak to take back the items.");

        //Jade
        add("jade.resourcefish.fish", "Fish: %s");
        add("jade.resourcefish.parents", "Parents: %s");
        add("jade.resourcefish.bred", "Bred: %s");
        add("config.jade.plugin_resourcefish.fish_tanks", "Fish Tank Contents");

        //Entity
        add("entity.resourcefish.resource_fish", "%s Resource Fish");

        //Screen
        add("screen.resourcefish.tank_controller.overfilled", "Tank is overfilled! Drops Disabled.");
        add("screen.resourcefish.tank_controller.fish_types", "Fish Types in Tank:");

    }

    private void addItemTranslation(String name, String translation) {
        add("item." + ResourceFish.MOD_ID + "." + name, translation);
    }
    private void addBlockTranslation(String name, String translation) {
        add("block." + ResourceFish.MOD_ID + "." + name, translation);
    }
    private void addJEITranslation(String name, String translation) {
        add("jei." + ResourceFish.MOD_ID + "." + name, translation);
    }
    private void addRecipeTranslation(String name, String translation) {
        add("recipe." + ResourceFish.MOD_ID + "." + name, translation);
    }
    private void addTooltipTranslation(String name, String translation) {
        add("tooltip." + ResourceFish.MOD_ID + "." + name, translation);
    }


}