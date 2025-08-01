/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.group

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.ItemLike
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.resources.ResourceKey
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTab.Output

@Suppress("unused", "UNUSED_PARAMETER")
object CobblemonItemGroups {

    // See https://docs.google.com/spreadsheets/d/1QgaIlW-S9A-Blqhc-5G7OO3igaQdEAiYQqVEPnEBFmc/edit#gid=978365418 for what goes what

    private val ALL = arrayListOf<ItemGroupHolder>()
    private val INJECTORS = hashMapOf<ResourceKey<CreativeModeTab>, (injector: Injector) -> Unit>()

    @JvmStatic val BLOCKS_KEY = this.create("blocks", this::blockEntries) {
        ItemStack(CobblemonItems.PC)
    }
    @JvmStatic val UTILITY_ITEMS_KEY = this.create("utility_item", this::utilityItemEntries) {
        ItemStack(CobblemonItems.POKE_BALL)
    }
    @JvmStatic val AGRICULTURE_KEY = this.create("agriculture", this::agricultureEntries) {
        ItemStack(CobblemonItems.MEDICINAL_LEEK)
    }
    @JvmStatic val ARCHAEOLOGY_KEY = this.create("archaeology", this::archaeologyEntries) {
        ItemStack(CobblemonItems.HELIX_FOSSIL)
    }
    @JvmStatic val CONSUMABLES_KEY = this.create("consumables", this::consumableEntries) {
        ItemStack(CobblemonItems.ROASTED_LEEK)
    }
    @JvmStatic val HELD_ITEMS_KEY = this.create("held_item", this::heldItemEntries) {
        ItemStack(CobblemonItems.EXP_SHARE)
    }
    @JvmStatic val EVOLUTION_ITEMS_KEY = this.create("evolution_item", this::evolutionItemEntries) {
        ItemStack(CobblemonItems.BLACK_AUGURITE)
    }

    @JvmStatic val BLOCKS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(BLOCKS_KEY)
    @JvmStatic val UTILITY_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(UTILITY_ITEMS_KEY)
    @JvmStatic val AGRICULTURE get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(AGRICULTURE_KEY)
    @JvmStatic val ARCHAEOLOGY get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(ARCHAEOLOGY_KEY)
    @JvmStatic val CONSUMABLES get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(CONSUMABLES_KEY)
    @JvmStatic val HELD_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(HELD_ITEMS_KEY)
    @JvmStatic val EVOLUTION_ITEMS get() = BuiltInRegistries.CREATIVE_MODE_TAB.get(EVOLUTION_ITEMS_KEY)

