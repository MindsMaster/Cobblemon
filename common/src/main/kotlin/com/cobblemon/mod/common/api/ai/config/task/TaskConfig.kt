/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.Expression
import com.bedrockk.molang.ast.NumberExpression
import com.bedrockk.molang.ast.StringExpression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.resolveString
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

/**
 * A configuration for a brain task. Its purpose is to generate a list of tasks to add to the brain of
 * an entity when it spawns.
 *
 * This is essentially a builder for tasks.
 *
 * @author Hiroku
 * @since October 14th, 2024
 */
interface TaskConfig {
    companion object {
        val types = mutableMapOf<ResourceLocation, Class<out TaskConfig>>(
            cobblemonResource("one_of") to OneOfTaskConfig::class.java,
            cobblemonResource("wander") to WanderTaskConfig::class.java,
            cobblemonResource("water_wander") to WaterWanderTaskConfig::class.java,
            cobblemonResource("air_wander") to AirWanderTaskConfig::class.java,
            cobblemonResource("look_at_target") to LookAtTargetTaskConfig::class.java,
            cobblemonResource("follow_walk_target") to FollowWalkTargetTaskConfig::class.java,
            cobblemonResource("random") to RandomTaskConfig::class.java,
            cobblemonResource("stay_afloat") to StayAfloatTaskConfig::class.java,
            cobblemonResource("look_at_entities") to LookAtEntitiesTaskConfig::class.java,
            cobblemonResource("do_nothing") to DoNothingTaskConfig::class.java,
            cobblemonResource("get_angry_at_attacker") to GetAngryAtAttackerTaskConfig::class.java,
            cobblemonResource("stop_being_angry_if_attacker_dead") to StopBeingAngryIfAttackerDeadTaskConfig::class.java,
            cobblemonResource("switch_npc_to_battle") to SwitchToNPCBattleTaskConfig::class.java,
            cobblemonResource("look_at_battling_pokemon") to LookAtBattlingPokemonTaskConfig::class.java,
            cobblemonResource("switch_npc_from_battle") to SwitchFromNPCBattleTaskConfig::class.java,
            cobblemonResource("switch_pokemon_to_battle") to SwitchToPokemonBattleTaskConfig::class.java,
            cobblemonResource("look_at_targeted_battle_pokemon") to LookAtTargetedBattlePokemonTaskConfig::class.java,
            cobblemonResource("switch_pokemon_from_battle") to SwitchFromPokemonBattleTaskConfig::class.java,
            cobblemonResource("go_to_healing_machine") to GoToHealingMachineTaskConfig::class.java,
            cobblemonResource("heal_using_healing_machine") to HealUsingHealingMachineTaskConfig::class.java,
            cobblemonResource("all_of") to AllOfTaskConfig::class.java,
            cobblemonResource("attack_angry_at") to AttackAngryAtTaskConfig::class.java,
            cobblemonResource("move_to_attack_target") to MoveToAttackTargetTaskConfig::class.java,
            cobblemonResource("melee_attack") to MeleeAttackTaskConfig::class.java,
            cobblemonResource("switch_from_fight") to SwitchFromFightTaskConfig::class.java,
            cobblemonResource("switch_to_fight") to SwitchToFightTaskConfig::class.java,
            cobblemonResource("switch_to_chatting") to SwitchToChattingTaskConfig::class.java,
            cobblemonResource("switch_from_chatting") to SwitchFromChattingTaskConfig::class.java,
            cobblemonResource("look_at_speaker") to LookAtSpeakerTaskConfig::class.java,
            cobblemonResource("switch_to_action_effect") to SwitchToActionEffectTaskConfig::class.java,
            cobblemonResource("switch_from_action_effect") to SwitchFromActionEffectTaskConfig::class.java,
            cobblemonResource("exit_battle_when_hurt") to ExitBattleWhenHurtTaskConfig::class.java,
            cobblemonResource("switch_to_panic_when_hurt") to SwitchToPanicWhenHurtTaskConfig::class.java,
            cobblemonResource("switch_to_panic_when_hostiles_nearby") to SwitchToPanicWhenHostilesNearbyTaskConfig::class.java,
            cobblemonResource("calm_down") to CalmDownTaskConfig::class.java,
            cobblemonResource("flee_attacker") to FleeAttackerTaskConfig::class.java,
            cobblemonResource("flee_nearest_hostile") to FleeNearestHostileTaskConfig::class.java,
            cobblemonResource("run_script") to RunScript::class.java,
            cobblemonResource("look_in_direction") to LookInDirectionTaskConfig::class.java,
            cobblemonResource("wake_up") to WakeUpTaskConfig::class.java,
            cobblemonResource("go_to_sleep") to GoToSleepTaskConfig::class.java,
            cobblemonResource("find_resting_place") to FindRestingPlaceTaskConfig::class.java,
            cobblemonResource("move_to_owner") to MoveToOwnerTaskConfig::class.java,
            cobblemonResource("switch_to_sleep_on_trainer_bed") to SwitchToSleepOnTrainerBedTaskConfig::class.java,
            cobblemonResource("switch_from_sleep_on_trainer_bed") to SwitchFromSleepOnTrainerBedTaskConfig::class.java,
            cobblemonResource("sleep_if_on_trainer_bed") to SleepIfOnTrainerBedTaskConfig::class.java,
            cobblemonResource("point_to_spawn") to PointToSpawnTaskConfig::class.java,
            cobblemonResource("eat_grass") to EatGrassTaskConfig::class.java,
            cobblemonResource("find_air") to FindAirTaskConfig::class.java,
            cobblemonResource("go_to_land") to GoToLandTaskConfig::class.java,
            cobblemonResource("battle_flight_task") to BattleFlightTaskConfig::class.java
        )
        
        val runtime = MoLangRuntime().setup()
    }

