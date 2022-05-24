package com.cyborgmas.mobstatues.util;

import com.cyborgmas.mobstatues.MobStatues;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StatueCreationHelper {
    private static final List<EntityType<?>> DYNAMIC_ENTITIES = Util.make(() -> ImmutableList.<EntityType<?>>builder()
            .add(EntityType.MAGMA_CUBE)
            .add(EntityType.SHEEP)
            .add(EntityType.HORSE)
            .add(EntityType.AXOLOTL)
            .add(EntityType.VILLAGER)
            .add(EntityType.LLAMA)
            .build()
    );

    private static final Map<EntityType<?>, VoxelShape> STATIC_SHAPE = new HashMap<>();
    private static final Map<EntityType<?>, EntityDimensions> STATIC_SIZE = new HashMap<>();
    private static final Map<EntityType<?>, Entity> STATIC_ENTITY_MODEL = new HashMap<>();

    @Nullable
    public static Entity getEntity(CompoundTag nbt, Level world) {
        return getEntity(nbt, world, false);
    }

    @Nullable
    public static Entity getEntity(CompoundTag nbt, Level world, boolean noCache) {
        return mapEither(type -> {
                    if (noCache)
                        return type.create(world);
                    return STATIC_ENTITY_MODEL.computeIfAbsent(type, t -> type.create(world));
                },
                Function.identity(),
                getEntityOrType(nbt, world)
        );
    }

    @Nullable
    public static EntityDimensions getEntitySize(CompoundTag nbt, Level world) {
        return mapEither(
                type -> STATIC_SIZE.computeIfAbsent(type, EntityType::getDimensions),
                e -> e.getDimensions(Pose.STANDING),
                getEntityOrType(nbt, world)
        );
    }

    @Nullable
    public static VoxelShape getShape(CompoundTag nbt, Level world) {
        return mapEither(
                type -> STATIC_SHAPE.computeIfAbsent(type, t ->
                        Shapes.create(t.getAABB(0, 0,0))),
                e -> Shapes.create(e.getBoundingBox()),
                getEntityOrType(nbt, world)
        );
    }

    private static Either<EntityType<?>, Entity> getEntityOrType(CompoundTag nbt, Level world) {
        EntityType<? extends Entity> type = EntityType.by(nbt).orElse(EntityType.PIG);
        if (!DYNAMIC_ENTITIES.contains(type)) //TODO proper data-driven check?
            return Either.left(type);

        Entity e = createEntityAndRead(type, nbt, world);
        handleEntity(e, nbt);
        return e == null ? Either.left(EntityType.PIG) : Either.right(e);
    }

    //Hard coded updates
    private static void handleEntity(Entity entity, CompoundTag tag) {
        // For llama the carpet is only synced from the server entity, but I only create client entities.
        if (entity instanceof Llama llama) {
            if (tag.contains("DecorColor"))
                llama.setSwag(DyeColor.byId(tag.getInt("DecorColor")));
        }
    }

    private static Entity createEntityAndRead(EntityType<?> type, CompoundTag nbt, Level world) {
        Entity entity =  type.create(world);

        if (entity != null) {
            try {
                entity.load(nbt);
            } catch (Exception e) {
                MobStatues.LOGGER.warn("Could not read nbt for entity of type {}, reading should never make assumptions about the tag.", entity.getType().getRegistryName(), e);
            }
        }
        return entity;
    }

    private static <A, L, R> A mapEither(Function<L, A> l, Function<R, A> r, Either<L, R> either) {
        return either.left().map(l).orElse(either.right().map(r).orElse(null));
    }
}
