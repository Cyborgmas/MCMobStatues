package com.cyborgmas.mobstatues.util;

import com.cyborgmas.mobstatues.MobStatues;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

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
    private static final Map<EntityType<?>, EntitySize> STATIC_SIZE = new HashMap<>();
    private static final Map<EntityType<?>, Entity> STATIC_ENTITY_MODEL = new HashMap<>();

    @Nullable
    public static Entity getEntity(CompoundNBT nbt, World world) {
        return getEntity(nbt, world, false);
    }

    @Nullable
    public static Entity getEntity(CompoundNBT nbt, World world, boolean noCache) {
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
    public static EntitySize getEntitySize(CompoundNBT nbt, World world) {
        return mapEither(
                type -> STATIC_SIZE.computeIfAbsent(type, EntityType::getDimensions),
                e -> e.getDimensions(Pose.STANDING),
                getEntityOrType(nbt, world)
        );
    }

    @Nullable
    public static VoxelShape getShape(CompoundNBT nbt, World world) {
        return mapEither(
                type -> STATIC_SHAPE.computeIfAbsent(type, t ->
                        VoxelShapes.create(t.getAABB(0, 0,0))),
                e -> VoxelShapes.create(e.getBoundingBox()),
                getEntityOrType(nbt, world)
        );
    }

    private static Either<EntityType<?>, Entity> getEntityOrType(CompoundNBT nbt, World world) {
        EntityType<? extends Entity> type = EntityType.by(nbt).orElse(EntityType.PIG);

        Entity e = createEntityAndRead(type, nbt, world);
        if (e == null)
            return Either.left(EntityType.PIG);

        return DYNAMIC_SIZED_ENTITIES.contains(type) ? Either.right(e) : Either.left(type);
    }

    private static Entity createEntityAndRead(EntityType<?> type, CompoundNBT nbt, World world) {
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
