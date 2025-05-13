package com.cobblemon.mod.common.item.crafting.brewingstand

import com.cobblemon.mod.common.CobblemonRecipeSerializers
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level

class BrewingStandRecipe(
    val groupName: String,
) : Recipe<BrewingStandInput> {

    override fun getType() = CobblemonRecipeTypes.BREWING_STAND
    override fun canCraftInDimensions(width: Int, height: Int) = true
    override fun getSerializer() = CobblemonRecipeSerializers.BREWING_STAND

    override fun matches(
        input: BrewingStandInput,
        level: Level
    ): Boolean {
        return false
    }

    override fun assemble(
        input: BrewingStandInput,
        registries: HolderLookup.Provider
    ): ItemStack? {
        return ItemStack.EMPTY
    }


    override fun getResultItem(registries: HolderLookup.Provider): ItemStack? {
        return ItemStack.EMPTY
    }

    class Serializer : RecipeSerializer<BrewingStandRecipe> {
        companion object {
            val CODEC: MapCodec<BrewingStandRecipe> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter { recipe -> recipe.group },
                ).apply(instance, ::BrewingStandRecipe)
            }


            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, BrewingStandRecipe> =
                StreamCodec.of(::toNetwork, ::fromNetwork)

            private fun fromNetwork(buffer: RegistryFriendlyByteBuf): BrewingStandRecipe {
                val group = buffer.readUtf(32767)
                return BrewingStandRecipe(group)
            }

            private fun toNetwork(buffer: RegistryFriendlyByteBuf, recipe: BrewingStandRecipe) {
                buffer.writeUtf(recipe.groupName)
            }
        }

        override fun codec() = CODEC
        override fun streamCodec() = STREAM_CODEC
    }
}