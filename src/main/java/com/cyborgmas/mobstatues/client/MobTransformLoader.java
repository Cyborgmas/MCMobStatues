package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.model.TransformationHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(modid = MobStatues.MODID, value = Dist.CLIENT)
public class MobTransformLoader extends SimpleJsonResourceReloadListener {
    public final static String FOLDER = "statue_transforms";
    private final static Gson GSON = new GsonBuilder().registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer()).registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer()).setPrettyPrinting().create();

    public static MobTransformLoader instance;

    private final Map<ResourceLocation, ItemTransforms> statueTransforms = new HashMap<>();

    public MobTransformLoader() {
        super(GSON, FOLDER);
        instance = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        statueTransforms.clear();

        jsonMap.entrySet().stream()
                .filter(e -> ForgeRegistries.ENTITIES.containsKey(e.getKey()))
                .forEach(e -> statueTransforms.put(e.getKey(), GSON.fromJson(e.getValue(), ItemTransforms.class)));
    }

    public static void applyEntitySpecificTransform(EntityType<?> entity, ItemTransforms.TransformType type, PoseStack stack) {
        if (instance.statueTransforms.containsKey(entity.getRegistryName())) {
            ItemTransforms transforms = instance.statueTransforms.get(entity.getRegistryName());
            if (!transforms.getTransform(type).equals(ItemTransform.NO_TRANSFORM)) {
                TransformationHelper.toTransformation(transforms.getTransform(type)).push(stack);
            }
        }
    }
}
