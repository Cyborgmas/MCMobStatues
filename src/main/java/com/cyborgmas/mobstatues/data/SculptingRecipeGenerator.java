package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipe;
import com.cyborgmas.mobstatues.registration.Registration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class SculptingRecipeGenerator extends RecipeProvider {
    public SculptingRecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> f) {
        SculptingRecipe.Ingredients ingredients = new SculptingRecipe.Ingredients(
                ImmutableList.of("# ", "T "),
                ImmutableMap.of("#", Ingredient.of(COBBLESTONE), "T", Ingredient.of(STONE)),
                Ingredient.of(GREEN_DYE),
                Ingredient.EMPTY,
                Util.make(() -> {
                    ItemStack ret = new ItemStack(Registration.STATUE_ITEM.get());
                    CompoundTag entityData = ret.getOrCreateTag();
                    entityData.putString("id", "zombie");
                    return ret;
                })
        );
        f.accept(new Result(new SculptingRecipe(MobStatues.getId("zombie_statue_sculpting"), ingredients)));
    }

    public record Result(SculptingRecipe recipe) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject ret) {
            JsonElement element = SculptingRecipe.Ingredients.CODEC.encodeStart(JsonOps.INSTANCE, recipe.ingredients).getOrThrow(false, s -> {
            });
            if (!(element instanceof JsonObject object)) {
                throw new RuntimeException();
            }
            object.entrySet().forEach(e -> ret.add(e.getKey(), e.getValue()));
        }

        @Override
        public ResourceLocation getId() {
            return recipe.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return Registration.SCULPTING_RECIPE_SERIALIZER.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
