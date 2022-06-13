package com.cyborgmas.mobstatues.client.sculptor;

import com.cyborgmas.mobstatues.registration.Registration;
import com.google.common.collect.ImmutableList;
import net.minecraft.Util;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.RecipeBookRegistry;

public class RecipeBookHelper {
    public static final RecipeBookCategories SCULPTING_CRAFTING = RecipeBookCategories.create("SCULPTING_CRAFTING", Util.make(() -> {
        ItemStack ret = new ItemStack(Registration.STATUE_ITEM.get());
        ret.getOrCreateTag().putString("id", "zombie");
        return ret;
    }));
    public static final RecipeBookCategories SCULPTING_SEARCH = RecipeBookCategories.create("SCULPTING_SEARCH", new ItemStack(Items.COMPASS));

    public static void init() {
        RecipeBookRegistry.addCategoriesToType(Registration.SCULPTING, ImmutableList.of(SCULPTING_SEARCH, SCULPTING_CRAFTING));
        RecipeBookRegistry.addAggregateCategories(SCULPTING_SEARCH, ImmutableList.of(SCULPTING_CRAFTING));
        RecipeBookRegistry.addCategoriesFinder(Registration.SCULPTING_RECIPE_TYPE.get(), r -> SCULPTING_CRAFTING);
    }
}
