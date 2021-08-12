package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.registration.Registration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
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
    //Only initialised after mods so static init is fine!
    private static final ItemStack BASE = new ItemStack(Registration.STATUE_ITEM.get());
    private static final Map<EntityType<?>, Item> SIMPLE_RECIPES = Util.make(() -> new ImmutableMap.Builder<EntityType<?>, Item>()
            .put(CREEPER, GUNPOWDER)
            .put(ZOMBIE, ROTTEN_FLESH)
            .put(SKELETON, BONE)
            .put(ENDERMAN, Items.ENDER_PEARL)
            .put(SPIDER, SPIDER_EYE)
            .put(BLAZE, BLAZE_ROD)
            .put(ZOMBIFIED_PIGLIN, GOLD_INGOT)
            .put(GUARDIAN, PRISMARINE_SHARD)
            .put(COW, LEATHER)
            .put(PIG, PORKCHOP)
            .put(HORSE, GOLDEN_APPLE)
            .put(VILLAGER, EMERALD)
            .put(CAT, Items.COD)
            .put(IRON_GOLEM, IRON_BLOCK)
            .build()
    );

    private static final List<RecipeMaker> RECIPE_MAKERS = Util.make(() -> new ImmutableList.Builder<RecipeMaker>()
            .addAll(SIMPLE_RECIPES.entrySet().stream().map(e -> simple(e.getKey(), e.getValue())).collect(Collectors.toList()))
            .add(simpleTag(SHEEP, ItemTags.WOOL))
            .add(weighted(MAGMA_CUBE, MAGMA_CREAM, 1))
            .add(complex(MAGMA_CUBE, MAGMA_CREAM, stack -> stack.getOrCreateTag().putInt("Size", 1)))
            .add(weightedComplex(MAGMA_CUBE, MAGMA_CREAM, 4, stack -> stack.getOrCreateTag().putInt("Size", 2)))
            .add(weightedComplex(MAGMA_CUBE, MAGMA_CREAM, 6, stack -> stack.getOrCreateTag().putInt("Size", 3)))
            .add(weightedComplex(MAGMA_CUBE, MAGMA_CREAM, 8, stack -> stack.getOrCreateTag().putInt("Size", 4)))
            .build()
    );

    public RecipesGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> f) {
        RECIPE_MAKERS.forEach(rm -> rm.makeRecipe(f));
    }

    private static ItemStack putId(EntityType<?> type) {
        ItemStack ret = BASE.copy();
        ret.getOrCreateTag().putString("id", type.getRegistryName().toString());
        return ret;
    }

    private static RecipeMaker simple(EntityType<?> model, Item ingredient) {
        return complex(model, ingredient, s -> {});
    }

    private static RecipeMaker complex(EntityType<?> model, Item ingredient, Consumer<ItemStack> dataSetter) {
        return weightedComplex(model, ingredient, 2, dataSetter);
    }

    private static RecipeMaker weighted(EntityType<?> model, Item ingredient, int nb) {
        return weightedComplex(model, ingredient, nb, s -> {});
    }

    private static RecipeMaker simpleTag(EntityType<?> model, Tag.Named<Item> ingredient) {
        return complexTag(model, ingredient, s -> {});
    }

    private static RecipeMaker complexTag(EntityType<?> model, Tag.Named<Item> ingredient, Consumer<ItemStack> dataSetter) {
        return weightedComplexTag(model, ingredient, 2, dataSetter);
    }

    private static RecipeMaker weightedTag(EntityType<?> model, Tag.Named<Item> ingredient, int nb) {
        return weightedComplexTag(model, ingredient, nb, s -> {});
    }

    private static RecipeMaker weightedComplexTag(EntityType<?> model, Tag.Named<Item> ingredient, int nb, Consumer<ItemStack> dataSetter) {
        return f ->
                WithNBTShapelessRecipeBuilder.shapelessRecipe(Util.make(putId(model), dataSetter))
                        .addIngredient(Ingredient.of(ingredient), nb)
                        .addIngredient(Registration.STATUE_ITEM.get())
                        .addCriterion("has_statue", has(Registration.STATUE_ITEM.get()))
                        .addCriterion("has_" + ingredient.getName().getPath(), has(ingredient))
                        .build(f, getRecipeName(model.getRegistryName().getPath())  + "_mob_statue");
    }

    private static RecipeMaker weightedComplex(EntityType<?> model, Item ingredient, int nb, Consumer<ItemStack> dataSetter) {
        return f ->
                WithNBTShapelessRecipeBuilder.shapelessRecipe(Util.make(putId(model), dataSetter))
                        .addIngredient(ingredient, nb)
                        .addIngredient(Registration.STATUE_ITEM.get())
                        .addCriterion("has_statue", has(Registration.STATUE_ITEM.get()))
                        .addCriterion("has_" + ingredient.getRegistryName().getPath(), has(ingredient))
                        .build(f, getRecipeName(model.getRegistryName().getPath())  + "_mob_statue");
    }

    private final static Map<String, Integer> NAME_MAP = new HashMap<>();

    private static String getRecipeName(String name) {
        if (NAME_MAP.containsKey(name)) {
            int idx = NAME_MAP.get(name);
            NAME_MAP.put(name, idx+1);
            return name + "_" + idx;
        }
        NAME_MAP.put(name, 0);
        return name;
    }

    @FunctionalInterface
    interface RecipeMaker {
        void makeRecipe(Consumer<FinishedRecipe> f);
    }
}
