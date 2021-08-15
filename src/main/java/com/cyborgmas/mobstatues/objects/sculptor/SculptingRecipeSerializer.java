package com.cyborgmas.mobstatues.objects.sculptor;

import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipe.Ingredients;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class SculptingRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<SculptingRecipe> {
    @Override
    public SculptingRecipe fromJson(ResourceLocation id, JsonObject json) {
        Ingredients ingredients = Ingredients.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, s->{});
        return new SculptingRecipe(id, ingredients);
    }

    @Nullable
    @Override
    public SculptingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        Ingredients ingredients = buf.readWithCodec(Ingredients.CODEC);
        return new SculptingRecipe(id, ingredients);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, SculptingRecipe recipe) {
        buffer.writeWithCodec(Ingredients.CODEC, recipe.ingredients);
    }
}
