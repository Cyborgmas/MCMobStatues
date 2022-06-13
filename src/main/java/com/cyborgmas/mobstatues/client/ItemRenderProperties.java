package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceBlock;
import com.cyborgmas.mobstatues.registration.Registration;
import com.cyborgmas.mobstatues.util.RenderingExceptionHandler;
import com.cyborgmas.mobstatues.util.StatueCreationHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.Map;
import java.util.WeakHashMap;

public class ItemRenderProperties {
    public static IItemRenderProperties getStatueBlockItemRender() {
        return new IItemRenderProperties() {
            private static final BlockEntityWithoutLevelRenderer RENDERER = new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {
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

                    try {
                        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(statue).render(statue, 0, 0, matrixStack, buffer, light);
                    } catch (Exception e) {
                        RenderingExceptionHandler.handle("item in inventory", statue.getType(), e);
                    }

                    matrixStack.popPose();
                }
            };

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return RENDERER;
            }
        };
    }

    public static IItemRenderProperties getSculptorWorkspaceBlockItemRenderer() {
        return new IItemRenderProperties() {
            private static final BlockEntityWithoutLevelRenderer RENDERER = new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {
                @Override
                public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
                    BakedModel bottomModel = blockRenderer.getBlockModel(Registration.SCULPTOR_WORKSPACE_BLOCK.get().defaultBlockState());
                    BakedModel topModel = blockRenderer.getBlockModel(Registration.SCULPTOR_WORKSPACE_BLOCK.get().defaultBlockState().setValue(SculptorWorkspaceBlock.HALF, DoubleBlockHalf.UPPER));

                    poseStack.pushPose();

                    poseStack.mulPose(Vector3f.YP.rotationDegrees(30));
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(17f));
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(12.5f));
                    poseStack.translate(3 / 16f, 1 / 16f, 2 / 16f);
                    poseStack.scale(0.5f, 0.5f, 0.5f);

                    //TODO find the right values in game then datagen it to json.
                    switch (transformType) {
                        case GUI -> {
                            poseStack.translate(0 , 2.5f / 16f, 0);
                            poseStack.scale(0.85f, 0.85f, 0.85f);
                        }
                        case GROUND -> poseStack.scale(0.2f, 0.2f, 0.2f);
//                        case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> poseStack.scale(0.5f, 0.5f, 0.5f);
//                        case THIRD_PERSON_LEFT_HAND, FIRST_PERSON_LEFT_HAND -> poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
                    }

                    itemRenderer.renderModelLists(bottomModel, stack, light, overlay, poseStack, buffer.getBuffer(Sheets.cutoutBlockSheet()));
                    poseStack.translate(0, 1, 0);
                    itemRenderer.renderModelLists(topModel, stack, light, overlay, poseStack, buffer.getBuffer(Sheets.cutoutBlockSheet()));

                    poseStack.popPose();
                }
            };

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return RENDERER;
            }
        };
    }
}
