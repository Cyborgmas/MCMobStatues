package com.cyborgmas.mobstatues.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Copied from {@link ShapelessRecipeBuilder}
 */
public class WithNBTShapelessRecipeBuilder {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ItemStack result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
    private String group;

    public WithNBTShapelessRecipeBuilder(ItemStack resultIn, int countIn) {
        this.result = resultIn;
        this.count = countIn;
    }

    public static WithNBTShapelessRecipeBuilder shapelessRecipe(ItemStack resultIn) {
        return new WithNBTShapelessRecipeBuilder(resultIn, 1);
    }

    public static WithNBTShapelessRecipeBuilder shapelessRecipe(ItemStack resultIn, int countIn) {
        return new WithNBTShapelessRecipeBuilder(resultIn, countIn);
    }

    public WithNBTShapelessRecipeBuilder addIngredient(TagKey<Item> tagIn) {
        return this.addIngredient(Ingredient.of(tagIn));
    }

    public WithNBTShapelessRecipeBuilder addIngredient(ItemLike itemIn) {
        return this.addIngredient(itemIn, 1);
    }

    public WithNBTShapelessRecipeBuilder addIngredient(ItemLike itemIn, int quantity) {
        for(int i = 0; i < quantity; ++i) {
            this.addIngredient(Ingredient.of(itemIn));
        }

        return this;
    }

    public WithNBTShapelessRecipeBuilder addIngredient(Ingredient ingredientIn) {
        return this.addIngredient(ingredientIn, 1);
    }

    public WithNBTShapelessRecipeBuilder addIngredient(Ingredient ingredientIn, int quantity) {
        for(int i = 0; i < quantity; ++i) {
            this.ingredients.add(ingredientIn);
        }
        return this;
    }

    public WithNBTShapelessRecipeBuilder addCriterion(String name, CriterionTriggerInstance criterionIn) {
        this.advancementBuilder.addCriterion(name, criterionIn);
        return this;
    }

    public WithNBTShapelessRecipeBuilder setGroup(String groupIn) {
        this.group = groupIn;
        return this;
    }

    public void build(Consumer<FinishedRecipe> consumerIn) {
        this.build(consumerIn, Registry.ITEM.getKey(this.result.getItem()));
    }

    public void build(Consumer<FinishedRecipe> consumerIn, String save) {
        ResourceLocation resourcelocation = Registry.ITEM.getKey(this.result.getItem());
        if ((new ResourceLocation(save)).equals(resourcelocation)) {
            throw new IllegalStateException("Shapeless Recipe " + save + " should remove its 'save' argument");
        } else {
            this.build(consumerIn, new ResourceLocation(save));
        }
    }

    public void build(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
        this.validate(id);
        this.advancementBuilder.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
        consumerIn.accept(new WithNBTShapelessRecipeBuilder.Result(id, this.result, this.count, this.group == null ? "" : this.group, this.ingredients, this.advancementBuilder, new ResourceLocation(id.getNamespace(), "recipes/" + this.result.getItem().getItemCategory().getRecipeFolderName() + "/" + id.getPath())));
    }

    /**
     * Makes sure that this recipe is valid and obtainable.
     */
    private void validate(ResourceLocation id) {
        if (this.advancementBuilder.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final ItemStack result;
        private final int count;
        private final String group;
        private final List<Ingredient> ingredients;
        private final Advancement.Builder advancementBuilder;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation idIn, ItemStack result, int countIn, String groupIn, List<Ingredient> ingredientsIn, Advancement.Builder advancementBuilderIn, ResourceLocation advancementIdIn) {
            this.id = idIn;
            this.result = result;
            this.count = countIn;
            this.group = groupIn;
            this.ingredients = ingredientsIn;
            this.advancementBuilder = advancementBuilderIn;
            this.advancementId = advancementIdIn;
        }

        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonArray jsonarray = new JsonArray();

            for(Ingredient ingredient : this.ingredients) {
                jsonarray.add(ingredient.toJson());
            }

            json.add("ingredients", jsonarray);
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("item", Registry.ITEM.getKey(this.result.getItem()).toString());
            if (this.count > 1)
                jsonobject.addProperty("count", this.count);
            if (this.result.getTag() != null)
                jsonobject.addProperty("nbt", this.result.getTag().toString());

            json.add("result", jsonobject);
        }

        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        /**
         * Gets the ID for the recipe.
         */
        public ResourceLocation getId() {
            return this.id;
        }

        /**
         * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
         */
        @Nullable
        public JsonObject serializeAdvancement() {
            return this.advancementBuilder.serializeToJson();
        }

        /**
         * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #serializeAdvancement}
         * is non-null.
         */
        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
