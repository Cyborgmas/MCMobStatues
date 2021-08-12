package com.cyborgmas.mobstatues;

import com.cyborgmas.mobstatues.client.MobTransformLoader;
import com.cyborgmas.mobstatues.client.StatueTileRenderer;
import com.cyborgmas.mobstatues.registration.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MobStatues.MODID)
public class MobStatues {
    public static final String MODID = "mob_statues";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOB_STATUE_KEY = "mob_statue_data";

    public MobStatues() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.registerAll(modbus);
    }

    public static ResourceLocation getId(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static TranslatableComponent translate(String prefix, String suffix, Object... args) {
        return new TranslatableComponent(prefix + "." + MODID + "." + suffix, args);
    }

    public static String translateRaw(String prefix, String suffix) {
        return prefix + "." + MODID + "." + suffix;
    }

    @Mod.EventBusSubscriber(modid = MobStatues.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientHandler {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ClientRegistry.b(Registration.STATUE_TILE.get(), StatueTileRenderer::new));
        }

        @SubscribeEvent
        public static void modConstruct(FMLConstructModEvent event) {
            event.enqueueWork(() -> {
                ResourceManager manager = Minecraft.getInstance().getResourceManager();
                if (manager instanceof ReloadableResourceManager) {
                    ((ReloadableResourceManager) manager).registerReloadListener(new MobTransformLoader());
                } else {
                    LOGGER.error("Could not add reload listener!");
                }
            });
        }
    }
}
