package com.cyborgmas.mobstatues.registration;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.client.StatueTileRenderer;
import com.cyborgmas.mobstatues.objects.DelegatingTileEntity;
import com.cyborgmas.mobstatues.objects.StatueBlock;
import com.cyborgmas.mobstatues.objects.StatueBlockItem;
import com.cyborgmas.mobstatues.objects.StatueTileEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registration {
    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MobStatues.MODID);
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MobStatues.MODID);
    public static DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MobStatues.MODID);

    public static RegistryObject<Block> STATUE_BLOCK = BLOCKS.register("statue_block", () ->
            new StatueBlock(
                    BlockBehaviour.Properties.of(Material.STONE)
                            .isValidSpawn((s, r, p, e) -> false)
                            .noOcclusion()
                            .dynamicShape() // makes it not cache the collision boxes and whatnot.
            )
    );

    public static RegistryObject<Item> STATUE_ITEM = ITEMS.register("statue", () ->
            new StatueBlockItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .tab(CreativeModeTab.TAB_DECORATIONS)
            )
    );

    public static RegistryObject<BlockEntityType<StatueTileEntity>> STATUE_TILE =
            TILE_ENTITIES.register("statue", () -> BlockEntityType.Builder.of(StatueTileEntity::new, STATUE_BLOCK.get()).build(null));
    public static RegistryObject<BlockEntityType<DelegatingTileEntity>> DELEGATING_TILE =
            TILE_ENTITIES.register("delegating", () -> BlockEntityType.Builder.of(DelegatingTileEntity::new, STATUE_BLOCK.get()).build(null));

    public static void registerAll(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILE_ENTITIES.register(bus);
    }
}
