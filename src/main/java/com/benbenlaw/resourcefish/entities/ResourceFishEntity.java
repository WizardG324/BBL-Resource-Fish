package com.benbenlaw.resourcefish.entities;

import com.benbenlaw.resourcefish.config.ResourceFishConfig;
import com.benbenlaw.resourcefish.block.ResourceFishBlocks;
import com.benbenlaw.resourcefish.block.entity.TankControllerBlockEntity;
import com.benbenlaw.resourcefish.item.ResourceFishDataComponents;
import com.benbenlaw.resourcefish.item.ResourceFishItems;
import com.benbenlaw.resourcefish.util.ResourceType;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ResourceFishEntity extends AbstractSchoolingFish  {

    private static final EntityDataAccessor<String> DATA_RESOURCE_TYPE = SynchedEntityData.defineId(ResourceFishEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_PATTERN = SynchedEntityData.defineId(ResourceFishEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODEL = SynchedEntityData.defineId(ResourceFishEntity.class, EntityDataSerializers.INT);

    private int dropTimer = 0;
    private int ticksPerDrop = Integer.MAX_VALUE;
    private boolean allowedToDrop = false;
    private BlockPos tankHome = null;

    private final Level level;

    public ResourceFishEntity(EntityType<? extends AbstractSchoolingFish> type, Level level) {
        super(type, level);
        this.level = level;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level.isClientSide()) {

            if (tankHome == null) {
                this.allowedToDrop = false;
                return;
            }

            if (!level.getBlockState(tankHome).is(ResourceFishBlocks.TANK_CONTROLLER.get())) {
                this.allowedToDrop = false;
                tankHome = null;
                return;
            }

            if (!this.allowedToDrop) return;
            dropTimer++;

            ResourceType resourceType = getResourceType();
            ticksPerDrop = resourceType.getDropIntervalTicks();

            if (dropTimer >= ticksPerDrop) {
                dropTimer = 0;

                List<ItemStack> dropStack = resourceType.rollResults(random);

                for (ItemStack stack : dropStack) {
                    if (!stack.isEmpty()) {

                        BlockEntity be = level.getBlockEntity(tankHome);
                        if (be instanceof TankControllerBlockEntity tankControllerBlockEntity) {

                            ItemStackHandler handler = tankControllerBlockEntity.getItemStackHandler();
                            int[] slots = new int[handler.getSlots()];
                            for (int i = 0; i < 12; i++) {
                                slots[i] = i;
                            }

                            tankControllerBlockEntity.insertStackIntoSlots(handler, stack, slots);
                        }


                        //this.spawnAtLocation(stack);
                    }
                }
            }
        }
    }

    public boolean isAllowedToDrop() {
        return allowedToDrop;
    }

    public void setAllowedToDrop(boolean allowedToDrop) {
        this.allowedToDrop = allowedToDrop;
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(ResourceFishItems.RESOURCE_FISH_BUCKET.get());
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    public record Variant(ResourceType type, Pattern pattern, ResourceFishEntity.Pattern.Base model) {

        public String getPatternTextureName() {
            return pattern.name().toLowerCase(); // e.g., "small_pattern_1"
        }

        public ResourceFishEntity.Pattern.Base getModelBase() {
            return model;
        }
    }

    public static Variant generateVariant(ResourceType type, RandomSource random) {

        List<ResourceFishEntity.Pattern.Base> models = type.getModels();
        ResourceFishEntity.Pattern.Base chosenModel = models.isEmpty()
                ? ResourceFishEntity.Pattern.Base.SMALL
                : models.get(random.nextInt(models.size()));

        List<ResourceFishEntity.Pattern> patterns = type.getPatterns().stream()
                .filter(p -> p.getBase() == chosenModel).toList();

        ResourceFishEntity.Pattern chosenPattern = patterns.isEmpty()
                ? ResourceFishEntity.Pattern.SMALL_0
                : patterns.get(random.nextInt(patterns.size()));


        return new Variant(type, chosenPattern, chosenModel);
    }

    public enum Pattern {
        SMALL_0(0, Base.SMALL),
        SMALL_1(0, Base.SMALL),
        SMALL_2(1, Base.SMALL),
        SMALL_3(2, Base.SMALL),
        SMALL_4(3, Base.SMALL),
        SMALL_5(4, Base.SMALL),
        SMALL_6(5, Base.SMALL),
        LARGE_0(6, Base.LARGE),
        LARGE_1(6, Base.LARGE),
        LARGE_2(7, Base.LARGE),
        LARGE_3(8, Base.LARGE),
        LARGE_4(9, Base.LARGE),
        LARGE_5(10, Base.LARGE),
        LARGE_6(11, Base.LARGE);

        public enum Base {
            SMALL, LARGE;
            public static final Codec<Base> CODEC = Codec.STRING.xmap(s -> Base.valueOf(s.toUpperCase()), Base::name);

            public static final StreamCodec<RegistryFriendlyByteBuf, Base> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, Base::name,
                    Base::valueOf
            );

        }

        private final int id;
        private final Base size;

        Pattern(int id, Base size) {
            this.id = id;
            this.size = size;
        }

        public int getId() {
            return id;
        }

        public Base getBase() {
            return size;
        }

        public static Pattern byId(int id) {
            for (Pattern p : values()) {
                if (p.getId() == id) return p;
            }
            return SMALL_1; // default fallback
        }

        public static final Codec<Pattern> CODEC = Codec.INT.xmap(Pattern::byId, Pattern::getId);

        public static final StreamCodec<RegistryFriendlyByteBuf, Pattern> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, Pattern::getId,
                Pattern::byId
        );
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RESOURCE_TYPE, ResourceType.NONE.getId().toString());
        builder.define(DATA_PATTERN, Pattern.SMALL_1.ordinal());
        builder.define(DATA_MODEL, Pattern.Base.SMALL.ordinal());
    }

    public Variant getVariant() {
        // Safe parse, fallback to NONE
        ResourceType resourceType = ResourceType.REGISTRY.getOrDefault(
                ResourceLocation.tryParse(this.entityData.get(DATA_RESOURCE_TYPE)),
                ResourceType.NONE
        );

        Pattern pattern = Pattern.values()[this.entityData.get(DATA_PATTERN)];
        Pattern.Base model = Pattern.Base.values()[this.entityData.get(DATA_MODEL)];

        return new Variant(resourceType, pattern, model);
    }

    public void setVariant(Variant variant) {
        this.entityData.set(DATA_RESOURCE_TYPE, variant.type().getId().toString());
        this.entityData.set(DATA_PATTERN, variant.pattern().ordinal());
        this.entityData.set(DATA_MODEL, variant.model().ordinal());
    }

    public ResourceType getResourceType() {
        String idString = this.entityData.get(DATA_RESOURCE_TYPE);
        if (idString == null || idString.isEmpty()) {
            idString = ResourceType.NONE.getId().toString();
        }
        ResourceLocation id = ResourceLocation.parse(idString);
        ResourceType resourceType = ResourceType.REGISTRY.get(id);
        if (resourceType == null) {
            resourceType = ResourceType.NONE;
        }
        return resourceType;
    }

    public void setResourceType(ResourceType type) {
        this.entityData.set(DATA_RESOURCE_TYPE, type.getId().toString());
    }

    public int getResourceColor() {
        return getResourceType().getColor();
    }

    public void setTankHome(BlockPos pos) {
        this.tankHome = pos;
    }
    public BlockPos getTankHome() {
        return tankHome;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ResourceType", getResourceType().getId().toString());
        tag.putInt("Pattern", this.entityData.get(DATA_PATTERN));
        tag.putInt("Model", this.entityData.get(DATA_MODEL));
        tag.putInt("dropTimer", dropTimer);
        tag.putInt("ticksPerDrop", ticksPerDrop);
        tag.putBoolean("allowedToDrop", allowedToDrop);

        if (tankHome != null) {
            tag.put("tankHome", NbtUtils.writeBlockPos(tankHome));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ResourceType")) {
            this.entityData.set(DATA_RESOURCE_TYPE, tag.getString("ResourceType"));
        }
        if (tag.contains("Pattern")) {
            this.entityData.set(DATA_PATTERN, tag.getInt("Pattern"));
        }
        if (tag.contains("Model")) {
            this.entityData.set(DATA_MODEL, tag.getInt("Model"));
        }
        dropTimer = tag.getInt("dropTimer");
        ticksPerDrop = tag.getInt("ticksPerDrop");
        allowedToDrop = tag.getBoolean("allowedToDrop");

        if (NbtUtils.readBlockPos(tag, "tankHome").isPresent()) {
            tankHome =  NbtUtils.readBlockPos(tag, "tankHome").get();
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {

        if (spawnType == MobSpawnType.BUCKET) {
            return super.finalizeSpawn(level, difficulty, spawnType, groupData);
        }

        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, groupData);

        Holder<Biome> biomeHolder = level.getBiome(this.blockPosition());
        ResourceType type = generateRandomResource(level.getRandom(), biomeHolder);
        this.setResourceType(type);
        this.setVariant(generateVariant(type, level.getRandom()));
        this.allowedToDrop = false;

        if (type == ResourceType.NONE) {
            System.out.println("No naturally spawning ResourceType found for biome " + Objects.requireNonNull(biomeHolder.getKey()).location() + ". Removing entity.");
            this.discard();
            return null;
        }


        return data;
    }

    private boolean matchesBiomeOrTag(Holder<Biome> biomeHolder, String biomeOrTag) {
        if (biomeOrTag.startsWith("#")) {
            // It's a tag, strip leading '#' and check
            String tagLocation = biomeOrTag.substring(1);
            TagKey<Biome> biomeTagKey = TagKey.create(Registries.BIOME, ResourceLocation.parse(tagLocation));
            return biomeHolder.is(biomeTagKey);
        } else {
            ResourceLocation biomeId = Objects.requireNonNull(biomeHolder.getKey()).location();
            return biomeId.toString().equals(biomeOrTag);
        }
    }

    private ResourceType generateRandomResource(RandomSource random, Holder<Biome> biomeHolder) {
        List<ResourceType> values = ResourceType.REGISTRY.values().stream()
                .filter(type -> !type.getBiomes().isEmpty() &&
                        type.getBiomes().stream().anyMatch(biomeOrTag -> matchesBiomeOrTag(biomeHolder, biomeOrTag))
                )
                .toList();

        if (values.isEmpty()) {
            return ResourceType.NONE; // fallback default
        }
        return values.get(random.nextInt(values.size()));
    }



    @Override
    public boolean checkSpawnRules(LevelAccessor levelAccessor, MobSpawnType spawnType) {

        if (spawnType == MobSpawnType.BUCKET) {
            return true;
        }


        BlockPos pos = this.blockPosition();
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    public static <T extends ResourceFishEntity> boolean canSpawnHere(
            EntityType<T> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {

        if (spawnType == MobSpawnType.BUCKET) {
            return true;
        }

        if ((spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION)
                && !ResourceFishConfig.NATURAL_SPAWNING.get())
            return false;

        if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;

        Holder<Biome> biomeHolder = level.getBiome(pos);

        // Find any ResourceType that can spawn here
        boolean validBiome = ResourceType.REGISTRY.values().stream()
                .anyMatch(rt -> rt.getBiomes().stream().anyMatch(biomeOrTag -> {
                    if (biomeOrTag.startsWith("#")) {
                        TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, ResourceLocation.tryParse(biomeOrTag.substring(1)));
                        return biomeHolder.is(tagKey);
                    } else {
                        ResourceLocation biomeId = Objects.requireNonNull(biomeHolder.getKey()).location();
                        return biomeId.toString().equals(biomeOrTag);
                    }
                }));

        if (validBiome) {
            //System.out.println("Spawning ResourceFishEntity at " + pos + " in biome " + Objects.requireNonNull(biomeHolder.getKey()).location());
        }
        return validBiome;
    }



    @Override
    public void loadFromBucketTag(CompoundTag tag) {
        super.loadFromBucketTag(tag);
        if (tag.contains("BucketResourceType", Tag.TAG_STRING)) {
            this.entityData.set(DATA_RESOURCE_TYPE, tag.getString("BucketResourceType"));
        }
        if (tag.contains("BucketPattern", Tag.TAG_INT)) {
            this.entityData.set(DATA_PATTERN, tag.getInt("BucketPattern"));
        }
        if (tag.contains("BucketModel", Tag.TAG_INT)) {
            this.entityData.set(DATA_MODEL, tag.getInt("BucketModel"));
        }
        if (tag.contains("allowedToDrop", Tag.TAG_BYTE)) {
            this.allowedToDrop = tag.getBoolean("allowedToDrop");
        } else {
            this.allowedToDrop = false; // Default to false if not specified
        }
    }

    @Override
    public void saveToBucketTag(ItemStack bucket) {
        super.saveToBucketTag(bucket);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, bucket, data -> {
            data.putString("BucketResourceType", this.getResourceType().getId().toString());
            data.putInt("BucketPattern", this.entityData.get(DATA_PATTERN));
            data.putInt("BucketModel", this.entityData.get(DATA_MODEL));
            data.putBoolean("allowedToDrop", allowedToDrop);
        });

        bucket.set(ResourceFishDataComponents.FISH_TYPE.get(), this.getResourceType().getId());
    }

    @Override
    public Component getName() {
        return this.getDisplayName();
    }

    @Override
    public Component getDisplayName() {

        if (this.hasCustomName()) {
            return Objects.requireNonNull(this.getCustomName());
        } else {
            ResourceType resourceType = getResourceType();
            String baseName = "entity.resourcefish.resource_fish";
            if (resourceType != null && resourceType != ResourceType.NONE) {

                String path = resourceType.getId().getPath();
                String capitalizedPath = path.substring(0, 1).toUpperCase(Locale.ROOT) + path.substring(1).toLowerCase(Locale.ROOT);

                return Component.translatable(baseName, capitalizedPath);
            }
        }
        return Component.literal("This should not happen check logs its probably a missing resource type for the biome!");
    }

    @Override
    public boolean removeWhenFarAway(double p_27492_) {
        return false;
    }
}