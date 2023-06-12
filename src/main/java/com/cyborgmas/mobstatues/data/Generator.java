package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.MobStatues;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MobStatues.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Generator {
    @SubscribeEvent
    public static void gatherDataEvent(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(true, new LangGen(generator));
        generator.addProvider(true, new AllModelsGenerator(event));
        generator.addProvider(true, new RecipesGenerator(event.getGenerator()));
        generator.addProvider(true, new SculptingRecipeGenerator(event.getGenerator()));
    }
}
