package com.cyborgmas.mobstatues;

import com.cyborgmas.mobstatues.client.StatueBlockEntityRenderer;
import com.cyborgmas.mobstatues.client.sculptor.SculptorWorkspaceScreen;
import com.cyborgmas.mobstatues.registration.Registration;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(MobStatues.MODID)
public class MobStatues {
    public static final String MODID = "mob_statues";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOB_STATUE_KEY = "mob_statue_data";
    public static final Random RANDOM = new Random();

    public MobStatues() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.registerAll(modbus);
    }

    public static ResourceLocation getId(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static MutableComponent translate(String prefix, String suffix, Object... args) {
        return Component.translatable(prefix + "." + MODID + "." + suffix, args);
    }

    public static String translateRaw(String prefix, String suffix) {
        return prefix + "." + MODID + "." + suffix;
    }

    @Mod.EventBusSubscriber(modid = MobStatues.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientHandler {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() ->  {
                BlockEntityRenderers.register(Registration.STATUE_BLOCK_ENTITY.get(), StatueBlockEntityRenderer::new);
                MenuScreens.register(Registration.SCULPTOR_WORKSPACE_MENU_TYPE.get(), SculptorWorkspaceScreen::new);
            });
        }
    }
}
