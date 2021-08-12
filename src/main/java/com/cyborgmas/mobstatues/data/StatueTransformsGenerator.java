package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.client.MobTransformLoader;
import com.google.gson.*;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.*;

import static net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.*;
import static net.minecraft.client.renderer.model.ItemTransformVec3f.NO_TRANSFORM;

public class StatueTransformsGenerator implements IDataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;
    private final Map<ResourceLocation, ItemCameraTransforms> toSerialize = new HashMap<>();

    public StatueTransformsGenerator(DataGenerator generator) {
        this.generator = generator;
    }

    public TransformsBuilder makeTransformsFor(ResourceLocation entity) {
        return new TransformsBuilder(entity);
    }

    @Override
    public void run(DirectoryCache cache) {
        toSerialize.forEach(LamdbaExceptionUtils.rethrowBiConsumer((rl, ict) -> {
            String path = "assets/" + rl.getNamespace() + "/" + MobTransformLoader.FOLDER + "/" + rl.getPath() + ".json" ;
            IDataProvider.save(GSON, cache , serialize(ict), generator.getOutputFolder().resolve(path));
        }));
    }

    private JsonElement serialize(ItemCameraTransforms transforms) {
        JsonObject ret = new JsonObject();
        Arrays.stream(values()).forEach(t -> {
            JsonObject tObj = new JsonObject();
            ItemTransformVec3f vec = transforms.getTransform(t);
            if (vec == NO_TRANSFORM)
                return;
            if (!vec.rotation.equals(NO_TRANSFORM.rotation))
                putVec(tObj, vec.rotation, "rotation");
            if (!vec.translation.equals(NO_TRANSFORM.scale))
                putVec(tObj, vec.translation, "translation");
            if (!vec.scale.equals(NO_TRANSFORM.scale))
                putVec(tObj, vec.scale, "scale");
            ret.add(getTransformTypeName(t), tObj);
        });
        return ret;
    }

    private void putVec(JsonObject obj, Vector3f vec, String name) {
        JsonArray array = new JsonArray();
        array.add(vec.x());
        array.add(vec.y());
        array.add(vec.z());
        obj.add(name, array);
    }

    //Names gotten from ItemCameraTransforms.Deserializer
    private static String getTransformTypeName(TransformType type) {
        switch (type) {
            case THIRD_PERSON_RIGHT_HAND:
                return "thirdperson_righthand";
            case THIRD_PERSON_LEFT_HAND:
                return "thirdperson_lefthand";
            case FIRST_PERSON_RIGHT_HAND:
                return "firstperson_righthand";
            case FIRST_PERSON_LEFT_HAND:
                return "firstperson_lefthand";
            default:
                return type.name().toLowerCase(Locale.ENGLISH);
        }
    }

    @Override
    public String getName() {
        return "Statue Transforms Generator";
    }

    public class TransformsBuilder {
        private final Map<TransformType, ItemTransformVec3f> transforms = new HashMap<>();
        private final ResourceLocation entity;

        public TransformsBuilder(ResourceLocation entity) {
            this.entity = entity;
        }

        private TransformsBuilder add(TransformType type, VecTransformBuilder builder) {
            transforms.put(type, new ItemTransformVec3f(builder.rotation, builder.translation, builder.scale));
            return this;
        }

        public VecTransformBuilder transform() {
            return new VecTransformBuilder();
        }

        public StatueTransformsGenerator endTransforms() {
            ItemCameraTransforms transforms = new ItemCameraTransforms(
                    this.transforms.getOrDefault(THIRD_PERSON_LEFT_HAND, NO_TRANSFORM),
                    this.transforms.getOrDefault(THIRD_PERSON_RIGHT_HAND, NO_TRANSFORM),
                    this.transforms.getOrDefault(FIRST_PERSON_LEFT_HAND, NO_TRANSFORM),
                    this.transforms.getOrDefault(FIRST_PERSON_RIGHT_HAND, NO_TRANSFORM),
                    this.transforms.getOrDefault(HEAD, NO_TRANSFORM),
                    this.transforms.getOrDefault(GUI, NO_TRANSFORM),
                    this.transforms.getOrDefault(GROUND, NO_TRANSFORM),
                    this.transforms.getOrDefault(FIXED, NO_TRANSFORM)
            );
            StatueTransformsGenerator.this.toSerialize.put(this.entity, transforms);
            return StatueTransformsGenerator.this;
        }

        public class VecTransformBuilder {
            private Vector3f translation = new Vector3f();
            private Vector3f rotation = new Vector3f();
            private Vector3f scale = new Vector3f(1, 1, 1);

            public VecTransformBuilder addTranslation(int x, int y, int z) {
                translation = new Vector3f(x, y, z);
                return this;
            }

            public VecTransformBuilder addTranslation(int p) {
                translation = new Vector3f(p, p, p);
                return this;
            }

            public VecTransformBuilder addScale(int x, int y, int z) {
                scale = new Vector3f(x, y, z);
                return this;
            }

            public VecTransformBuilder addScale(int s) {
                scale = new Vector3f(s, s, s);
                return this;
            }

            public VecTransformBuilder addRotation(int x, int y, int z) {
                rotation = new Vector3f(x, y, z);
                return this;
            }

            public VecTransformBuilder addRotation(int s) {
                rotation = new Vector3f(s, s, s);
                return this;
            }

            public TransformsBuilder end(TransformType type) {
                return TransformsBuilder.this.add(type, this);
            }
        }
    }
}