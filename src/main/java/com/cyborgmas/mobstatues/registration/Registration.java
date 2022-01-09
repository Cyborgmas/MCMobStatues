package com.cyborgmas.mobstatues.registration;

import com.cyborgmas.mobstatues.client.ItemRenderProperties;
import com.cyborgmas.mobstatues.objects.DelegatingBlockEntity;
import com.cyborgmas.mobstatues.objects.statue.StatueBlock;
import com.cyborgmas.mobstatues.objects.statue.StatueBlockEntity;
import com.cyborgmas.mobstatues.objects.statue.StatueBlockItem;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipe;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipeSerializer;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceBlock;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

import static com.cyborgmas.mobstatues.MobStatues.MODID;

public class Registration {
    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);
    public static DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    public static DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final RecipeBookType SCULPTING = RecipeBookType.create("SCULPTING");

    public static void registerAll(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }

    public static RegistryObject<Block> STATUE_BLOCK = BLOCKS.register("statue_block", () ->
            new StatueBlock(
                    BlockBehaviour.Properties.of(Material.STONE)
                            .isValidSpawn((s, bg, p, e) -> false)
                            .noOcclusion()
                            .dynamicShape() // makes it not cache the collision boxes and whatnot.
            )
    );

    public static RegistryObject<Block> SCULPTOR_WORKSPACE_BLOCK = BLOCKS.register("sculptor_workspace_block", () ->
            new SculptorWorkspaceBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(4f))
    );

    public static RegistryObject<Item> STATUE_ITEM = ITEMS.register("statue", () ->
            new StatueBlockItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .tab(CreativeModeTab.TAB_DECORATIONS)
            )
    );

    public static RegistryObject<Item> SCULPTOR_WORKSPACE_ITEM = ITEMS.register("sculptor_workspace", () ->
            new BlockItem(SCULPTOR_WORKSPACE_BLOCK.get(), new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_DECORATIONS)) {
                @Override
                public void initializeClient(Consumer<IItemRenderProperties> consumer) {
                    if (!DatagenModLoader.isRunningDataGen()) //crashes in datagen
                        consumer.accept(ItemRenderProperties.getSculptorWorkspaceBlockItemRenderer());
                }
            });

    public static RegistryObject<BlockEntityType<StatueBlockEntity>> STATUE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("statue", () -> BlockEntityType.Builder.of(StatueBlockEntity::new, STATUE_BLOCK.get()).build(null));
    public static RegistryObject<BlockEntityType<DelegatingBlockEntity>> DELEGATING_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("delegating", () -> BlockEntityType.Builder.of(DelegatingBlockEntity::new, STATUE_BLOCK.get()).build(null));

    public static RegistryObject<MenuType<SculptorWorkspaceMenu>> SCULPTOR_WORKSPACE_MENU_TYPE =
            MENUS.register("sculptor_workspace_menu", () -> IForgeMenuType.create(SculptorWorkspaceMenu::new));

    public static RegistryObject<RecipeSerializer<SculptingRecipe>> SCULPTING_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("sculpting_recipe", SculptingRecipeSerializer::new);
}
