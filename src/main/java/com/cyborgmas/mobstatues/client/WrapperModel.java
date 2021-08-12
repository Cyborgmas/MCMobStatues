package com.cyborgmas.mobstatues.client;

import com.cyborgmas.mobstatues.MobStatues;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterators;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.Supplier;

//am i doing something dumb again
public class WrapperModel<E extends Entity> extends EntityModel<E> {
    //TODO srg names
    private static final Supplier<Method> GET_HEAD_PARTS = Suppliers.memoize(() -> ObfuscationReflectionHelper.findMethod(AgeableListModel.class, "headParts"));
    private static final Supplier<Method> GET_BODY_PARTS = Suppliers.memoize(() -> ObfuscationReflectionHelper.findMethod(AgeableListModel.class, "bodyParts"));

    private final EntityModel<E> wrapped;
    private final Iterable<ModelPart> bodyParts;
    private final Iterable<ModelPart> headParts;

    public WrapperModel(EntityModel<E> wrapped) {
        this.wrapped = wrapped;
        if (wrapped instanceof ListModel<E> list) {
            this.bodyParts = list.parts();
            this.headParts = Collections.emptyList();
        } else if (wrapped instanceof AgeableListModel<E> ageable) {
            try {
                Object temp = GET_HEAD_PARTS.get().invoke(ageable);
                Object temp1 = GET_BODY_PARTS.get().invoke(ageable);

                if (temp instanceof Iterable && temp1 instanceof Iterable) {
                    this.headParts = (Iterable<ModelPart>) temp;
                    this.bodyParts = (Iterable<ModelPart>) temp1;
                } else {
                    throw new RuntimeException("Could not get statue parts.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not get statue parts.", e);
            }
        } else {
            throw new RuntimeException("Unknown model!");
        }
    }

    @Override
    public void setupAnim(E p_102618_, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_) {
        this.wrapped.setupAnim(p_102618_, p_102619_, p_102620_, p_102621_, p_102622_, p_102623_);
    }

    @Override
    public void renderToBuffer(PoseStack p_103111_, VertexConsumer p_103112_, int p_103113_, int p_103114_, float p_103115_, float p_103116_, float p_103117_, float p_103118_) {
        this.wrapped.renderToBuffer(p_103111_, p_103112_, p_103113_, p_103114_, p_103115_, p_103116_, p_103117_, p_103118_);
    }

    @Override
    public void prepareMobModel(E p_102614_, float p_102615_, float p_102616_, float p_102617_) {
        this.wrapped.prepareMobModel(p_102614_, p_102615_, p_102616_, p_102617_);
    }

    public Iterable<ModelPart> getHeadParts() {
        return headParts;
    }

    public Iterable<ModelPart> getBodyParts() {
        return bodyParts;
    }
}
