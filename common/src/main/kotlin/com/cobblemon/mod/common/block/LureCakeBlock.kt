/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BaitConsumedEvent
import com.cobblemon.mod.common.api.events.fishing.BaitSetEvent
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.block.entity.LureCakeBlockEntity
import com.cobblemon.mod.common.item.RodBaitComponent
import com.cobblemon.mod.common.item.components.CookingComponent
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import kotlin.collections.flatten

class LureCakeBlock(settings: BlockBehaviour.Properties): Block(settings) {

    companion object {
        val AGE: IntegerProperty = BlockStateProperties.AGE_5

        fun getBaitOnLureCake(stack: ItemStack): FishingBait? {
            return getCookingComponentOnRod(stack) ?: stack.components.get(CobblemonItemComponents.BAIT)?.bait
        }

        fun getBaitStackOnRod(stack: ItemStack): ItemStack {
            return stack.components.get(CobblemonItemComponents.BAIT)?.stack ?: ItemStack.EMPTY
        }

        fun getCookingComponentOnRod(rodStack: ItemStack): FishingBait? {
            // Check if the stack within the RodBaitComponent has a CookingComponent
            val cookingComponent = rodStack.get(CobblemonItemComponents.COOKING_COMPONENT) ?: return null

            // Combine effects from the CookingComponent
            val combinedEffects = listOf(
                cookingComponent.bait1.effects,
                cookingComponent.bait2.effects,
                cookingComponent.bait3.effects
            ).flatten()

            // Return a new FishingBait with combined effects
            return FishingBait(
                item = BuiltInRegistries.ITEM.getKey(
                    rodStack.components.get(CobblemonItemComponents.BAIT)?.stack?.item ?: ItemStack.EMPTY.item
                ), // Use the rodStack's item as the bait identifier
                effects = combinedEffects
            )
        }


        fun setBait(stack: ItemStack, bait: ItemStack) {
            CobblemonEvents.BAIT_SET.postThen(BaitSetEvent(stack, bait), { event -> }, {
                if (bait.isEmpty) {
                    stack.set<RodBaitComponent>(CobblemonItemComponents.BAIT, null)
                    stack.set<CookingComponent>(CobblemonItemComponents.COOKING_COMPONENT, null)
                    return
                }

                // Retrieve FishingBait and CookingComponent from the bait ItemStack
                val fishingBait = FishingBaits.getFromBaitItemStack(bait) ?: return
                val cookingComponent = bait.get(CobblemonItemComponents.COOKING_COMPONENT)

                // Apply both RodBaitComponent and CookingComponent to the rod ItemStack
                stack.set(CobblemonItemComponents.BAIT, RodBaitComponent(fishingBait, bait))
                if (cookingComponent != null) {
                    stack.set(CobblemonItemComponents.COOKING_COMPONENT, cookingComponent)
                } else {
                    // Clear CookingComponent if the new bait does not have it
                    stack.set<CookingComponent>(CobblemonItemComponents.COOKING_COMPONENT, null)
                }
            })
        }


        fun consumeBait(stack: ItemStack) {
            CobblemonEvents.BAIT_CONSUMED.postThen(BaitConsumedEvent(stack), { event -> }, {
                val baitStack = getBaitStackOnRod(stack)
                val baitCount = baitStack.count
                val cookingComponent = stack.get(CobblemonItemComponents.COOKING_COMPONENT)

                if (baitCount == 1) {
                    stack.set<RodBaitComponent>(CobblemonItemComponents.BAIT, null)
                    stack.set<CookingComponent>(CobblemonItemComponents.COOKING_COMPONENT, null)
                    return
                }

                if (baitCount > 1) {
                    val fishingBait = getBaitOnLureCake(stack) ?: return
                    stack.set<RodBaitComponent>(
                        CobblemonItemComponents.BAIT,
                        RodBaitComponent(fishingBait, ItemStack(baitStack.item, baitCount - 1))
                    )
                    if (cookingComponent != null) {
                        stack.set(CobblemonItemComponents.COOKING_COMPONENT, cookingComponent)
                    }
                }
            })
        }



        fun getBaitEffects(stack: ItemStack): List<FishingBait.Effect> {
            return getBaitOnLureCake(stack)?.effects ?: return emptyList()
        }
    }

