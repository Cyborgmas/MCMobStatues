package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.StatueTileEntity;
import com.cyborgmas.mobstatues.util.RenderingExceptionHandler;
import com.cyborgmas.mobstatues.util.StatueCreationHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

public class StatueTileRenderer extends BlockEntityRenderer<StatueTileEntity> {
    public StatueTileRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(StatueTileEntity statue, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        statue.renderEntity(stack, partialTicks, buffer, combinedLight);
    }

    public static BlockEntityWithoutLevelRenderer getStatueItemRenderer() {
        return new BlockEntityWithoutLevelRenderer() {
            private final Map<ItemStack, Entity> dynamicModelMap = new WeakHashMap<>();
            private final Map<ItemStack, Float> dynamicSizeMap = new WeakHashMap<>();

            @Override
            public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
                if (!stack.hasTag())
                    return;

                Level world = Minecraft.getInstance().level;

                Entity statue = dynamicModelMap.computeIfAbsent(stack, s ->
                        StatueCreationHelper.getEntity(s.getOrCreateTag(), world));

                if (statue == null)
                    return;

                float scale = dynamicSizeMap.computeIfAbsent(stack, s -> {
                    EntityDimensions size = StatueCreationHelper.getEntitySize(s.getOrCreateTag(), world);
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
