package com.cyborgmas.mobstatues.objects.sculptor;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.registration.Registration;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SculptingRecipe implements Recipe<SculptorWorkspaceContainer> {
    @SuppressWarnings("unchecked")
    public static final Supplier<RecipeType<SculptingRecipe>> TYPE = Suppliers.memoize(() -> (RecipeType<SculptingRecipe>) Registry.RECIPE_TYPE.get(MobStatues.getId("sculpting")));
    public final ResourceLocation id;
    public final Ingredients ingredients;
    private final int width;
    private final int height;
    private final List<Ingredient> items;

    public SculptingRecipe(ResourceLocation id, Ingredients ingredients) {
        this.id = id;
        this.ingredients = ingredients;
        this.width = ingredients.pattern.get(0).length();
        this.height = ingredients.pattern.size();
        List<String> pattern = new ArrayList<>(ingredients.pattern); //might need to reverse this list.
        while (pattern.size() != 4)
            pattern.add("  ");
        this.items = pattern.stream()
                .flatMap(s -> Stream.of(s.substring(0,1), s.substring(1, 2)))
                .map(s -> s.equals(" ") ? Ingredient.EMPTY : ingredients.sculpture.get(s))
                .peek(i -> Objects.requireNonNull(i, "A key in sculpting pattern was not defined!"))
                .toList();
    }

    /**
     * Taken from {@link ShapedRecipe}
     */
    @Override
    public boolean matches(SculptorWorkspaceContainer container, Level level) {
        if (!this.ingredients.color.test(container.getColor()) || !this.ingredients.texture.test(container.getTexture()))
            return false;

        for (int i = 0; i <= 2 - this.width; ++i) {
            for (int j = 0; j <= 4 - this.height; ++j) {
                if (this.matches(container, i, j, true) || this.matches(container, i, j, false))
                    return true;
            }
        }

        return false;
    }

    private boolean matches(SculptorWorkspaceContainer container, int x, int y, boolean mirror) { //unsure about the last boolean
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 4; ++j) {
                int curX = i - x;
                int curY = j - y;
                Ingredient ingredient = Ingredient.EMPTY;
                if (curX >= 0 && curY >= 0 && curX < this.width && curY < this.height) {
                    int idx = mirror ? this.width - curX - 1 + curY * this.width : curX + curY * this.width;
                    ingredient = this.items.get(idx);
                }

                if (!ingredient.test(container.getItem(i + j * 2))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(SculptorWorkspaceContainer container) {
        return this.getResultItem().copy();
    }

    @Override
    public ItemStack getResultItem() {
        return this.ingredients.result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false; //used for recipe book
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.SCULPTING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE.get();
    }

    public record Ingredients(List<String> pattern, Map<String, Ingredient> sculpture, Ingredient color, Ingredient texture, ItemStack result) {
        private static final Function<String, DataResult<String>> VERIFY_LENGTH_2 = s -> s.length() == 2 ? DataResult.success(s) :
                DataResult.error("Key row length must be of 2!");
        private static final Function<List<String>, DataResult<List<String>>> VERIFY_SIZE = l -> {
            if (l.size() <= 4 && l.size() >= 1) {
                List<String> temp = new ArrayList<>(l);
                Collections.reverse(temp); //reverse so the first row is at the bottom in the json.
                return DataResult.success(ImmutableList.copyOf(temp));
            }
            return DataResult.error("Pattern must have between 1 and 4 rows of keys");
        };
        //only used once, but needed for typing reasons
        private static final Function<String, DataResult<String>> VERIFY_LENGTH_1 = s -> s.length() == 1 ? DataResult.success(s) :
                DataResult.error("Key must be a single character!");

        public static final Codec<Ingredient> INGREDIENT_CODEC = Codec.PASSTHROUGH.comapFlatMap(obj -> {
            JsonElement json = obj.convert(JsonOps.INSTANCE).getValue();
            try {
                return DataResult.success(Ingredient.fromJson(json));
            } catch (Exception e) {
                MobStatues.LOGGER.warn("Failed to parse ingredient", e);
                return DataResult.error("Failed to parse ingredient: " + e.getMessage());
            }
        }, ingredient -> new Dynamic<>(JsonOps.INSTANCE, ingredient.toJson()));

        public static final Codec<Ingredients> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.flatXmap(VERIFY_LENGTH_2, VERIFY_LENGTH_2).listOf().flatXmap(VERIFY_SIZE, VERIFY_SIZE).fieldOf("pattern").forGetter(Ingredients::pattern),
                        Codec.unboundedMap(Codec.STRING.flatXmap(VERIFY_LENGTH_1, VERIFY_LENGTH_1), INGREDIENT_CODEC).fieldOf("key").forGetter(Ingredients::sculpture),
                        INGREDIENT_CODEC.optionalFieldOf("color", Ingredient.EMPTY).forGetter(Ingredients::color),
                        INGREDIENT_CODEC.optionalFieldOf("texture", Ingredient.EMPTY).forGetter(Ingredients::texture),
                        ItemStack.CODEC.fieldOf("result").forGetter(Ingredients::result)
                ).apply(inst, Ingredients::new)
        );

        public static class Builder {
            private final List<String> pattern = new ArrayList<>();
            private final Map<String, Ingredient> keys = new HashMap<>();
            private Ingredient texture = Ingredient.EMPTY;
            private Ingredient color = Ingredient.EMPTY;
            private final ItemStack result;

            private Builder(ItemStack result) {
                this.result = result;
            }

            public static Builder create(ItemStack result) {
                Objects.requireNonNull(result);
                return new Builder(result);
            }

            public Builder define(String key, Ingredient value) {
                Objects.requireNonNull(value);
                if (key.length() != 1)
                    throw new IllegalStateException("Key " + key + " must be of length 1");
                if (this.keys.put(key, value) != null)
                    throw new IllegalArgumentException("Key " + key + " was already defined!");
                return this;
            }

            public Builder row(String row) {
                if (pattern.size() == 4)
                    throw new IllegalArgumentException("Can't have a recipe with more than 4 rows!");
                if (row.length() < 1 || row.length() > 2)
                    throw new IllegalArgumentException("Row " + row + " must have 1 or 2 characters!");
                if (pattern.size() > 0 && pattern.get(0).length() != row.length())
                    throw new IllegalArgumentException("all rows must have the same length!");
                if (row.isBlank())
                    throw new IllegalArgumentException("Row must have 1 non whitespace character!");
                this.pattern.add(row);
                return this;
            }

            public Builder texture(Ingredient texture) {
                Objects.requireNonNull(texture);
                this.texture = texture;
                return this;
            }

            public Builder color(Ingredient color) {
                Objects.requireNonNull(color);
                this.color = color;
                return this;
            }

            public Ingredients build() {
                this.pattern.forEach(s -> {
                    String test = s.substring(0, 1);
                    if (!test.isBlank() && !this.keys.containsKey(test))
                        throw new IllegalStateException("Did not define key " + test);
                    test = s.length() == 2 ? s.substring(1, 2) : " ";
                    if (!test.isBlank() && !this.keys.containsKey(test))
                        throw new IllegalStateException("Did not define key " + test);
                });

                this.keys.keySet().forEach(k ->
                        this.pattern.stream().filter(s -> s.contains(k)).findAny()
                                .orElseThrow(() -> new IllegalStateException("Key " + k + " is not used in the pattern.")));

                List<String> pattern = this.pattern.stream()
                        .map(s -> s.length() == 2 ? s : s.charAt(0) + " ")
                        .collect(Collectors.toList());

                return new Ingredients(pattern, this.keys, this.color, this.texture, this.result);
            }
        }
    }
}
