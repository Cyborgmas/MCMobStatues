package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.StatueTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.LazyValue;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public class StatueEditingScreen extends Screen {
    private static final LazyValue<Method> GET_HEAD_PARTS = new LazyValue<>(() -> ObfuscationReflectionHelper.findMethod(AgeableModel.class, "headParts"));
    private static final LazyValue<Method> GET_BODY_PARTS = new LazyValue<>(() -> ObfuscationReflectionHelper.findMethod(AgeableModel.class, "bodyParts"));

    private EntityRenderer<? extends Entity> main = null;
    private Entity model;
    private Iterable<ModelRenderer> body = null;
    private Iterable<ModelRenderer> head = null;

    @SuppressWarnings("unchecked")
    public StatueEditingScreen(StatueTileEntity statue) {
        super(new StringTextComponent("Title"));

        this.model = statue.getStatue();
        this.main = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.model);

        if (this.main instanceof LivingRenderer) {
            LivingRenderer<?, ?> renderer = (LivingRenderer<?, ?>) this.main;
            EntityModel<?> model = renderer.getModel();
            if (model instanceof SegmentedModel) {
                SegmentedModel<?> withPartsModel = (SegmentedModel<?>) model;
                this.body = withPartsModel.parts();
            } else if (model instanceof AgeableModel<?>) {
                AgeableModel<?> withHeadPartsModel = (AgeableModel<?>) model;
                try {
                    Object temp = GET_HEAD_PARTS.get().invoke(withHeadPartsModel);
                    Object temp1 = GET_BODY_PARTS.get().invoke(withHeadPartsModel);

                    if (temp instanceof Iterable)
                        this.head = (Iterable<ModelRenderer>) temp;
                    if (temp1 instanceof Iterable)
                        this.body = (Iterable<ModelRenderer>) temp1;
                } catch (Exception ex) {
                    MobStatues.LOGGER.warn("Could not get statue parts.");
                }
            }
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        if (this.model == null)
            return;

        super.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        stack.pushPose();

        stack.translate(2,2,2);

        renderEntity(this.main, this.model, stack, buffer, 0, partialTicks, 15728880);

        stack.popPose();
        buffer.endBatch();
    }

    @SuppressWarnings("unchecked")
    public static <E extends Entity> void renderEntity(EntityRenderer<? extends Entity> renderer, E entity, MatrixStack stack, IRenderTypeBuffer buffer, float yaw, float partialTicks, int light) {
        EntityRenderer<E> castedRenderer = (EntityRenderer<E>) renderer;
        castedRenderer.render(entity, yaw, partialTicks, stack, buffer, light);
    }
}
