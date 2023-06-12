package com.cyborgmas.mobstatues.util;

import com.cyborgmas.mobstatues.MobStatues;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RenderingExceptionHandler {
    private static final Map<String, Set<EntityType<?>>> CONTEXT_TO_ERRORS = new HashMap<>();

    public static void handle(String context, EntityType<?> entity, Exception e) {
        Set<EntityType<?>> errors = CONTEXT_TO_ERRORS.computeIfAbsent(context, c -> new HashSet<>());
        if (!errors.contains(entity)) {
            errors.add(entity);
            MobStatues.LOGGER.warn("Could not render entity of type {} in context [{}]", ForgeRegistries.ENTITY_TYPES.getKey(entity), context, e);
        }
    }
}
