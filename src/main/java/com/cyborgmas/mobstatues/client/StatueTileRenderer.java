package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.objects.statue.StatueBlockEntity;
import com.cyborgmas.mobstatues.util.RenderingExceptionHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class StatueTileRenderer implements BlockEntityRenderer<StatueBlockEntity> {
    public StatueTileRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(StatueBlockEntity statueBE, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!statueBE.shouldRender() || !statueBE.setup())
            return;

        stack.pushPose();

        stack.translate(statueBE.getToCenter().x(), 0, statueBE.getToCenter().z());
        stack.mulPose(Vector3f.YP.rotationDegrees(statueBE.getYRotation()));

        try {
            Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(statueBE.getStatue()).render(statueBE.getStatue(), 0, partialTicks, stack, buffer, light);
        } catch (Exception e) {
            RenderingExceptionHandler.handle("statue", statueBE.getStatue().getType(), e);
        }

        stack.popPose();
    }
}
