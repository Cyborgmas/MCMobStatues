package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.StatueBlockEntity;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class StatueEditingScreen extends Screen {
    private static final Supplier<Method> GET_HEAD_PARTS = Suppliers.memoize(() -> ObfuscationReflectionHelper.findMethod(AgeableListModel.class, "headParts"));
    private static final Supplier<Method> GET_BODY_PARTS = Suppliers.memoize(() -> ObfuscationReflectionHelper.findMethod(AgeableListModel.class, "bodyParts"));

    private EntityRenderer<? extends Entity> main = null;
    private Entity model;
    private Iterable<ModelPart> body = null;
    private Iterable<ModelPart> head = null;

    @SuppressWarnings("unchecked")
    public StatueEditingScreen(StatueBlockEntity statue) {
        super(new TextComponent("Title"));

        this.model = statue.getStatue();
        this.main = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.model);

        if (this.main instanceof LivingEntityRenderer) {
            LivingEntityRenderer<?, ?> renderer = (LivingEntityRenderer<?, ?>) this.main;
            EntityModel<?> model = renderer.getModel();
            if (model instanceof ListModel) {
                ListModel<?> withPartsModel = (ListModel<?>) model;
                this.body = withPartsModel.parts();
            } else if (model instanceof AgeableListModel<?>) {
                AgeableListModel<?> withHeadPartsModel = (AgeableListModel<?>) model;
                try {
                    Object temp = GET_HEAD_PARTS.get().invoke(withHeadPartsModel);
                    Object temp1 = GET_BODY_PARTS.get().invoke(withHeadPartsModel);

                    if (temp instanceof Iterable)
                        this.head = (Iterable<ModelPart>) temp;
                    if (temp1 instanceof Iterable)
                        this.body = (Iterable<ModelPart>) temp1;
                } catch (Exception ex) {
                    MobStatues.LOGGER.warn("Could not get statue parts.");
                }
            }
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        if (this.model == null)
            return;

        super.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        stack.pushPose();

        stack.translate(2,2,2);

        renderEntity(this.main, this.model, stack, buffer, 0, partialTicks, 15728880);

        stack.popPose();
        buffer.endBatch();
    }

    @SuppressWarnings("unchecked")
    public static <E extends Entity> void renderEntity(EntityRenderer<? extends Entity> renderer, E entity, PoseStack stack, MultiBufferSource buffer, float yaw, float partialTicks, int light) {
        EntityRenderer<E> castedRenderer = (EntityRenderer<E>) renderer;
        castedRenderer.render(entity, yaw, partialTicks, stack, buffer, light);
    }
}
