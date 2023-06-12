package com.cyborgmas.mobstatues.client.sculptor;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.registration.Registration;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.Util;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = MobStatues.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RecipeBookHelper {
    public static final Supplier<RecipeBookCategories> SCULPTING_CRAFTING = Suppliers.memoize(() -> RecipeBookCategories.create("SCULPTING_CRAFTING", Util.make(() -> {
        ItemStack ret = new ItemStack(Registration.STATUE_ITEM.get());
        ret.getOrCreateTag().putString("id", "zombie");
        return ret;
    })));
    public static final Supplier<RecipeBookCategories> SCULPTING_SEARCH = Suppliers.memoize(() -> RecipeBookCategories.create("SCULPTING_SEARCH", new ItemStack(Items.COMPASS)));

    @SubscribeEvent
    public static void registerCategories(RegisterRecipeBookCategoriesEvent event)
    {
        event.registerBookCategories(Registration.SCULPTING, ImmutableList.of(SCULPTING_SEARCH.get(), SCULPTING_CRAFTING.get()));
        event.registerAggregateCategory(SCULPTING_SEARCH.get(), ImmutableList.of(SCULPTING_CRAFTING.get()));
        event.registerRecipeCategoryFinder(Registration.SCULPTING_RECIPE_TYPE.get(), r -> SCULPTING_CRAFTING.get());
    }
}
