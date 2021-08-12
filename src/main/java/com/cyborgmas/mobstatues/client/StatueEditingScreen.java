package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.StatueBlockEntity;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fmlclient.gui.GuiUtils;

public class StatueEditingScreen extends Screen {
    public static final int DEFAULT_SCALE = 80;

    private final Entity model;
    private final WrapperModel<? extends Entity> wrapper;
    private int viewerLeft;
    private int viewerRight;
    private int viewerBottom;
    private int viewerTop;

    public StatueEditingScreen(StatueBlockEntity statue) {
        super(TextComponent.EMPTY);

        this.model = statue.getStatue();
        EntityRenderer<?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.model);

        if (entityRenderer instanceof LivingEntityRenderer<?, ?> renderer) {
            this.wrapper = new WrapperModel<>(renderer.getModel());
        } else {
            MobStatues.LOGGER.warn("Entity {} is not supported for editing.", this.model.getType().getRegistryName());
            this.wrapper = null;
        }
    }

    @Override
    protected void init() {
        this.viewerLeft = this.width - 35 - 10 - 150;
        this.viewerRight = this.width - 35 - 10;
        this.viewerBottom = 25 + 10 + 100;
        this.viewerTop = 25 + 10;
    }

    public static boolean canEditModel(Entity e) {
        EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(e);
        return renderer instanceof LivingEntityRenderer;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        if (!(this.model instanceof LivingEntity e))
            return;

        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        renderEntityOnScreen(this.model, viewerLeft, viewerBottom, 75, 10, 0);
    }

    @Override
    public void renderBackground(PoseStack stack) {
        super.renderBackground(stack);
        GuiUtils.drawGradientRect(stack.last().pose(), this.getBlitOffset(), 35, 25, this.width - 35, this.height - 25, 0xFFE1E1E1, 0xFFBAB9B9);
        GuiUtils.drawGradientRect(stack.last().pose(), this.getBlitOffset(), viewerLeft, viewerTop, viewerRight, viewerBottom,0xFF282018, 0xFF271A0E);
    }

    /**
     * Copied from {@link InventoryScreen#renderEntityInInventory}
     */
    public void renderEntityOnScreen(Entity entity, int x, int y, int scale, float yRot, float xRot) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(x, y, 1050);
        modelViewStack.scale(1, 1, -1);
        RenderSystem.applyModelViewMatrix();

        PoseStack stack = new PoseStack();
        stack.translate(0, 0, 500);
        stack.scale(scale, scale, scale);
        Quaternion rotZ = Vector3f.ZP.rotationDegrees(180);
        Quaternion rotX = Vector3f.XP.rotationDegrees(xRot * 20.0F);
        rotZ.mul(rotX);
        stack.mulPose(rotZ);

        float oldYBody = 0;
        float oldYrot = 0;
        float oldXRot = 0;
        float oldYHead0 = 0;
        float oldYHead = 0;
        if (entity instanceof LivingEntity le) {
            oldYBody = le.yBodyRot;
            oldYrot = le.getYRot();
            oldXRot = le.getXRot();
            oldYHead0 = le.yHeadRotO;
            oldYHead = le.yHeadRot;
            le.yBodyRot = 180.0F + yRot;
            le.setYRot(180.0F + yRot);
            le.setXRot(-xRot);
            le.yHeadRot = le.getYRot();
            le.yHeadRotO = le.getYRot();
        }

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher renderer = Minecraft.getInstance().getEntityRenderDispatcher();
        renderer.setRenderShadow(false);

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderer.render(entity, 0, 0, 0, 0, 1, stack, buffer, 15728880);
        buffer.endBatch();

        renderer.setRenderShadow(true);

        if (entity instanceof LivingEntity le) {
            le.yBodyRot = oldYBody;
            le.setYRot(oldYrot);
            le.setXRot(oldXRot);
            le.yHeadRotO = oldYHead0;
            le.yHeadRot = oldYHead;
        }

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }
}