    init {
        // Default state with age set to 0
        registerDefaultState(this.stateDefinition.any().setValue(AGE, 0))
    }

    override fun setPlacedBy(
            world: Level,
            pos: BlockPos,
            state: BlockState,
            placer: LivingEntity?,
            stack: ItemStack
    ) {
        super.setPlacedBy(world, pos, state, placer, stack)
        val blockEntity = world.getBlockEntity(pos) as? LureCakeBlockEntity ?: return
        blockEntity.initializeFromItemStack(stack)
    }

    override fun getCloneItemStack(level: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        val blockEntity = level.getBlockEntity(pos) as? LureCakeBlockEntity ?: return ItemStack.EMPTY
        return blockEntity.toItemStack()
    }

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        // Check if the block is at max age
        if (state.getValue(AGE) < 5) {
            // Increment the age and update the block state
            world.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2)
        }
    }

    override fun isRandomlyTicking(state: BlockState): Boolean {
        // Enable random ticking for this block
        return true
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        // Register the AGE property
        builder.add(AGE)
    }

    /*// Fishing Rod: Bundle edition
    override fun overrideOtherStackedOnMe(
        itemStack: ItemStack,
        itemStack2: ItemStack,
        slot: Slot,
        clickAction: ClickAction,
        player: Player,
        slotAccess: SlotAccess
    ): Boolean {
        if (clickAction != ClickAction.SECONDARY || !slot.allowModification(player))
            return false

        val baitStack = getBaitStackOnRod(itemStack)

        CobblemonEvents.BAIT_SET_PRE.postThen(BaitSetEvent(itemStack, itemStack2), { event ->
            return event.isCanceled
        }, {

            // If not holding an item on cursor
            if (itemStack2.isEmpty) {
                // Retrieve bait onto cursor
                if (baitStack != ItemStack.EMPTY) {
                    playDetachSound(player)
                    setBait(itemStack, ItemStack.EMPTY)
                    slotAccess.set(baitStack.copy())
                    return true
                }
            }
            // If holding item on cursor
            else {

                // If item on cursor is a valid bait
                if (FishingBaits.getFromBaitItemStack(itemStack2) != null) {

                    // Add as much as possible
                    if (baitStack != ItemStack.EMPTY) {
                        if (baitStack.item == itemStack2.item) {

                            playAttachSound(player)
                            // Calculate how much bait to add
                            val diff = (baitStack.maxStackSize - baitStack.count).coerceIn(0, itemStack2.count)
                            itemStack2.shrink(diff)
                            baitStack.grow(diff)
                            setBait(itemStack, baitStack)
                            return true
                        }

                        // If Item on rod is different from cursor item, swap them
                        playAttachSound(player)
                        setBait(itemStack, itemStack2.copy())
                        slotAccess.set(baitStack.copy())
                        return true
                    }

                    // If no bait currently on rod, add all
                    playAttachSound(player)
                    setBait(itemStack, itemStack2.copy())
                    itemStack2.shrink(itemStack2.count)
                    return true
                }
            }
        })
        return false
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {

        val itemStack = user.getItemInHand(hand)
        val offHandItem = user.getItemInHand(InteractionHand.OFF_HAND)
        val offHandBait = FishingBaits.getFromBaitItemStack(offHandItem)

        var baitOnRod = getBaitOnRod(itemStack)

        // Check if offhand item is valid bait and the rod is not in use, if so then apply bait from offhand
        if (!world.isClientSide && user.fishing == null && offHandBait != null) {
            CobblemonEvents.BAIT_SET_PRE.postThen(BaitSetEvent(itemStack, offHandItem), { event ->
                return InteractionResultHolder.fail(itemStack)
            }, {
                playAttachSound(user)

                // if there is bait on the rod already then drop it on the ground before applying offhand bait
                val baitStack = baitOnRod?.toItemStack(world.itemRegistry)

                if (baitStack != null && offHandItem.item != baitStack.item) {
                    if (!baitStack.isEmpty) {
                        baitStack.count = getBaitStackOnRod(itemStack).count
                        user.drop(baitStack, true) // Drop the full stack
                    }

                    // apply single bait item from offhand
                    val singleBait = offHandItem.copy()
                    singleBait.count = 1
                    setBait(itemStack, singleBait)
                    offHandItem.shrink(1)
                }
            })
        }

        var i: Int
        if (user.fishing != null) { // if the bobber is out yet
            if (!world.isClientSide) {
                CobblemonEvents.POKEROD_REEL.postThen(
                    PokerodReelEvent(itemStack),
                    { event -> return InteractionResultHolder.fail(itemStack) },
                    { event ->
                        i = user.fishing!!.retrieve(itemStack)
                        itemStack.hurtAndBreak(i, user, hand.toEquipmentSlot())
                        world.playSoundServer(
                            Vec3(
                                user.x,
                                user.y,
                                user.z
                            ),
                            CobblemonSounds.FISHING_ROD_REEL_IN,
                            SoundSource.PLAYERS,
                            1.0f,
                            1.0f / (world.getRandom().nextFloat() * 0.4f + 0.8f)
                        )
                    }
                )
            }


            world.playSound(null as Player?, user.x, user.y, user.z, CobblemonSounds.FISHING_ROD_REEL_IN, SoundSource.PLAYERS, 1.0f, 1.0f)
            user.gameEvent(GameEvent.ITEM_INTERACT_FINISH)
        } else { // if the bobber is not out yet

            if (!world.isClientSide) {
                val lureLevel = world.enchantmentRegistry.getHolder(Enchantments.LURE).map { EnchantmentHelper.getItemEnchantmentLevel(it, itemStack) }.orElse(0)
                val luckLevel = world.enchantmentRegistry.getHolder(Enchantments.LUCK_OF_THE_SEA).map { EnchantmentHelper.getItemEnchantmentLevel(it, itemStack) }.orElse(0)

                val bobberEntity = PokeRodFishingBobberEntity(
                    user,
                    pokeRodId,
                    getBaitOnRod(itemStack)?.toItemStack(world.itemRegistry) ?: ItemStack.EMPTY,
                    world,
                    luckLevel,
                    lureLevel,
                    itemStack
                )
                CobblemonEvents.POKEROD_CAST_PRE.postThen(
                    PokerodCastEvent.Pre(itemStack, bobberEntity, getBaitStackOnRod(itemStack)),
                    { event -> return InteractionResultHolder.fail(itemStack) },
                    { event ->
                        world.addFreshEntity(bobberEntity)
                        var baitId = getBaitOnRod(itemStack)?.item ?: cobblemonResource("empty_bait")
                        CobblemonCriteria.CAST_POKE_ROD.trigger(user as ServerPlayer, CastPokeRodContext(baitId))

                        CobblemonEvents.POKEROD_CAST_POST.post(
                            PokerodCastEvent.Post(itemStack, bobberEntity, getBaitStackOnRod(itemStack))
                        )
                    }
                )
            }

            user.awardStat(Stats.ITEM_USED.get(this))
            user.gameEvent(GameEvent.ITEM_INTERACT_START)
        }
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide)
    }*/

    private fun playAttachSound(entity: Entity) {
        entity.playSound(CobblemonSounds.FISHING_BAIT_ATTACH, 1F, 1F)
    }

    private fun playDetachSound(entity: Entity) {
        entity.playSound(CobblemonSounds.FISHING_BAIT_DETACH, 1F, 1F)
    }


}
