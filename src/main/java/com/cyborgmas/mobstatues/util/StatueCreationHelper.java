package com.cyborgmas.mobstatues.util;

import com.cyborgmas.mobstatues.MobStatues;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.Util;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StatueCreationHelper {
    private static final List<EntityType<?>> DYNAMIC_SIZED_ENTITIES = Util.make(() -> ImmutableList.<EntityType<?>>builder()
            .add(EntityType.MAGMA_CUBE)
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

        Entity e = createEntityAndRead(type, nbt, world);
        if (e == null)
            return Either.left(EntityType.PIG);

        return DYNAMIC_SIZED_ENTITIES.contains(type) ? Either.right(e) : Either.left(type);
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