    val runtime: MoLangRuntime
        get() = Companion.runtime

    fun checkCondition(entity: LivingEntity, expressionOrEntityVariable: ExpressionOrEntityVariable): Boolean {
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        return expressionOrEntityVariable.resolveBoolean()
    }

    fun ExpressionOrEntityVariable.asSimplifiedExpression(entity: LivingEntity): Expression {
        return map(
            { it },
            {
                if (entity is MoLangScriptingEntity) {
                    val variable = entity.config.map[it.variableName]
                    if (variable is DoubleValue) {
                        return@map NumberExpression(variable.value)
                    } else if (variable is StringValue) {
                        return@map StringExpression(variable)
                    }
                }
                return@map "q.entity.config.${it.variableName}".asExpression()
            }
        )
    }

    fun ExpressionOrEntityVariable.asExpression() = map({ it }, { "q.entity.config.${it.variableName}".asExpression() })
    fun ExpressionOrEntityVariable.resolveString() = runtime.resolveString(asExpression())
    fun ExpressionOrEntityVariable.resolveBoolean() = runtime.resolveBoolean(asExpression())
    fun ExpressionOrEntityVariable.resolveInt() = runtime.resolveInt(asExpression())
    fun ExpressionOrEntityVariable.resolveDouble() = runtime.resolveDouble(asExpression())
    fun ExpressionOrEntityVariable.resolveFloat() = runtime.resolveFloat(asExpression())

    private fun variable(category: String, name: String, type: MoLangConfigVariable.MoLangVariableType, default: String) = MoLangConfigVariable(
        variableName = name,
        category = lang("entity.variable.category.$category"),
        displayName = lang("entity.variable.$name.name"),
        description = lang("entity.variable.$name.desc"),
        type = type,
        defaultValue = default
    )

    fun stringVariable(category: String, name: String, default: String) = variable(category = category, name = name, type = MoLangConfigVariable.MoLangVariableType.TEXT, default = default)
    fun numberVariable(category: String, name: String, default: Number) = variable(category = category, name = name, type = MoLangConfigVariable.MoLangVariableType.NUMBER, default = default.toString())
    fun booleanVariable(category: String, name: String, default: Boolean) = variable(category = category, name = name, type = MoLangConfigVariable.MoLangVariableType.BOOLEAN, default = default.toString())

    fun getVariableExpression(name: String) = "q.entity.config.$name".asExpression()
    fun resolveStringVariable(name: String) = runtime.resolveString(getVariableExpression(name))
    fun resolveBooleanVariable(name: String) = runtime.resolveBoolean(getVariableExpression(name))
    fun resolveNumberVariable(name: String) = runtime.resolveDouble(getVariableExpression(name))

    /** The variables that this task config uses. These are used to declare variables on the entity cleanly. */
    fun getVariables(entity: LivingEntity): List<MoLangConfigVariable>
    /** Given the entity in construction, returns a list of tasks. */
    fun createTasks(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<BehaviorControl<in LivingEntity>>
}