package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.MobStatues;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = MobStatues.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Generator {
    @SubscribeEvent
    public static void gatherDataEvent(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(new LangGen(generator));
        generator.addProvider(new AllModelsGenerator(event));
        generator.addProvider(new RecipesGenerator(event.getGenerator()));

//        generator.addProvider(new StatueTransformsGenerator(event.getGenerator())
//                .makeTransformsFor(new ResourceLocation("pig"))
//                .transform()
//                .addTranslation(3)
//                .end(ItemCameraTransforms.TransformType.GUI)
//                .endTransforms());
    }
}
