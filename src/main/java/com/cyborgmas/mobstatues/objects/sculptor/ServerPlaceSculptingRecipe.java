package com.cyborgmas.mobstatues.objects.sculptor;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.inventory.RecipeBookMenu;

public class ServerPlaceSculptingRecipe extends ServerPlaceRecipe<SculptorWorkspaceContainer> implements PlaceSculptingRecipe<Integer> {
    public ServerPlaceSculptingRecipe(RecipeBookMenu<SculptorWorkspaceContainer> menu) {
        super(menu);
    }
}
