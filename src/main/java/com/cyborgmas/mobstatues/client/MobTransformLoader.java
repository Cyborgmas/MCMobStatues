package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
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
public class MobTransformLoader extends JsonReloadListener {
    public final static String FOLDER = "statue_transforms";
    private final static Gson GSON = new GsonBuilder().registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer()).registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer()).setPrettyPrinting().create();

    public static MobTransformLoader instance;

    private final Map<ResourceLocation, ItemCameraTransforms> statueTransforms = new HashMap<>();

    public MobTransformLoader() {
        super(GSON, FOLDER);
        instance = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, IResourceManager resourceManager, IProfiler profiler) {
        statueTransforms.clear();

        jsonMap.entrySet().stream()
                .filter(e -> ForgeRegistries.ENTITIES.containsKey(e.getKey()))
                .forEach(e -> statueTransforms.put(e.getKey(), GSON.fromJson(e.getValue(), ItemCameraTransforms.class)));
    }

    public static void applyEntitySpecificTransform(EntityType<?> entity, ItemCameraTransforms.TransformType type, MatrixStack stack) {
        if (instance.statueTransforms.containsKey(entity.getRegistryName())) {
            ItemCameraTransforms transforms = instance.statueTransforms.get(entity.getRegistryName());
            if (!transforms.getTransform(type).equals(ItemTransformVec3f.NO_TRANSFORM)) {
                TransformationHelper.toTransformation(transforms.getTransform(type)).push(stack);
            }
        }
    }
}