    @JvmStatic val BUILDING_BLOCKS_INJECTIONS = this.inject(ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.parse("building_blocks")), this::blocksInjections)
    @JvmStatic val FOOD_INJECTIONS = this.inject(ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.parse("food_and_drinks")), this::foodInjections)
    @JvmStatic val TOOLS_AND_UTILITIES_INJECTIONS = this.inject(ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.parse("tools_and_utilities")), this::toolsAndUtilitiesInjections)
    @JvmStatic val INGREDIENTS_INJECTIONS = this.inject(ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.parse("ingredients")), this::ingredientsInjections)
    @JvmStatic val OP_BLOCKS_INJECTIONS = this.inject(ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.parse("op_blocks")), this::opBlocksInjections)

    fun register(consumer: (holder: ItemGroupHolder) -> CreativeModeTab) {
        ALL.forEach(consumer::invoke)
    }

    fun inject(tabKey: ResourceKey<CreativeModeTab>, injector: Injector) {
        INJECTORS[tabKey]?.invoke(injector)
    }

    fun injectorKeys(): Collection<ResourceKey<CreativeModeTab>> = this.INJECTORS.keys

    data class ItemGroupHolder(
        val key: ResourceKey<CreativeModeTab>,
        val displayIconProvider: () -> ItemStack,
        val entryCollector: DisplayItemsGenerator,
        val displayName: Component = Component.translatable("itemGroup.${key.location().namespace}.${key.location().path}")
    )

    private fun create(name: String, entryCollector: DisplayItemsGenerator, displayIconProvider: () -> ItemStack): ResourceKey<CreativeModeTab> {
        val key = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), cobblemonResource(name))
        this.ALL += ItemGroupHolder(key, displayIconProvider, entryCollector)
        return key
    }

    private fun inject(key: ResourceKey<CreativeModeTab>, consumer: (injector: Injector) -> Unit): (injector: Injector) -> Unit {
        this.INJECTORS[key] = consumer
        return consumer
    }

    private fun agricultureEntries(displayContext: ItemDisplayParameters, entries: Output) {
        entries.accept(CobblemonItems.MEDICINAL_LEEK)
        entries.accept(CobblemonItems.BIG_ROOT)
        entries.accept(CobblemonItems.ENERGY_ROOT)
        entries.accept(CobblemonItems.REVIVAL_HERB)
        entries.accept(CobblemonItems.PEP_UP_FLOWER)
        entries.accept(CobblemonItems.MENTAL_HERB)
        entries.accept(CobblemonItems.POWER_HERB)
        entries.accept(CobblemonItems.WHITE_HERB)
        entries.accept(CobblemonItems.MIRROR_HERB)
        entries.accept(CobblemonItems.VIVICHOKE)
        entries.accept(CobblemonItems.VIVICHOKE_SEEDS)
        entries.accept(CobblemonItems.GALARICA_NUTS)
        entries.accept(CobblemonItems.HEARTY_GRAINS)
        entries.accept(CobblemonItems.TASTY_TAIL)
        entries.accept(CobblemonItems.BUGWORT)

        entries.accept(CobblemonItems.RED_APRICORN)
        entries.accept(CobblemonItems.YELLOW_APRICORN)
        entries.accept(CobblemonItems.GREEN_APRICORN)
        entries.accept(CobblemonItems.BLUE_APRICORN)
        entries.accept(CobblemonItems.PINK_APRICORN)
        entries.accept(CobblemonItems.BLACK_APRICORN)
        entries.accept(CobblemonItems.WHITE_APRICORN)
        entries.accept(CobblemonItems.RED_APRICORN_SEED)
        entries.accept(CobblemonItems.YELLOW_APRICORN_SEED)
        entries.accept(CobblemonItems.GREEN_APRICORN_SEED)
        entries.accept(CobblemonItems.BLUE_APRICORN_SEED)
        entries.accept(CobblemonItems.PINK_APRICORN_SEED)
        entries.accept(CobblemonItems.BLACK_APRICORN_SEED)
        entries.accept(CobblemonItems.WHITE_APRICORN_SEED)

        entries.accept(CobblemonItems.SACCHARINE_SAPLING)

        entries.accept(CobblemonItems.RED_MINT_SEEDS)
        entries.accept(CobblemonItems.RED_MINT_LEAF)
        entries.accept(CobblemonItems.BLUE_MINT_SEEDS)
        entries.accept(CobblemonItems.BLUE_MINT_LEAF)
        entries.accept(CobblemonItems.CYAN_MINT_SEEDS)
        entries.accept(CobblemonItems.CYAN_MINT_LEAF)
        entries.accept(CobblemonItems.PINK_MINT_SEEDS)
        entries.accept(CobblemonItems.PINK_MINT_LEAF)
        entries.accept(CobblemonItems.GREEN_MINT_SEEDS)
        entries.accept(CobblemonItems.GREEN_MINT_LEAF)
        entries.accept(CobblemonItems.WHITE_MINT_SEEDS)
        entries.accept(CobblemonItems.WHITE_MINT_LEAF)

        entries.accept(CobblemonItems.GROWTH_MULCH)
        entries.accept(CobblemonItems.RICH_MULCH)
        entries.accept(CobblemonItems.SURPRISE_MULCH)
        entries.accept(CobblemonItems.LOAMY_MULCH)
        entries.accept(CobblemonItems.COARSE_MULCH)
        entries.accept(CobblemonItems.PEAT_MULCH)
        entries.accept(CobblemonItems.HUMID_MULCH)
        entries.accept(CobblemonItems.SANDY_MULCH)
        entries.accept(CobblemonItems.MULCH_BASE)

        CobblemonItems.berries().values.forEach(entries::accept)
    }

    private fun archaeologyEntries(displayContext: ItemDisplayParameters, entries: Output) {
        entries.accept(CobblemonItems.HELIX_FOSSIL)
        entries.accept(CobblemonItems.DOME_FOSSIL)
        entries.accept(CobblemonItems.OLD_AMBER_FOSSIL)
        entries.accept(CobblemonItems.ROOT_FOSSIL)
        entries.accept(CobblemonItems.CLAW_FOSSIL)
        entries.accept(CobblemonItems.SKULL_FOSSIL)
        entries.accept(CobblemonItems.ARMOR_FOSSIL)
        entries.accept(CobblemonItems.COVER_FOSSIL)
        entries.accept(CobblemonItems.PLUME_FOSSIL)
        entries.accept(CobblemonItems.JAW_FOSSIL)
        entries.accept(CobblemonItems.SAIL_FOSSIL)
        entries.accept(CobblemonItems.FOSSILIZED_BIRD)
        entries.accept(CobblemonItems.FOSSILIZED_FISH)
        entries.accept(CobblemonItems.FOSSILIZED_DRAKE)
        entries.accept(CobblemonItems.FOSSILIZED_DINO)

        entries.accept(CobblemonItems.TUMBLESTONE)
        entries.accept(CobblemonItems.BLACK_TUMBLESTONE)
        entries.accept(CobblemonItems.SKY_TUMBLESTONE)

        entries.accept(CobblemonItems.SMALL_BUDDING_TUMBLESTONE)
        entries.accept(CobblemonItems.SMALL_BUDDING_BLACK_TUMBLESTONE)
        entries.accept(CobblemonItems.SMALL_BUDDING_SKY_TUMBLESTONE)

        entries.accept(CobblemonItems.MEDIUM_BUDDING_TUMBLESTONE)
        entries.accept(CobblemonItems.MEDIUM_BUDDING_BLACK_TUMBLESTONE)
        entries.accept(CobblemonItems.MEDIUM_BUDDING_SKY_TUMBLESTONE)

        entries.accept(CobblemonItems.LARGE_BUDDING_TUMBLESTONE)
        entries.accept(CobblemonItems.LARGE_BUDDING_BLACK_TUMBLESTONE)
        entries.accept(CobblemonItems.LARGE_BUDDING_SKY_TUMBLESTONE)

        entries.accept(CobblemonItems.TUMBLESTONE_CLUSTER)
        entries.accept(CobblemonItems.BLACK_TUMBLESTONE_CLUSTER)
        entries.accept(CobblemonItems.SKY_TUMBLESTONE_CLUSTER)

        entries.accept(CobblemonItems.TUMBLESTONE_BLOCK)
        entries.accept(CobblemonItems.BLACK_TUMBLESTONE_BLOCK)
        entries.accept(CobblemonItems.SKY_TUMBLESTONE_BLOCK)

        entries.accept(CobblemonItems.BYGONE_SHERD)
        entries.accept(CobblemonItems.CAPTURE_SHERD)
        entries.accept(CobblemonItems.DOME_SHERD)
        entries.accept(CobblemonItems.HELIX_SHERD)
        entries.accept(CobblemonItems.NOSTALGIC_SHERD)
        entries.accept(CobblemonItems.SUSPICIOUS_SHERD)

        entries.accept(CobblemonItems.AUTOMATON_ARMOR_TRIM_SMITHING_TEMPLATE)

        entries.accept(CobblemonItems.RELIC_COIN)
        entries.accept(CobblemonItems.RELIC_COIN_POUCH)
        entries.accept(CobblemonItems.RELIC_COIN_SACK)
        entries.accept(CobblemonItems.GILDED_CHEST)
        entries.accept(CobblemonItems.YELLOW_GILDED_CHEST)
        entries.accept(CobblemonItems.GREEN_GILDED_CHEST)
        entries.accept(CobblemonItems.BLUE_GILDED_CHEST)
        entries.accept(CobblemonItems.PINK_GILDED_CHEST)
        entries.accept(CobblemonItems.BLACK_GILDED_CHEST)
        entries.accept(CobblemonItems.WHITE_GILDED_CHEST)
        entries.accept(CobblemonItems.GIMMIGHOUL_CHEST)

        entries.accept(CobblemonItems.NORMAL_GEM)
        entries.accept(CobblemonItems.FIRE_GEM)
        entries.accept(CobblemonItems.WATER_GEM)
        entries.accept(CobblemonItems.GRASS_GEM)
        entries.accept(CobblemonItems.ELECTRIC_GEM)
        entries.accept(CobblemonItems.ICE_GEM)
        entries.accept(CobblemonItems.FIGHTING_GEM)
        entries.accept(CobblemonItems.POISON_GEM)
        entries.accept(CobblemonItems.GROUND_GEM)
        entries.accept(CobblemonItems.FLYING_GEM)
        entries.accept(CobblemonItems.PSYCHIC_GEM)
        entries.accept(CobblemonItems.BUG_GEM)
        entries.accept(CobblemonItems.ROCK_GEM)
        entries.accept(CobblemonItems.GHOST_GEM)
        entries.accept(CobblemonItems.DRAGON_GEM)
        entries.accept(CobblemonItems.DARK_GEM)
        entries.accept(CobblemonItems.STEEL_GEM)
        entries.accept(CobblemonItems.FAIRY_GEM)
    }

    private fun blockEntries(displayContext: ItemDisplayParameters, entries: Output) {
        entries.accept(CobblemonItems.RESTORATION_TANK)
        entries.accept(CobblemonItems.FOSSIL_ANALYZER)
        entries.accept(CobblemonItems.MONITOR)
        entries.accept(CobblemonItems.PC)
        entries.accept(CobblemonItems.HEALING_MACHINE)
        entries.accept(CobblemonItems.PASTURE)

        entries.accept(CobblemonItems.GILDED_CHEST)
        entries.accept(CobblemonItems.YELLOW_GILDED_CHEST)
        entries.accept(CobblemonItems.GREEN_GILDED_CHEST)
        entries.accept(CobblemonItems.BLUE_GILDED_CHEST)
        entries.accept(CobblemonItems.PINK_GILDED_CHEST)
        entries.accept(CobblemonItems.BLACK_GILDED_CHEST)
        entries.accept(CobblemonItems.WHITE_GILDED_CHEST)
        entries.accept(CobblemonItems.GIMMIGHOUL_CHEST)
        entries.accept(CobblemonItems.RELIC_COIN_POUCH)
        entries.accept(CobblemonItems.RELIC_COIN_SACK)

        entries.accept(CobblemonItems.INCENSE_SWEET)
        entries.accept(CobblemonItems.DISPLAY_CASE)
        entries.accept(CobblemonItems.APRICORN_LOG)
        entries.accept(CobblemonItems.APRICORN_WOOD)
        entries.accept(CobblemonItems.STRIPPED_APRICORN_LOG)
        entries.accept(CobblemonItems.STRIPPED_APRICORN_WOOD)
        entries.accept(CobblemonItems.APRICORN_PLANKS)
        entries.accept(CobblemonItems.APRICORN_STAIRS)
        entries.accept(CobblemonItems.APRICORN_SLAB)
        entries.accept(CobblemonItems.APRICORN_FENCE)
        entries.accept(CobblemonItems.APRICORN_FENCE_GATE)
        entries.accept(CobblemonItems.APRICORN_DOOR)
        entries.accept(CobblemonItems.APRICORN_TRAPDOOR)
        entries.accept(CobblemonItems.APRICORN_BUTTON)
        entries.accept(CobblemonItems.APRICORN_PRESSURE_PLATE)
        entries.accept(CobblemonItems.APRICORN_SIGN)
        entries.accept(CobblemonItems.APRICORN_HANGING_SIGN)
        entries.accept(CobblemonItems.APRICORN_LEAVES)

        entries.accept(CobblemonItems.SACCHARINE_LOG)
        entries.accept(CobblemonItems.SACCHARINE_WOOD)
        entries.accept(CobblemonItems.STRIPPED_SACCHARINE_LOG)
        entries.accept(CobblemonItems.STRIPPED_SACCHARINE_WOOD)
        entries.accept(CobblemonItems.SACCHARINE_PLANKS)
        entries.accept(CobblemonItems.SACCHARINE_STAIRS)
        entries.accept(CobblemonItems.SACCHARINE_SLAB)
        entries.accept(CobblemonItems.SACCHARINE_FENCE)
        entries.accept(CobblemonItems.SACCHARINE_FENCE_GATE)
        entries.accept(CobblemonItems.SACCHARINE_DOOR)
        entries.accept(CobblemonItems.SACCHARINE_TRAPDOOR)
        entries.accept(CobblemonItems.SACCHARINE_BUTTON)
        entries.accept(CobblemonItems.SACCHARINE_PRESSURE_PLATE)
        entries.accept(CobblemonItems.SACCHARINE_SIGN)
        entries.accept(CobblemonItems.SACCHARINE_HANGING_SIGN)
        entries.accept(CobblemonItems.SACCHARINE_LEAVES)
        //entries.accept(CobblemonItems.SACCHARINE_SAPLING)

        entries.accept(CobblemonItems.HEARTY_GRAIN_BALE)

        entries.accept(CobblemonItems.TATAMI_BLOCK)
        entries.accept(CobblemonItems.TATAMI_MAT)

        entries.accept(CobblemonItems.TUMBLESTONE_BLOCK)
        entries.accept(CobblemonItems.BLACK_TUMBLESTONE_BLOCK)
        entries.accept(CobblemonItems.SKY_TUMBLESTONE_BLOCK)

        entries.accept(CobblemonItems.POLISHED_TUMBLESTONE)
        entries.accept(CobblemonItems.POLISHED_TUMBLESTONE_STAIRS)
        entries.accept(CobblemonItems.POLISHED_TUMBLESTONE_SLAB)
        entries.accept(CobblemonItems.POLISHED_TUMBLESTONE_WALL)
        entries.accept(CobblemonItems.CHISELED_POLISHED_TUMBLESTONE)
        entries.accept(CobblemonItems.TUMBLESTONE_BRICKS)
        entries.accept(CobblemonItems.TUMBLESTONE_BRICK_STAIRS)
        entries.accept(CobblemonItems.TUMBLESTONE_BRICK_SLAB)
        entries.accept(CobblemonItems.TUMBLESTONE_BRICK_WALL)
        entries.accept(CobblemonItems.CHISELED_TUMBLESTONE_BRICKS)
        entries.accept(CobblemonItems.SMOOTH_TUMBLESTONE)
        entries.accept(CobblemonItems.SMOOTH_TUMBLESTONE_STAIRS)
        entries.accept(CobblemonItems.SMOOTH_TUMBLESTONE_SLAB)
        entries.accept(CobblemonItems.POLISHED_BLACK_TUMBLESTONE)
        entries.accept(CobblemonItems.POLISHED_BLACK_TUMBLESTONE_STAIRS)
        entries.accept(CobblemonItems.POLISHED_BLACK_TUMBLESTONE_SLAB)
        entries.accept(CobblemonItems.POLISHED_BLACK_TUMBLESTONE_WALL)
        entries.accept(CobblemonItems.CHISELED_POLISHED_BLACK_TUMBLESTONE)
        entries.accept(CobblemonItems.BLACK_TUMBLESTONE_BRICKS)
        entries.accept(CobblemonItems.BLACK_TUMBLESTONE_BRICK_STAIRS)
        entries.accept(CobblemonItems.BLACK_TUMBLESTONE_BRICK_SLAB)
        entries.accept(CobblemonItems.BLACK_TUMBLESTONE_BRICK_WALL)
        entries.accept(CobblemonItems.CHISELED_BLACK_TUMBLESTONE_BRICKS)
        entries.accept(CobblemonItems.SMOOTH_BLACK_TUMBLESTONE)
        entries.accept(CobblemonItems.SMOOTH_BLACK_TUMBLESTONE_STAIRS)
        entries.accept(CobblemonItems.SMOOTH_BLACK_TUMBLESTONE_SLAB)
        entries.accept(CobblemonItems.POLISHED_SKY_TUMBLESTONE)
        entries.accept(CobblemonItems.POLISHED_SKY_TUMBLESTONE_STAIRS)
        entries.accept(CobblemonItems.POLISHED_SKY_TUMBLESTONE_SLAB)
        entries.accept(CobblemonItems.POLISHED_SKY_TUMBLESTONE_WALL)
        entries.accept(CobblemonItems.CHISELED_POLISHED_SKY_TUMBLESTONE)
        entries.accept(CobblemonItems.SKY_TUMBLESTONE_BRICKS)
        entries.accept(CobblemonItems.SKY_TUMBLESTONE_BRICK_STAIRS)
        entries.accept(CobblemonItems.SKY_TUMBLESTONE_BRICK_SLAB)
        entries.accept(CobblemonItems.SKY_TUMBLESTONE_BRICK_WALL)
        entries.accept(CobblemonItems.CHISELED_SKY_TUMBLESTONE_BRICKS)
        entries.accept(CobblemonItems.SMOOTH_SKY_TUMBLESTONE)
        entries.accept(CobblemonItems.SMOOTH_SKY_TUMBLESTONE_STAIRS)
        entries.accept(CobblemonItems.SMOOTH_SKY_TUMBLESTONE_SLAB)

        entries.accept(CobblemonItems.DAWN_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_DAWN_STONE_ORE)
        entries.accept(CobblemonItems.DUSK_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_DUSK_STONE_ORE)
        entries.accept(CobblemonItems.FIRE_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_FIRE_STONE_ORE)
        entries.accept(CobblemonItems.NETHER_FIRE_STONE_ORE)
        entries.accept(CobblemonItems.ICE_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_ICE_STONE_ORE)
        entries.accept(CobblemonItems.LEAF_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_LEAF_STONE_ORE)
        entries.accept(CobblemonItems.MOON_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_MOON_STONE_ORE)
        entries.accept(CobblemonItems.DRIPSTONE_MOON_STONE_ORE)
        entries.accept(CobblemonItems.SHINY_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_SHINY_STONE_ORE)
        entries.accept(CobblemonItems.SUN_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_SUN_STONE_ORE)
        entries.accept(CobblemonItems.TERRACOTTA_SUN_STONE_ORE)
        entries.accept(CobblemonItems.THUNDER_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_THUNDER_STONE_ORE)
        entries.accept(CobblemonItems.WATER_STONE_ORE)
        entries.accept(CobblemonItems.DEEPSLATE_WATER_STONE_ORE)

        entries.accept(CobblemonItems.DAWN_STONE_BLOCK)
        entries.accept(CobblemonItems.DUSK_STONE_BLOCK)
        entries.accept(CobblemonItems.FIRE_STONE_BLOCK)
        entries.accept(CobblemonItems.ICE_STONE_BLOCK)
        entries.accept(CobblemonItems.LEAF_STONE_BLOCK)
        entries.accept(CobblemonItems.MOON_STONE_BLOCK)
        entries.accept(CobblemonItems.SHINY_STONE_BLOCK)
        entries.accept(CobblemonItems.SUN_STONE_BLOCK)
        entries.accept(CobblemonItems.THUNDER_STONE_BLOCK)
        entries.accept(CobblemonItems.WATER_STONE_BLOCK)

        CobblemonItems.campfire_pots.forEach(entries::accept)
    }

    private fun consumableEntries(displayContext: ItemDisplayParameters, entries: Output) {
        entries.accept(CobblemonItems.ROASTED_LEEK)
        entries.accept(CobblemonItems.LEEK_AND_POTATO_STEW)
        entries.accept(CobblemonItems.VIVICHOKE_DIP)
        entries.accept(CobblemonItems.TART_APPLE)
        entries.accept(CobblemonItems.SWEET_APPLE)
        entries.accept(CobblemonItems.SYRUPY_APPLE)
        entries.accept(CobblemonItems.WHIPPED_DREAM)
        entries.accept(CobblemonItems.STRAWBERRY_SWEET)
        entries.accept(CobblemonItems.LOVE_SWEET)
        entries.accept(CobblemonItems.BERRY_SWEET)
        entries.accept(CobblemonItems.CLOVER_SWEET)
        entries.accept(CobblemonItems.FLOWER_SWEET)
        entries.accept(CobblemonItems.STAR_SWEET)
        entries.accept(CobblemonItems.RIBBON_SWEET)

        entries.accept(CobblemonItems.CANDIED_APPLE)
        entries.accept(CobblemonItems.CANDIED_BERRY)
        entries.accept(CobblemonItems.POTATO_MOCHI)
        entries.accept(CobblemonItems.PONIGIRI)
        entries.accept(CobblemonItems.SINISTER_TEA)

        entries.accept(CobblemonItems.PEWTER_CRUNCHIES)
        entries.accept(CobblemonItems.RAGE_CANDY_BAR)
        entries.accept(CobblemonItems.LAVA_COOKIE)
        entries.accept(CobblemonItems.JUBILIFE_MUFFIN)
        entries.accept(CobblemonItems.OLD_GATEAU)
        entries.accept(CobblemonItems.CASTELIACONE)
        entries.accept(CobblemonItems.LUMIOSE_GALETTE)
        entries.accept(CobblemonItems.BIG_MALASADA)
        entries.accept(CobblemonItems.SMOKED_TAIL_CURRY)
        entries.accept(CobblemonItems.OPEN_FACED_SANDWICH)

        entries.accept(CobblemonItems.BERRY_JUICE)
        entries.accept(CobblemonItems.REMEDY)
        entries.accept(CobblemonItems.FINE_REMEDY)
        entries.accept(CobblemonItems.SUPERB_REMEDY)
        entries.accept(CobblemonItems.HEAL_POWDER)
        entries.accept(CobblemonItems.MOOMOO_MILK)
        entries.accept(CobblemonItems.MEDICINAL_BREW)

        entries.accept(CobblemonItems.POTION)
        entries.accept(CobblemonItems.SUPER_POTION)
        entries.accept(CobblemonItems.HYPER_POTION)
        entries.accept(CobblemonItems.MAX_POTION)
        entries.accept(CobblemonItems.FULL_RESTORE)

        entries.accept(CobblemonItems.ANTIDOTE)
        entries.accept(CobblemonItems.AWAKENING)
        entries.accept(CobblemonItems.BURN_HEAL)
        entries.accept(CobblemonItems.ICE_HEAL)
        entries.accept(CobblemonItems.PARALYZE_HEAL)

        entries.accept(CobblemonItems.FULL_HEAL)

        entries.accept(CobblemonItems.ETHER)
        entries.accept(CobblemonItems.MAX_ETHER)
        entries.accept(CobblemonItems.ELIXIR)
        entries.accept(CobblemonItems.MAX_ELIXIR)

        entries.accept(CobblemonItems.REVIVE)
        entries.accept(CobblemonItems.MAX_REVIVE)

        entries.accept(CobblemonItems.X_ATTACK)
        entries.accept(CobblemonItems.X_DEFENSE)
        entries.accept(CobblemonItems.X_SP_ATK)
        entries.accept(CobblemonItems.X_SP_DEF)
        entries.accept(CobblemonItems.X_SPEED)
        entries.accept(CobblemonItems.X_ACCURACY)

        entries.accept(CobblemonItems.DIRE_HIT)
        entries.accept(CobblemonItems.GUARD_SPEC)

        entries.accept(CobblemonItems.HEALTH_FEATHER)
        entries.accept(CobblemonItems.MUSCLE_FEATHER)
        entries.accept(CobblemonItems.RESIST_FEATHER)
        entries.accept(CobblemonItems.GENIUS_FEATHER)
        entries.accept(CobblemonItems.CLEVER_FEATHER)
        entries.accept(CobblemonItems.SWIFT_FEATHER)

        entries.accept(CobblemonItems.HEALTH_MOCHI)
        entries.accept(CobblemonItems.MUSCLE_MOCHI)
        entries.accept(CobblemonItems.RESIST_MOCHI)
        entries.accept(CobblemonItems.GENIUS_MOCHI)
        entries.accept(CobblemonItems.CLEVER_MOCHI)
        entries.accept(CobblemonItems.SWIFT_MOCHI)
        entries.accept(CobblemonItems.FRESH_START_MOCHI)

        entries.accept(CobblemonItems.HP_UP)
        entries.accept(CobblemonItems.PROTEIN)
        entries.accept(CobblemonItems.IRON)
        entries.accept(CobblemonItems.CALCIUM)
        entries.accept(CobblemonItems.ZINC)
        entries.accept(CobblemonItems.CARBOS)
        entries.accept(CobblemonItems.PP_UP)
        entries.accept(CobblemonItems.PP_MAX)

        entries.accept(CobblemonItems.EXPERIENCE_CANDY_XS)
        entries.accept(CobblemonItems.EXPERIENCE_CANDY_S)
        entries.accept(CobblemonItems.EXPERIENCE_CANDY_M)
        entries.accept(CobblemonItems.EXPERIENCE_CANDY_L)
        entries.accept(CobblemonItems.EXPERIENCE_CANDY_XL)
        entries.accept(CobblemonItems.RARE_CANDY)

        entries.accept(CobblemonItems.HEALTH_CANDY)
        entries.accept(CobblemonItems.MIGHTY_CANDY)
        entries.accept(CobblemonItems.TOUGH_CANDY)
        entries.accept(CobblemonItems.SMART_CANDY)
        entries.accept(CobblemonItems.COURAGE_CANDY)
        entries.accept(CobblemonItems.QUICK_CANDY)
        entries.accept(CobblemonItems.SICKLY_CANDY)
        entries.accept(CobblemonItems.WEAK_CANDY)
        entries.accept(CobblemonItems.BRITTLE_CANDY)
        entries.accept(CobblemonItems.NUMB_CANDY)
        entries.accept(CobblemonItems.COWARD_CANDY)
        entries.accept(CobblemonItems.SLOW_CANDY)

        entries.accept(CobblemonItems.LONELY_MINT)
        entries.accept(CobblemonItems.ADAMANT_MINT)
        entries.accept(CobblemonItems.NAUGHTY_MINT)
        entries.accept(CobblemonItems.BRAVE_MINT)
        entries.accept(CobblemonItems.BOLD_MINT)
        entries.accept(CobblemonItems.IMPISH_MINT)
        entries.accept(CobblemonItems.LAX_MINT)
        entries.accept(CobblemonItems.RELAXED_MINT)
        entries.accept(CobblemonItems.MODEST_MINT)
        entries.accept(CobblemonItems.MILD_MINT)
        entries.accept(CobblemonItems.RASH_MINT)
        entries.accept(CobblemonItems.QUIET_MINT)
        entries.accept(CobblemonItems.CALM_MINT)
        entries.accept(CobblemonItems.GENTLE_MINT)
        entries.accept(CobblemonItems.CAREFUL_MINT)
        entries.accept(CobblemonItems.SASSY_MINT)
        entries.accept(CobblemonItems.TIMID_MINT)
        entries.accept(CobblemonItems.HASTY_MINT)
        entries.accept(CobblemonItems.JOLLY_MINT)
        entries.accept(CobblemonItems.NAIVE_MINT)
        entries.accept(CobblemonItems.SERIOUS_MINT)

        entries.accept(CobblemonItems.ABILITY_CAPSULE)
        entries.accept(CobblemonItems.ABILITY_PATCH)

        entries.accept(CobblemonItems.POKE_BAIT)
        entries.accept(CobblemonItems.LURE_CAKE)
        entries.accept(CobblemonItems.POKE_CAKE)
        entries.accept(CobblemonItems.POKE_PUFF)

        entries.accept(CobblemonItems.APRIJUICE_RED)
        entries.accept(CobblemonItems.APRIJUICE_YELLOW)
        entries.accept(CobblemonItems.APRIJUICE_GREEN)
        entries.accept(CobblemonItems.APRIJUICE_BLUE)
        entries.accept(CobblemonItems.APRIJUICE_PINK)
        entries.accept(CobblemonItems.APRIJUICE_BLACK)
        entries.accept(CobblemonItems.APRIJUICE_WHITE)

    }

    private fun evolutionItemEntries(displayContext: ItemDisplayParameters, entries: Output) {
        entries.accept(CobblemonItems.FIRE_STONE)
        entries.accept(CobblemonItems.WATER_STONE)
        entries.accept(CobblemonItems.THUNDER_STONE)
        entries.accept(CobblemonItems.LEAF_STONE)
        entries.accept(CobblemonItems.MOON_STONE)
        entries.accept(CobblemonItems.SUN_STONE)
        entries.accept(CobblemonItems.SHINY_STONE)
        entries.accept(CobblemonItems.DUSK_STONE)
        entries.accept(CobblemonItems.DAWN_STONE)
        entries.accept(CobblemonItems.ICE_STONE)
        entries.accept(CobblemonItems.LINK_CABLE)
        entries.accept(CobblemonItems.KINGS_ROCK)
        entries.accept(CobblemonItems.GALARICA_CUFF)
        entries.accept(CobblemonItems.GALARICA_WREATH)
        entries.accept(CobblemonItems.METAL_COAT)
        entries.accept(CobblemonItems.BLACK_AUGURITE)
        entries.accept(CobblemonItems.PROTECTOR)
        entries.accept(CobblemonItems.OVAL_STONE)
        entries.accept(CobblemonItems.DRAGON_SCALE)
        entries.accept(CobblemonItems.ELECTIRIZER)
        entries.accept(CobblemonItems.MAGMARIZER)
        entries.accept(CobblemonItems.UPGRADE)
        entries.accept(CobblemonItems.DUBIOUS_DISC)
        entries.accept(CobblemonItems.RAZOR_FANG)
        entries.accept(CobblemonItems.RAZOR_CLAW)
        entries.accept(CobblemonItems.PEAT_BLOCK)
        entries.accept(CobblemonItems.PRISM_SCALE)
        entries.accept(CobblemonItems.REAPER_CLOTH)
        entries.accept(CobblemonItems.DEEP_SEA_TOOTH)
        entries.accept(CobblemonItems.DEEP_SEA_SCALE)
        entries.accept(CobblemonItems.SACHET)
        entries.accept(CobblemonItems.WHIPPED_DREAM)
        entries.accept(CobblemonItems.TART_APPLE)
        entries.accept(CobblemonItems.SWEET_APPLE)
        entries.accept(CobblemonItems.SYRUPY_APPLE)
        entries.accept(CobblemonItems.CRACKED_POT)
        entries.accept(CobblemonItems.CHIPPED_POT)
        entries.accept(CobblemonItems.MASTERPIECE_TEACUP)
        entries.accept(CobblemonItems.UNREMARKABLE_TEACUP)
        entries.accept(CobblemonItems.STRAWBERRY_SWEET)
        entries.accept(CobblemonItems.LOVE_SWEET)
        entries.accept(CobblemonItems.BERRY_SWEET)
        entries.accept(CobblemonItems.CLOVER_SWEET)
        entries.accept(CobblemonItems.FLOWER_SWEET)
        entries.accept(CobblemonItems.STAR_SWEET)
        entries.accept(CobblemonItems.RIBBON_SWEET)
        entries.accept(CobblemonItems.AUSPICIOUS_ARMOR)
        entries.accept(CobblemonItems.MALICIOUS_ARMOR)
        entries.accept(CobblemonItems.SHELL_HELMET)
        entries.accept(CobblemonItems.METAL_ALLOY)
        entries.accept(CobblemonItems.SCROLL_OF_DARKNESS)
        entries.accept(CobblemonItems.SCROLL_OF_WATERS)
    }

    private fun heldItemEntries(displayContext: ItemDisplayParameters, entries: Output) {
        entries.accept(CobblemonItems.ABILITY_SHIELD)
        entries.accept(CobblemonItems.ABSORB_BULB)
        entries.accept(CobblemonItems.AIR_BALLOON)
        entries.accept(CobblemonItems.ASSAULT_VEST)
        entries.accept(CobblemonItems.BIG_ROOT)
        entries.accept(CobblemonItems.BINDING_BAND)
        entries.accept(CobblemonItems.BLACK_BELT)
        entries.accept(CobblemonItems.BLACK_GLASSES)
        entries.accept(CobblemonItems.BLACK_SLUDGE)
        entries.accept(CobblemonItems.BLUNDER_POLICY)
        entries.accept(CobblemonItems.BRIGHT_POWDER)
        entries.accept(CobblemonItems.CELL_BATTERY)
        entries.accept(CobblemonItems.CHARCOAL)
        entries.accept(CobblemonItems.CHOICE_BAND)
        entries.accept(CobblemonItems.CHOICE_SCARF)
        entries.accept(CobblemonItems.CHOICE_SPECS)
        entries.accept(CobblemonItems.CLEANSE_TAG)
        entries.accept(CobblemonItems.CLEAR_AMULET)
        entries.accept(CobblemonItems.COVERT_CLOAK)
        entries.accept(CobblemonItems.DAMP_ROCK)
        entries.accept(CobblemonItems.DEEP_SEA_SCALE)
        entries.accept(CobblemonItems.DEEP_SEA_TOOTH)
        entries.accept(CobblemonItems.DESTINY_KNOT)
        entries.accept(CobblemonItems.DRAGON_FANG)
        entries.accept(CobblemonItems.EJECT_BUTTON)
        entries.accept(CobblemonItems.EJECT_PACK)
        entries.accept(CobblemonItems.EVERSTONE)
        entries.accept(CobblemonItems.EVIOLITE)
        entries.accept(CobblemonItems.EXPERT_BELT)
        entries.accept(CobblemonItems.EXP_SHARE)
        entries.accept(CobblemonItems.FAIRY_FEATHER)
        entries.accept(CobblemonItems.FLAME_ORB)
        entries.accept(CobblemonItems.FLOAT_STONE)
        entries.accept(CobblemonItems.FOCUS_BAND)
        entries.accept(CobblemonItems.FOCUS_SASH)
        entries.accept(CobblemonItems.GRIP_CLAW)
        entries.accept(CobblemonItems.HARD_STONE)
        entries.accept(CobblemonItems.HEAT_ROCK)
        entries.accept(CobblemonItems.HEAVY_DUTY_BOOTS)
        entries.accept(CobblemonItems.ICY_ROCK)
        entries.accept(CobblemonItems.IRON_BALL)
        entries.accept(CobblemonItems.KINGS_ROCK)
        entries.accept(CobblemonItems.LAGGING_TAIL)
        entries.accept(CobblemonItems.LEFTOVERS)
        entries.accept(CobblemonItems.LIFE_ORB)
        entries.accept(CobblemonItems.LIGHT_BALL)
        entries.accept(CobblemonItems.LIGHT_CLAY)
        entries.accept(CobblemonItems.LOADED_DICE)
        entries.accept(CobblemonItems.LUCKY_EGG)
        entries.accept(CobblemonItems.LUMINOUS_MOSS)
        entries.accept(CobblemonItems.MAGNET)
        entries.accept(CobblemonItems.MENTAL_HERB)
        entries.accept(CobblemonItems.METAL_COAT)
        entries.accept(CobblemonItems.METAL_POWDER)
        entries.accept(CobblemonItems.METRONOME)
        entries.accept(CobblemonItems.MIRACLE_SEED)
        entries.accept(CobblemonItems.MIRROR_HERB)
        entries.accept(CobblemonItems.MUSCLE_BAND)
        entries.accept(CobblemonItems.MYSTIC_WATER)
        entries.accept(CobblemonItems.NEVER_MELT_ICE)
        entries.accept(CobblemonItems.POISON_BARB)
        entries.accept(CobblemonItems.POWER_ANKLET)
        entries.accept(CobblemonItems.POWER_BAND)
        entries.accept(CobblemonItems.POWER_BELT)
        entries.accept(CobblemonItems.POWER_BRACER)
        entries.accept(CobblemonItems.POWER_LENS)
        entries.accept(CobblemonItems.POWER_WEIGHT)
        entries.accept(CobblemonItems.POWER_HERB)
        entries.accept(CobblemonItems.PROTECTIVE_PADS)
        entries.accept(CobblemonItems.PUNCHING_GLOVE)
        entries.accept(CobblemonItems.QUICK_CLAW)
        entries.accept(CobblemonItems.QUICK_POWDER)
        entries.accept(CobblemonItems.RAZOR_CLAW)
        entries.accept(CobblemonItems.RAZOR_FANG)
        entries.accept(CobblemonItems.RED_CARD)
        entries.accept(CobblemonItems.RING_TARGET)
        entries.accept(CobblemonItems.ROCKY_HELMET)
        entries.accept(CobblemonItems.ROOM_SERVICE)
        entries.accept(CobblemonItems.SAFETY_GOGGLES)
        entries.accept(CobblemonItems.SCOPE_LENS)
        entries.accept(CobblemonItems.SHARP_BEAK)
        entries.accept(CobblemonItems.SHED_SHELL)
        entries.accept(CobblemonItems.SHELL_BELL)
        entries.accept(CobblemonItems.SILK_SCARF)
        entries.accept(CobblemonItems.SILVER_POWDER)
        entries.accept(CobblemonItems.SMOKE_BALL)
        entries.accept(CobblemonItems.SMOOTH_ROCK)
        entries.accept(CobblemonItems.SOFT_SAND)
        entries.accept(CobblemonItems.SOOTHE_BELL)
        entries.accept(CobblemonItems.SPELL_TAG)
        entries.accept(CobblemonItems.STICKY_BARB)
        entries.accept(CobblemonItems.TERRAIN_EXTENDER)
        entries.accept(CobblemonItems.THROAT_SPRAY)
        entries.accept(CobblemonItems.TOXIC_ORB)
        entries.accept(CobblemonItems.TWISTED_SPOON)
        entries.accept(CobblemonItems.UTILITY_UMBRELLA)
        entries.accept(CobblemonItems.WEAKNESS_POLICY)
        entries.accept(CobblemonItems.WHITE_HERB)
        entries.accept(CobblemonItems.WIDE_LENS)
        entries.accept(CobblemonItems.WISE_GLASSES)
        entries.accept(CobblemonItems.ZOOM_LENS)

        entries.accept(CobblemonItems.MEDICINAL_LEEK)
        entries.accept(Items.BONE)
        entries.accept(Items.SNOWBALL)
        entries.accept(Items.GOLD_BLOCK)

        entries.accept(CobblemonItems.NORMAL_GEM)
        entries.accept(CobblemonItems.FIRE_GEM)
        entries.accept(CobblemonItems.WATER_GEM)
        entries.accept(CobblemonItems.GRASS_GEM)
        entries.accept(CobblemonItems.ELECTRIC_GEM)
        entries.accept(CobblemonItems.ICE_GEM)
        entries.accept(CobblemonItems.FIGHTING_GEM)
        entries.accept(CobblemonItems.POISON_GEM)
        entries.accept(CobblemonItems.GROUND_GEM)
        entries.accept(CobblemonItems.FLYING_GEM)
        entries.accept(CobblemonItems.PSYCHIC_GEM)
        entries.accept(CobblemonItems.BUG_GEM)
        entries.accept(CobblemonItems.ROCK_GEM)
        entries.accept(CobblemonItems.GHOST_GEM)
        entries.accept(CobblemonItems.DRAGON_GEM)
        entries.accept(CobblemonItems.DARK_GEM)
        entries.accept(CobblemonItems.STEEL_GEM)
        entries.accept(CobblemonItems.FAIRY_GEM)
        entries.accept(CobblemonItems.ELECTRIC_SEED)
        entries.accept(CobblemonItems.GRASSY_SEED)
        entries.accept(CobblemonItems.MISTY_SEED)
        entries.accept(CobblemonItems.PSYCHIC_SEED)
    }

    private fun utilityItemEntries(displayContext: ItemDisplayParameters, entries: Output) {
        CobblemonItems.pokeBalls.forEach(entries::accept)
        CobblemonItems.pokeRods.forEach(entries::accept)
        CobblemonItems.pokedexes.forEach(entries::accept)
    }

    private fun blocksInjections(injector: Injector) {
        if(Cobblemon.implementation.isModInstalled("adorn")) { //adorn modifies vanilla block tab and if we don't follow suit we crash the game
            injector.putLast(CobblemonItems.APRICORN_LOG)
            injector.putLast(CobblemonItems.APRICORN_WOOD)
            injector.putLast(CobblemonItems.STRIPPED_APRICORN_LOG)
            injector.putLast(CobblemonItems.STRIPPED_APRICORN_WOOD)
            injector.putLast(CobblemonItems.APRICORN_PLANKS)
            injector.putLast(CobblemonItems.APRICORN_STAIRS)
            injector.putLast(CobblemonItems.APRICORN_SLAB)
            injector.putLast(CobblemonItems.APRICORN_FENCE)
            injector.putLast(CobblemonItems.APRICORN_FENCE_GATE)
            injector.putLast(CobblemonItems.APRICORN_DOOR)
            injector.putLast(CobblemonItems.APRICORN_TRAPDOOR)
            injector.putLast(CobblemonItems.APRICORN_BUTTON)
            injector.putLast(CobblemonItems.APRICORN_PRESSURE_PLATE)
        }
    }

    private fun foodInjections(injector: Injector) {
        injector.putAfter(CobblemonItems.TART_APPLE, Items.ENCHANTED_GOLDEN_APPLE)
        injector.putAfter(CobblemonItems.SWEET_APPLE, CobblemonItems.TART_APPLE)
        injector.putAfter(CobblemonItems.SYRUPY_APPLE, CobblemonItems.SWEET_APPLE)

        injector.putAfter(CobblemonItems.MEDICINAL_LEEK, Items.POISONOUS_POTATO)
        injector.putAfter(CobblemonItems.ROASTED_LEEK, CobblemonItems.MEDICINAL_LEEK)

        injector.putAfter(CobblemonItems.WHIPPED_DREAM, Items.PUMPKIN_PIE)
        injector.putAfter(CobblemonItems.STRAWBERRY_SWEET, CobblemonItems.WHIPPED_DREAM)
        injector.putAfter(CobblemonItems.LOVE_SWEET, CobblemonItems.STRAWBERRY_SWEET)
        injector.putAfter(CobblemonItems.BERRY_SWEET, CobblemonItems.LOVE_SWEET)
        injector.putAfter(CobblemonItems.CLOVER_SWEET, CobblemonItems.BERRY_SWEET)
        injector.putAfter(CobblemonItems.FLOWER_SWEET, CobblemonItems.CLOVER_SWEET)
        injector.putAfter(CobblemonItems.STAR_SWEET, CobblemonItems.FLOWER_SWEET)
        injector.putAfter(CobblemonItems.RIBBON_SWEET, CobblemonItems.STAR_SWEET)

        injector.putAfter(CobblemonItems.CANDIED_APPLE, CobblemonItems.RIBBON_SWEET)
        injector.putAfter(CobblemonItems.CANDIED_BERRY, CobblemonItems.CANDIED_APPLE)
        injector.putAfter(CobblemonItems.POTATO_MOCHI, CobblemonItems.CANDIED_BERRY)
        injector.putAfter(CobblemonItems.PONIGIRI, CobblemonItems.POTATO_MOCHI)

        injector.putAfter(CobblemonItems.PEWTER_CRUNCHIES, CobblemonItems.PONIGIRI)
        injector.putAfter(CobblemonItems.RAGE_CANDY_BAR, CobblemonItems.PEWTER_CRUNCHIES)
        injector.putAfter(CobblemonItems.LAVA_COOKIE, CobblemonItems.RAGE_CANDY_BAR)
        injector.putAfter(CobblemonItems.JUBILIFE_MUFFIN, CobblemonItems.LAVA_COOKIE)
        injector.putAfter(CobblemonItems.OLD_GATEAU, CobblemonItems.JUBILIFE_MUFFIN)
        injector.putAfter(CobblemonItems.CASTELIACONE, CobblemonItems.OLD_GATEAU)
        injector.putAfter(CobblemonItems.LUMIOSE_GALETTE, CobblemonItems.CASTELIACONE)
        injector.putAfter(CobblemonItems.BIG_MALASADA, CobblemonItems.LUMIOSE_GALETTE)
        injector.putAfter(CobblemonItems.SMOKED_TAIL_CURRY, CobblemonItems.BIG_MALASADA)
        injector.putAfter(CobblemonItems.OPEN_FACED_SANDWICH, CobblemonItems.SMOKED_TAIL_CURRY)

        injector.putAfter(CobblemonItems.LEEK_AND_POTATO_STEW, Items.RABBIT_STEW)
        injector.putAfter(CobblemonItems.VIVICHOKE_DIP, CobblemonItems.LEEK_AND_POTATO_STEW)

        injector.putAfter(CobblemonItems.SINISTER_TEA, Items.MILK_BUCKET)
    }

    private fun toolsAndUtilitiesInjections(injector: Injector) {
        injector.putAfter(CobblemonItems.APRICORN_BOAT, Items.BAMBOO_CHEST_RAFT)
        injector.putAfter(CobblemonItems.APRICORN_CHEST_BOAT, CobblemonItems.APRICORN_BOAT)
        injector.putAfter(CobblemonItems.SACCHARINE_BOAT, CobblemonItems.APRICORN_CHEST_BOAT)
        injector.putAfter(CobblemonItems.SACCHARINE_CHEST_BOAT, CobblemonItems.SACCHARINE_BOAT)
    }

    private fun ingredientsInjections(injector: Injector) {
        injector.putAfter(CobblemonItems.BYGONE_SHERD, Items.SNORT_POTTERY_SHERD)
        injector.putAfter(CobblemonItems.CAPTURE_SHERD, CobblemonItems.BYGONE_SHERD)
        injector.putAfter(CobblemonItems.DOME_SHERD, CobblemonItems.CAPTURE_SHERD)
        injector.putAfter(CobblemonItems.HELIX_SHERD, CobblemonItems.DOME_SHERD)
        injector.putAfter(CobblemonItems.NOSTALGIC_SHERD, CobblemonItems.HELIX_SHERD)
        injector.putAfter(CobblemonItems.SUSPICIOUS_SHERD, CobblemonItems.NOSTALGIC_SHERD)

        injector.putAfter(CobblemonItems.AUTOMATON_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE)
        injector.putAfter(CobblemonItems.POKEROD_SMITHING_TEMPLATE, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
    }

    private fun opBlocksInjections(injector: Injector) {
        injector.putLast(CobblemonItems.NPC_EDITOR)
    }

    /**
     * The abstract behaviour of injecting into existing [CreativeModeTab]s in specific positions.
     * Each platform has to implement the behaviour.
     */
    interface Injector {

        /**
         * Places the given [item] at the start of a creative tab.
         *
         * @param item The [ItemLike] being added at the start of a tab.
         */
        fun putFirst(item: ItemLike)

        /**
         * Places the given [item] before the [target].
         * If the [target] is not present behaves as [putLast].
         *
         * @param item The [ItemLike] being added before [target].
         * @param target The [ItemLike] being targeted.
         */
        fun putBefore(item: ItemLike, target: ItemLike)

        /**
         * Places the given [item] after the [target].
         * If the [target] is not present behaves as [putLast].
         *
         * @param item The [ItemLike] being added after [target].
         * @param target The [ItemLike] being targeted.
         */
        fun putAfter(item: ItemLike, target: ItemLike)

        /**
         * Places the given [item] at the end of a creative tab.
         *
         * @param item The [ItemLike] being added at the end of a tab.
         */
        fun putLast(item: ItemLike)

    }

}
