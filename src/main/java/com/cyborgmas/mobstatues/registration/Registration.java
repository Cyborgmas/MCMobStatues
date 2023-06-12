package com.cyborgmas.mobstatues.registration;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.client.ItemRenderProperties;
import com.cyborgmas.mobstatues.objects.DelegatingBlockEntity;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipe;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipeSerializer;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceBlock;
import com.cyborgmas.mobstatues.objects.sculptor.SculptorWorkspaceMenu;
import com.cyborgmas.mobstatues.objects.statue.StatueBlock;
import com.cyborgmas.mobstatues.objects.statue.StatueBlockEntity;
import com.cyborgmas.mobstatues.objects.statue.StatueBlockItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

import static com.cyborgmas.mobstatues.MobStatues.MODID;

public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    public static final RecipeBookType SCULPTING = RecipeBookType.create("SCULPTING");

    public static void registerAll(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        RECIPE_TYPE.register(bus);
    }

    public static RegistryObject<Block> STATUE_BLOCK = BLOCKS.register("statue_block", () ->
            new StatueBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .isValidSpawn((s, bg, p, e) -> false)
                            .noOcclusion()
                            .dynamicShape() // makes it not cache the collision boxes and whatnot.
            )
    );

    public static RegistryObject<Block> SCULPTOR_WORKSPACE_BLOCK = BLOCKS.register("sculptor_workspace_block", () ->
            new SculptorWorkspaceBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.BLOCK)
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(4f))
    );

    public static RegistryObject<Item> STATUE_ITEM = ITEMS.register("statue", () ->
            new StatueBlockItem(
                    new Item.Properties()
                            .stacksTo(1)
            )
    );

    public static RegistryObject<Item> SCULPTOR_WORKSPACE_ITEM = ITEMS.register("sculptor_workspace", () ->
            new BlockItem(SCULPTOR_WORKSPACE_BLOCK.get(), new Item.Properties().stacksTo(1)) {
                @Override
                public void initializeClient(Consumer<IClientItemExtensions> consumer) {
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

    public static RegistryObject<RecipeType<SculptingRecipe>> SCULPTING_RECIPE_TYPE = make("sculpting");

    private static RegistryObject<RecipeType<SculptingRecipe>> make(String name) {
        return RECIPE_TYPE.register(name, () -> RecipeType.simple(MobStatues.getId(name)));
    }

    public static void registerTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SCULPTOR_WORKSPACE_ITEM.get());
        }
    }
}
