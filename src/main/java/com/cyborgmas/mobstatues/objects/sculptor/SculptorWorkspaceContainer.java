package com.cyborgmas.mobstatues.objects.sculptor;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Bot-left slot is idx 0, top-right is index 7
 */
public class SculptorWorkspaceContainer extends SimpleContainer {
    public static final int TEXTURE_IDX = 8;
    public static final int COLOR_IDX = 9;

    public SculptorWorkspaceContainer() {
        super(10); //8 + 1 + 1
    }

    public ItemStack getTexture() {
        return this.getItem(TEXTURE_IDX);
    }

    public ItemStack getColor() {
        return this.getItem(COLOR_IDX);
    }
}
