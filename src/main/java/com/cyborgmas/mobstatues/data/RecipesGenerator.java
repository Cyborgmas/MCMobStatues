package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.registration.Registration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.minecraft.world.entity.EntityType.*;
import static net.minecraft.world.item.Items.*;

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
