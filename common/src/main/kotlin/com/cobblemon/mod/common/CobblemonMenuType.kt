package com.cobblemon.mod.common


import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotMenu
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotScreen
import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType

object CobblemonMenuType : PlatformRegistry<Registry<MenuType<*>>, ResourceKey<Registry<MenuType<*>>>, MenuType<*>>() {

    val COOKING_POT = MenuType.register("cooking_pot", ::CookingPotMenu)

    init {
        MenuScreens.register(COOKING_POT, ::CookingPotScreen)
    }

    override val registry: Registry<MenuType<*>>
        get() = BuiltInRegistries.MENU
    override val resourceKey: ResourceKey<Registry<MenuType<*>>>
        get() = Registries.MENU
}