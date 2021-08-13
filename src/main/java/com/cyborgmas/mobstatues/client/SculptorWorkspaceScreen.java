package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SculptorWorkspaceScreen extends AbstractContainerScreen<SculptorWorkspaceMenu> {
    public SculptorWorkspaceScreen(SculptorWorkspaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {

    }
}
