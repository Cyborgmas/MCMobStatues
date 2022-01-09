package com.cyborgmas.mobstatues.objects.sculptor;

import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Iterator;

public interface PlaceSculptingRecipe<T> extends PlaceRecipe<T> {
    @Override
    default void placeRecipe(int width, int height, int outputIdx, Recipe<?> recipe, Iterator<T> iterator, int maxAmount) {
        PlaceRecipe.super.placeRecipe(width, height, outputIdx, recipe, iterator, maxAmount);
        //+1 since result slot is not in the container -- last params are never used
        this.addItemToSlot(iterator, SculptorWorkspaceContainer.COLOR_IDX + 1, maxAmount, -1, -1);
        this.addItemToSlot(iterator, SculptorWorkspaceContainer.TEXTURE_IDX + 1, maxAmount, -1, -1);
    }
}
