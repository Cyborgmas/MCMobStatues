package com.cyborgmas.mobstatues.data;

import com.cyborgmas.mobstatues.MobStatues;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipe;
import com.cyborgmas.mobstatues.objects.sculptor.SculptingRecipe.Ingredients;
import com.cyborgmas.mobstatues.registration.Registration;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.NBTIngredient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class SculptingRecipeGenerator extends RecipeProvider {
    public SculptingRecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> c) {
        //Overworld Peaceful
        make(c, "iron_golem_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "iron_golem")))
                .row("##").row("TT").row("TT").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(IRON_BLOCK))
                .build());

        make(c,"cow_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "cow")))
                .row("##").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(LEATHER))
                .build());
        make(c,"mooshroom_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "mooshroom")))
                .row("##").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_RED))
                .texture(Ingredient.of(RED_MUSHROOM))
                .group("mooshroom")
                .build());
        make(c,"brown_mooshroom_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> {
                    t.putString("id", "mooshroom");
                    t.putString("Type", "brown");
                }))
                .row("##").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_BROWN))
                .texture(Ingredient.of(BROWN_MUSHROOM))
                .group("mooshroom")
                .build());

        make(c,"pig_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "pig")))
                .row("#T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_PINK))
                .texture(Ingredient.of(PORKCHOP))
                .build());

        for (DyeColor dyeColor : DyeColor.values()) {
            make(c, "sheep/" + dyeColor.getName() + "_sheep_statue_sculpting", Ingredients.Builder.create(
                    tagged(t -> {
                        t.putString("id", "sheep");
                        t.putByte("Color", (byte) dyeColor.getId());
                    }))
                    .row("##").row("TT")
                    .define("#", Ingredient.of(COBBLESTONE))
                    .define("T", Ingredient.of(STONE))
                    .color(Ingredient.of(dyeColor.getTag()))
                    .texture(Ingredient.of(WHITE_WOOL))
                    .group("sheep")
                    .build());
        }

        for (Map.Entry<Variant, Ingredient> e : HORSE_MAP.entrySet()) {
            make(c,"horse/" + e.getKey().name().toLowerCase(Locale.ROOT) + "_horse_statue_sculpting", Ingredients.Builder.create(
                    tagged(t -> {
                        t.putString("id", "horse");
                        t.putInt("Variant", e.getKey().getId());
                    }))
                    .row("#T").row("#T").row(" T")
                    .define("#", Ingredient.of(COBBLESTONE))
                    .define("T", Ingredient.of(STONE))
                    .color(e.getValue())
                    .texture(Ingredient.of(SLIME_BALL))
                    .group("horse")
                    .build());
        }

        for (Map.Entry<Integer, Pair<String, Ingredient>> e : RABBIT_COLOR.entrySet()) {
            make(c,"rabbit/" + e.getValue().getFirst().toLowerCase(Locale.ROOT) + "_rabbit_statue_sculpting", Ingredients.Builder.create(
                    tagged(t -> {
                        t.putString("id", "rabbit");
                        t.putInt("RabbitType", e.getKey());
                    }))
                    .row("T")
                    .define("T", Ingredient.of(STONE))
                    .color(e.getValue().getSecond())
                    .texture(Ingredient.of(RABBIT_HIDE))
                    .group("rabbit")
                    .build());
        }

        for (Map.Entry<Axolotl.Variant, Ingredient> e : AXOLOTL_MAP.entrySet()) {
            make(c,"axolotl/" + e.getKey().name().toLowerCase(Locale.ROOT) + "_axolotl_statue_sculpting", Ingredients.Builder.create(
                    tagged(t -> {
                        t.putString("id", "axolotl");
                        t.putInt("Variant", e.getKey().getId());
                    }))
                    .row("T")
                    .define("T", Ingredient.of(STONE))
                    .color(e.getValue())
                    //TODO make ingredient that accepts all regen potions (strong, long & splash?)
                    .texture(NBTIngredient.of(PotionUtils.setPotion(new ItemStack(POTION), Potions.REGENERATION)))
                    .group("axolotl")
                    .build());
        }

        for (int i = 0; i < CAT_TYPE.length; i++) {
            String name = CAT_TYPE[i];
            Ingredient color = CAT_COLOR[i];
            final int fi = i;
            make(c, "cat/" + name + "_cat_statue_sculpting", Ingredients.Builder.create(
                            tagged(t -> {
                                t.putString("id", "cat");
                                t.putByte("CatType", (byte) fi);
                            }))
                    .row("TT")
                    .define("T", Ingredient.of(STONE))
                    .color(color)
                    .texture(Ingredient.of(ItemTags.FISHES))
                    .group("cat")
                    .build());
        }

        make(c, "ocelot_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "ocelot")))
                .row("#T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_YELLOW))
                .texture(Ingredient.of(SAND))
                .group("cat")
                .build());

        make(c,"bat_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "bat")))
                .row("#")
                .define("#", Ingredient.of(COBBLESTONE))
                .color(Ingredient.of(Tags.Items.DYES_BLACK))
                .texture(Ingredient.of(LEATHER))
                .build());
        make(c,"bee_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "bee")))
                .row("T")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_YELLOW))
                .texture(Ingredient.of(HONEYCOMB))
                .build());
        make(c,"chicken_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "chicken")))
                .row("#")
                .define("#", Ingredient.of(COBBLESTONE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(FEATHER))
                .build());
        make(c,"cod_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "cod")))
                .row("##")
                .define("#", Ingredient.of(PRISMARINE))
                .color(Ingredient.of(Tags.Items.DYES_BROWN))
                .group("fish")
                .build());
        make(c,"pufferfish_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "pufferfish")))
                .row("##")
                .define("#", Ingredient.of(PRISMARINE))
                .color(Ingredient.of(Tags.Items.DYES_BROWN))
                .group("fish")
                .build());
        make(c,"salmon_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "salmon")))
                .row("##")
                .define("#", Ingredient.of(PRISMARINE))
                .color(Ingredient.of(Tags.Items.DYES_PINK))
                .group("fish")
                .build());
        make(c,"dolphin_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "dolphin")))
                .row("##").row("TT")
                .define("#", Ingredient.of(PRISMARINE))
                .define("T", Ingredient.of(PRISMARINE_BRICKS))
                .color(Ingredient.of(Tags.Items.DYES_LIGHT_GRAY))
                .build());
        make(c,"donkey_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "donkey")))
                .row("##").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GRAY))
                .texture(Ingredient.of(SLIME_BALL))
                .build());
        make(c,"fox_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "fox")))
                .row("#T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_ORANGE))
                .texture(Ingredient.of(SWEET_BERRIES))
                .build());
        make(c,"wolf_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "wolf")))
                .row("#T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GRAY))
                .texture(Ingredient.of(MUTTON))
                .build());
        make(c,"squid_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "squid")))
                .row("#T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_BLUE))
                .texture(Ingredient.of(INK_SAC))
                .build());
        make(c,"glow_squid_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "glow_squid")))
                .row("#T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_BLUE))
                .texture(Ingredient.of(GLOW_INK_SAC))
                .build());
        make(c,"goat_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "goat")))
                .row("##").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(MILK_BUCKET))
                .build());

        for (Ingredient variant : LLAMA_VARIANTS) {
            int idx = LLAMA_VARIANTS.indexOf(variant);
            String variantName = LLAMA_VARIANTS_NAMES.get(idx);
            make(c, "llama/" + variantName + "_llama_statue_sculpting", Ingredients.Builder.create(
                    tagged(t -> {
                        t.putString("id", "llama");
                        t.putInt("Variant", idx);
                    }))
                    .row("##").row("TT").row(" T")
                    .define("#", Ingredient.of(COBBLESTONE))
                    .define("T", Ingredient.of(STONE))
                    .texture(variant)
                    .group("llama_" + variantName)
                    .build());
            make(c, "llama/" + variantName + "_trader_llama_statue_sculpting", Ingredients.Builder.create(
                    tagged(t -> {
                        t.putString("id", "trader_llama");
                        t.putInt("Variant", idx);
                    }))
                    .row("##").row("TT").row(" T")
                    .define("#", Ingredient.of(COBBLESTONE))
                    .define("T", Ingredient.of(STONE))
                    .texture(variant)
                    .color(Ingredient.of(GOLD_NUGGET))
                    .group("llama_" + variantName)
                    .build());

            for (DyeColor color : DyeColor.values()) {
                make(c,"llama/" + variantName + "_" + color.getName() + "_carpet_llama_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> {
                            t.putString("id", "llama");
                            t.putInt("Variant", LLAMA_VARIANTS.indexOf(variant));
                            t.putInt("DecorColor", color.getId());
                        }))
                        .row("##").row("TT").row(" T")
                        .define("#", Ingredient.of(COBBLESTONE))
                        .define("T", Ingredient.of(STONE))
                        .color(Ingredient.of(color.getTag()))
                        .texture(variant)
                        .group("llama_" + variantName)
                        .build());
            }
        }

        make(c,"panda_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> {
                    t.putString("id", "panda");
                    t.putString("MainGene", "normal");
                    t.putString("HiddenGene", "normal");
                }))
                .row("##").row("TT").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_BLACK))
                .texture(Ingredient.of(WHITE_WOOL))
                .group("panda")
                .build());
        make(c,"brown_panda_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> {
                    t.putString("id", "panda");
                    t.putString("MainGene", "brown");
                    t.putString("HiddenGene", "brown");
                }))
                .row("##").row("TT").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_BROWN))
                .texture(Ingredient.of(WHITE_WOOL))
                .group("panda")
                .build());

        for (TagKey<Item> key : PARROT_COLOR) {
            String tag = key.location().getPath();
            make(c,"parrot/" + tag.substring(tag.indexOf('/') + 1) + "_parrot_statue_sculpting", Ingredients.Builder.create(
                    tagged(t -> {
                        t.putString("id", "parrot");
                        t.putInt("Variant", PARROT_COLOR.indexOf(key));
                    }))
                    .row("T")
                    .define("T", Ingredient.of(STONE))
                    .color(Ingredient.of(key))
                    .texture(Ingredient.of(FEATHER))
                    .group("parrot")
                    .build());
        }

        make(c,"polar_bear_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "polar_bear")))
                .row("##").row("TT").row("TT")
                .define("#", Ingredient.of(PACKED_ICE))
                .define("T", Ingredient.of(BLUE_ICE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(SNOW_BLOCK))
                .build());
        make(c,"snow_golem_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "snow_golem")))
                .row("#").row("T")
                .define("#", Ingredient.of(PACKED_ICE))
                .define("T", Ingredient.of(BLUE_ICE))
                .color(Ingredient.of(Tags.Items.DYES_ORANGE))
                .texture(Ingredient.of(SNOW_BLOCK))
                .build());
        make(c,"turtle_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "turtle")))
                .row("TT").row("TT")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_LIME))
                .texture(Ingredient.of(SCUTE))
                .build());

        //Overworld Hostile
        make(c,"zombie_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "zombie")))
                .row("#").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GREEN))
                .build());
        make(c,"drowned_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "drowned")))
                .row("#").row("T")
                .define("#", Ingredient.of(PRISMARINE))
                .define("T", Ingredient.of(PRISMARINE_BRICKS))
                .color(Ingredient.of(Tags.Items.DYES_BLUE))
                .build());
        make(c,"husk_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "husk")))
                .row("#").row("T")
                .define("#", Ingredient.of(SAND))
                .define("T", Ingredient.of(SANDSTONE))
                .color(Ingredient.of(Tags.Items.DYES_BROWN))
                .build());
        make(c,"creeper_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "creeper")))
                .row("#").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_LIME))
                .texture(Ingredient.of(GUNPOWDER))
                .build());
        make(c,"guardian_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "guardian")))
                .row("#T").row("TT")
                .define("#", Ingredient.of(PRISMARINE))
                .define("T", Ingredient.of(PRISMARINE_BRICKS))
                .color(Ingredient.of(Tags.Items.DYES_CYAN))
                .build());
        make(c,"elder_guardian_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "elder_guardian")))
                .row("##").row("#T").row("TT")
                .define("#", Ingredient.of(PRISMARINE))
                .define("T", Ingredient.of(PRISMARINE_BRICKS))
                .color(Ingredient.of(Tags.Items.DYES_BROWN))
                .texture(Ingredient.of(SPONGE))
                .build());
        make(c,"skeleton_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "skeleton")))
                .row("#").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(BONE))
                .build());
        make(c,"stray_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "stray")))
                .row("#").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_LIGHT_GRAY))
                .texture(Ingredient.of(BONE))
                .build());
        make(c,"skeleton_horse_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "skeleton_horse")))
                .row("#T").row("#T").row(" T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(BONE))
                .build());
        make(c,"zombie_horse_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "zombie_horse")))
                .row("#T").row("#T").row(" T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GREEN))
                .texture(Ingredient.of(ROTTEN_FLESH))
                .build());
        make(c,"spider_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "spider")))
                .row("TT")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_BLACK))
                .texture(Ingredient.of(STRING))
                .build());
        make(c,"cave_spider_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "cave_spider")))
                .row("T")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GREEN))
                .texture(Ingredient.of(STRING))
                .build());
        make(c,"ghast_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "ghast")))
                .row("TT").row("TT")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_WHITE))
                .texture(Ingredient.of(GHAST_TEAR))
                .build());
        make(c,"phantom_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "phantom")))
                .row("TT")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_BLUE))
                .texture(Ingredient.of(PHANTOM_MEMBRANE))
                .build());
        make(c,"silverfish_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "silverfish")))
                .row("T")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_LIGHT_GRAY))
                .build());
        make(c,"witch_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "witch")))
                .row("#").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_LIGHT_GRAY))
                .texture(Ingredient.of(REDSTONE))
                .build());

        //Pillagers
        make(c,"pillager_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "pillager")))
                .row("#").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GRAY))
                .texture(Ingredient.of(CROSSBOW))
                .group("pillager")
                .build());
        make(c,"vindicator_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "vindicator")))
                .row("#").row("T")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GRAY))
                .texture(Ingredient.of(IRON_AXE))
                .group("pillager")
                .build());
        make(c,"evoker_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "evoker")))
                .row("T").row("T")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GRAY))
                .texture(Ingredient.of(TOTEM_OF_UNDYING))
                .group("pillager")
                .build());
        make(c,"illusioner_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "illusioner")))
                .row("T").row("T")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GRAY))
                .texture(NBTIngredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY)))
                .group("pillager")
                .build());
        make(c,"ravager_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "ravager")))
                .row("##").row("##").row("TT").row("TT")
                .define("#", Ingredient.of(COBBLESTONE))
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GRAY))
                .texture(Ingredient.of(IRON_HORSE_ARMOR))
                .group("pillager")
                .build());
        make(c,"vex_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "vex")))
                .row("T")
                .define("T", Ingredient.of(STONE))
                .color(Ingredient.of(Tags.Items.DYES_GRAY))
                .texture(Ingredient.of(PHANTOM_MEMBRANE))
                .group("pillager")
                .build());

        //NETHER
        make(c,"strider_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "strider")))
                .row("##").row("TT")
                .define("#", Ingredient.of(BASALT))
                .define("T", Ingredient.of(SMOOTH_BASALT))
                .color(Ingredient.of(Tags.Items.DYES_RED))
                .texture(Ingredient.of(STRING))
                .build());
        make(c,"blaze_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "blaze")))
                .row("#").row("T")
                .define("#", Ingredient.of(BASALT))
                .define("T", Ingredient.of(SMOOTH_BASALT))
                .color(Ingredient.of(Tags.Items.DYES_ORANGE))
                .texture(Ingredient.of(Tags.Items.RODS_BLAZE))
                .build());
        make(c,"piglin_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "piglin")))
                .row("#").row("T")
                .define("#", Ingredient.of(BASALT))
                .define("T", Ingredient.of(SMOOTH_BASALT))
                .color(Ingredient.of(Tags.Items.DYES_PINK))
                .texture(Ingredient.of(GOLD_NUGGET))
                .build());
        make(c,"piglin_brute_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "piglin_brute")))
                .row("T").row("T")
                .define("T", Ingredient.of(SMOOTH_BASALT))
                .color(Ingredient.of(Tags.Items.DYES_PINK))
                .texture(Ingredient.of(GOLD_INGOT))
                .build());
        make(c,"zombified_piglin_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "zombified_piglin")))
                .row("#").row("T")
                .define("#", Ingredient.of(BASALT))
                .define("T", Ingredient.of(SMOOTH_BASALT))
                .color(Ingredient.of(Tags.Items.DYES_PINK))
                .build());
        make(c,"wither_skeleton_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "wither_skeleton")))
                .row("#").row("T").row("T")
                .define("#", Ingredient.of(BASALT))
                .define("T", Ingredient.of(SMOOTH_BASALT))
                .color(Ingredient.of(Tags.Items.DYES_BLACK))
                .texture(Ingredient.of(ItemTags.COALS))
                .build());
        make(c,"hoglin_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "hoglin")))
                .row("##").row("TT")
                .define("#", Ingredient.of(BASALT))
                .define("T", Ingredient.of(SMOOTH_BASALT))
                .color(Ingredient.of(Tags.Items.DYES_PINK))
                .texture(Ingredient.of(LEATHER))
                .build());
        make(c,"zoglin_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "zoglin")))
                .row("##").row("TT")
                .define("#", Ingredient.of(BASALT))
                .define("T", Ingredient.of(SMOOTH_BASALT))
                .color(Ingredient.of(Tags.Items.DYES_PINK))
                .texture(Ingredient.of(ROTTEN_FLESH))
                .build());

        //END
        make(c, "enderman_statue_sculpting", Ingredients.Builder.create(
                tagged(t -> t.putString("id", "enderman")))
                .row("#").row("T").row("T")
                .define("#", Ingredient.of(END_STONE))
                .define("T", Ingredient.of(END_STONE_BRICKS))
                .color(Ingredient.of(Tags.Items.DYES_PURPLE))
                .texture(Ingredient.of(ENDER_PEARL))
                .build());
        make(c,"endermite_statue_sculpting", Ingredients.Builder.create(
                        tagged(t -> t.putString("id", "endermite")))
                .row("T")
                .define("T", Ingredient.of(END_STONE))
                .color(Ingredient.of(Tags.Items.DYES_PURPLE))
                .build());

        /* TODO these arent centered, also need to add open variants?
        for (DyeColor color : DyeColor.values()) {
            make(c,"shulker/" + color + "_shulker_statue_sculpting", Ingredients.Builder.create(
                    tagged(t -> {
                        t.putString("id", "shulker");
                        t.putInt("Color", color.getId());
                    }))
                    .row("TT").row("TT")
                    .define("T", Ingredient.of(END_STONE))
                    .color(Ingredient.of(color.getTag()))
                    .texture(Ingredient.of(ENDER_PEARL))
                    .build());
        }
        */
    }

    private void make(Consumer<FinishedRecipe> c, String name, Ingredients ingredients) {
        c.accept(new Result(new SculptingRecipe(MobStatues.getId(name), ingredients)));
    }

    private ItemStack tagged(Consumer<CompoundTag> data) {
        ItemStack ret = new ItemStack(Registration.STATUE_ITEM.get());
        CompoundTag tag = ret.getOrCreateTag();
        data.accept(tag);
        return ret;
    }

    public record Result(SculptingRecipe recipe) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject ret) {
            JsonElement json = Ingredients.CODEC.encodeStart(JsonOps.INSTANCE, recipe.ingredients).getOrThrow(false, s -> {});
            json.getAsJsonObject().entrySet().forEach(e -> ret.add(e.getKey(), e.getValue()));
        }

        @Override
        public ResourceLocation getId() {
            return recipe.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return Registration.SCULPTING_RECIPE_SERIALIZER.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

    private static final Map<Variant, Ingredient> HORSE_MAP = Util.make(new HashMap<>(), map -> {
        map.put(Variant.WHITE, Ingredient.of(Tags.Items.DYES_WHITE));
        map.put(Variant.BROWN, Ingredient.of(Tags.Items.DYES_BROWN));
        map.put(Variant.BLACK, Ingredient.of(Tags.Items.DYES_BLACK));
        map.put(Variant.GRAY, Ingredient.of(Tags.Items.DYES_GRAY));

        map.put(Variant.CREAMY, Ingredient.of(RABBIT_HIDE));
        map.put(Variant.CHESTNUT, Ingredient.of(Tags.Items.INGOTS_COPPER));
        map.put(Variant.DARKBROWN, Ingredient.of(DARK_OAK_TRAPDOOR));
    });

    private static final List<String> LLAMA_VARIANTS_NAMES = ImmutableList.of("creamy", "white", "brown", "gray");

    //0 = Creamy, 1 = White, 2 = Brown, 3 = Gray.
    private static final List<Ingredient> LLAMA_VARIANTS = Util.make(new ArrayList<>(), l -> {
        l.add(Ingredient.of(RABBIT_HIDE));
        l.add(Ingredient.of(Tags.Items.DYES_WHITE));
        l.add(Ingredient.of(Tags.Items.DYES_BROWN));
        l.add(Ingredient.of(Tags.Items.DYES_GRAY));
    });

    private static final List<Block> CARPETS = Util.make(
            () -> ImmutableList.<Block>builder().add(
                            Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET, Blocks.LIGHT_BLUE_CARPET,
                            Blocks.YELLOW_CARPET, Blocks.LIME_CARPET, Blocks.PINK_CARPET, Blocks.GRAY_CARPET, Blocks.LIGHT_GRAY_CARPET,
                            Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET, Blocks.BROWN_CARPET,
                            Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET
                    ).build()
    );

    private static final Map<Axolotl.Variant, Ingredient> AXOLOTL_MAP = Util.make(new HashMap<>(), map -> {
        map.put(Axolotl.Variant.LUCY, Ingredient.of(Tags.Items.DYES_PINK));
        map.put(Axolotl.Variant.WILD, Ingredient.of(Tags.Items.DYES_BROWN));
        map.put(Axolotl.Variant.GOLD, Ingredient.of(Tags.Items.DYES_YELLOW));
        map.put(Axolotl.Variant.CYAN, Ingredient.of(Tags.Items.DYES_CYAN));
        map.put(Axolotl.Variant.BLUE, Ingredient.of(Tags.Items.DYES_BLUE)); //TODO make it a rare villager trade?
    });

    //Order from Cat class
    private static final String[] CAT_TYPE = new String[] {"tabby", "black", "red", "siamese", "british", "calico", "persian", "ragdoll", "white", "jellie", "all_black"};

    private static final Ingredient[] CAT_COLOR = new Ingredient[] {
            Ingredient.of(Tags.Items.DYES_BROWN),
            Ingredient.of(Tags.Items.DYES_BLACK),
            Ingredient.of(Tags.Items.DYES_ORANGE),
            Ingredient.of(SANDSTONE),
            Ingredient.of(Tags.Items.DYES_LIGHT_GRAY),
            Ingredient.of(YELLOW_GLAZED_TERRACOTTA),
            Ingredient.of(SAND),
            Ingredient.of(BONE),
            Ingredient.of(Tags.Items.DYES_WHITE),
            Ingredient.of(Tags.Items.DYES_GRAY),
            Ingredient.of(TINTED_GLASS)
    };

    //Red = 0; Blue = 1; Green = 2; Cyan = 3; Gray = 4
    private static final List<TagKey<Item>> PARROT_COLOR = ImmutableList.of(
            Tags.Items.DYES_RED, Tags.Items.DYES_BLUE, Tags.Items.DYES_GREEN, Tags.Items.DYES_CYAN, Tags.Items.DYES_GRAY
    );

    private static final Map<Integer, Pair<String, Ingredient>> RABBIT_COLOR = Util.make(new HashMap<>(), map -> {
        map.put(0, Pair.of("brown", Ingredient.of(Tags.Items.DYES_BROWN)));
        map.put(1, Pair.of("white", Ingredient.of(Tags.Items.DYES_WHITE)));
        map.put(2, Pair.of("black", Ingredient.of(Tags.Items.DYES_BLACK)));
        map.put(3, Pair.of("black_and_white", Ingredient.of(Tags.Items.DYES_LIGHT_GRAY)));
        map.put(4, Pair.of("gold", Ingredient.of(Tags.Items.DYES_YELLOW)));
        map.put(5, Pair.of("salt_and_pepper", Ingredient.of(Tags.Items.DYES_GRAY)));
        map.put(99, Pair.of("killer", Ingredient.of(SNOWBALL)));
    });
}
