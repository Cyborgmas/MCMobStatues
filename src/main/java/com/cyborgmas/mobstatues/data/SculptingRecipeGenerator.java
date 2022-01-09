package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipe;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipe.Ingredients;
import com.cyborgmas.mobstatues.registration.Registration;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class SculptingRecipeGenerator extends RecipeProvider {
    public SculptingRecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> c) {
        make(c,"zombie_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "zombie")))
                .row("#").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GREEN))
                .group("zombie_statue")
                .build());
        make(c, "enderman_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "enderman")))
                .row("#").row("T").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_PURPLE))
                .texture(Ingredient.of(ENDER_PEARL))
                .group("enderman_statue")
                .build());
        make(c, "iron_golem_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "iron_golem")))
                .row("##").row("TT").row("TT").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(IRON_BLOCK))
                .group("iron_golem_statue")
                .build());
    }

    private void make(Consumer<FinishedRecipe> c, String name, Ingredients ingredients) {
        c.accept(new Result(new SculptingRecipe(MobStatues.getId(name), ingredients)));
    }

    private ItemStack tagged(Consumer<CompoundTag> data) {
        ItemStack ret = new ItemStack(Registration.STATUE_ITEM.get());
        CompoundTag tag = ret.getOrCreateTag();
        data.accept(tag);
        return ret;
    }

    public record Result(SculptingRecipe recipe) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject ret) {
            JsonElement json = Ingredients.CODEC.encodeStart(JsonOps.INSTANCE, recipe.ingredients).getOrThrow(false, s -> {});
            json.getAsJsonObject().entrySet().forEach(e -> ret.add(e.getKey(), e.getValue()));
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
