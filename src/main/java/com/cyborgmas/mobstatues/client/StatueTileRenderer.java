package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.StatueTileEntity;
import com.cyborgmas.mobstatues.util.RenderingExceptionHandler;
import com.cyborgmas.mobstatues.util.StatueCreationHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;

public class StatueTileRenderer extends TileEntityRenderer<StatueTileEntity> {
    public StatueTileRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(StatueTileEntity statue, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        statue.renderEntity(stack, partialTicks, buffer, combinedLight);
    }

    public static ItemStackTileEntityRenderer getStatueItemRenderer() {
        return new ItemStackTileEntityRenderer() {
            private final Map<ItemStack, Entity> dynamicModelMap = new WeakHashMap<>();
            private final Map<ItemStack, Float> dynamicSizeMap = new WeakHashMap<>();

            @Override
            public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
                if (!stack.hasTag())
                    return;

                World world = Minecraft.getInstance().level;

                Entity statue = dynamicModelMap.computeIfAbsent(stack, s ->
                        StatueCreationHelper.getEntity(s.getOrCreateTag(), world));

                if (statue == null)
                    return;

                float scale = dynamicSizeMap.computeIfAbsent(stack, s -> {
                    EntitySize size = StatueCreationHelper.getEntitySize(s.getOrCreateTag(), world);
                    if (size == null) {
                        MobStatues.LOGGER.warn("Failed retrieving entity size with data {}", s.getOrCreateTag());
                        return 0F;
                    }
                    return 1 / Math.max(size.height, size.width);
                });

                if (scale == 0)
                    return;

                matrixStack.pushPose();

                if (scale < 1)
                    matrixStack.scale(scale, scale, scale);

                MobTransformLoader.applyEntitySpecificTransform(statue.getType(), transformType, matrixStack);

                try {
                    Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(statue).render(statue, 0, 0, matrixStack, buffer, light);
                } catch (Exception e) {
                    RenderingExceptionHandler.handle("item in inventory", statue.getType(), e);
                }

                matrixStack.popPose();
            }
        };
    }
}
