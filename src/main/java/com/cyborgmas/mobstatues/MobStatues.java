package com.cyborgmas.mobstatues;

import com.cyborgmas.mobstatues.client.SculptorWorkspaceScreen;
import com.cyborgmas.mobstatues.client.StatueTileRenderer;
import com.cyborgmas.mobstatues.registration.Registration;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        modbus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> RecipeType.register(getId("sculpting").toString()));
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
            event.enqueueWork(() ->  {
                BlockEntityRenderers.register(Registration.STATUE_BLOCK_ENTITY.get(), StatueTileRenderer::new);
                MenuScreens.register(Registration.SCULPTOR_WORKSPACE_MENU_TYPE.get(), SculptorWorkspaceScreen::new);
            });
        }
    }
}
