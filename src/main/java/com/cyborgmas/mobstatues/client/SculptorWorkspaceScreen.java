package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SculptorWorkspaceScreen extends AbstractContainerScreen<SculptorWorkspaceMenu> {
    private static final ResourceLocation TEXTURE = MobStatues.getId("textures/gui/container/sculptor_workspace.png");

    public SculptorWorkspaceScreen(SculptorWorkspaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = this.imageWidth - this.font.width(Language.getInstance().getVisualOrder(this.title)) - 5;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(stack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        this.blit(stack, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    /**
     * Color gotten from super
     */
    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        this.font.draw(stack, this.title, this.titleLabelX, this.titleLabelY, 4210752);
    }
}
