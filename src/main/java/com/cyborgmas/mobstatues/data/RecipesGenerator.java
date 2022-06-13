package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.registration.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

import static net.minecraft.world.item.Items.DARK_OAK_LOG;
import static net.minecraft.world.item.Items.STONE;

public class RecipesGenerator extends RecipeProvider {
    public RecipesGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> f) {
        ShapedRecipeBuilder.shaped(Registration.SCULPTOR_WORKSPACE_ITEM.get())
                .define('W', Ingredient.of(DARK_OAK_LOG))
                .define('S', Ingredient.of(STONE))
                .pattern(" S")
                .pattern(" S")
                .pattern("WW")
                .unlockedBy("has_stone", has(STONE))
                .save(f);
    }
}
