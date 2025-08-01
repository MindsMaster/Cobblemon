/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.ai.ActivityConfigurationContext
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.util.activityRegistry
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.resolve
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Pair
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

class ScriptBehaviourConfig : BehaviourConfig {
    val condition: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val script = cobblemonResource("dummy")
    val variables = mutableListOf<MoLangConfigVariable>()
    val memories = emptySet<MemoryModuleType<*>>()
    val sensors = emptySet<SensorType<*>>()

    override fun getVariables(entity: LivingEntity) = variables

    override fun configure(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) {
        if (!checkCondition(entity, condition)) return
        val runtime = MoLangRuntime().setup()
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())

        val struct = createBrainStruct(entity, behaviourConfigurationContext)
        val script = CobblemonScripts.scripts[this.script]
            ?: run {
                Cobblemon.LOGGER.error("Tried loading script $script as part of an entity brain but that script does not exist")
                return
            }

        behaviourConfigurationContext.addMemories(memories)
        behaviourConfigurationContext.addSensors(sensors)

        runtime.resolve(script, mapOf("brain" to struct))
    }

    fun createBrainStruct(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): QueryStruct {
        return QueryStruct(hashMapOf()).addStandardFunctions()
            .addFunction("entity") { entity.asMostSpecificMoLangValue() }
            .addFunction("create_activity") { params ->
                val name = params.getString(0).asIdentifierDefaultingNamespace()
                val activity = entity.level().activityRegistry.get(name) ?: return@addFunction run {
                    Cobblemon.LOGGER.error("Tried loading activity $name as part of an entity brain but that activity does not exist")
                    DoubleValue.ZERO
                }
                val existingActivityBuilder = behaviourConfigurationContext.activities.find { it.activity == activity }
                if (existingActivityBuilder != null) {
                    return@addFunction createActivityStruct(existingActivityBuilder)
                } else {
                    val activityConfigurationContext = ActivityConfigurationContext(activity)
                    behaviourConfigurationContext.activities.add(activityConfigurationContext)
                    return@addFunction createActivityStruct(activityConfigurationContext)
                }
            }
            .addFunction("set_core_activities") { params ->
                behaviourConfigurationContext.coreActivities = params.params.map { (it as ObjectValue<ActivityConfigurationContext>).obj.activity }.toSet()
                return@addFunction DoubleValue.ONE
            }
            .addFunction("set_default_activity") { params ->
                behaviourConfigurationContext.defaultActivity = params.get<ObjectValue<ActivityConfigurationContext>>(0).obj.activity
                return@addFunction DoubleValue.ONE
            }
    }

    fun createActivityStruct(activityConfigurationContext: ActivityConfigurationContext): ObjectValue<ActivityConfigurationContext> {
        val struct = ObjectValue(activityConfigurationContext)
        struct.addStandardFunctions()
            .addFunction("add_task") { params ->
                val priority = params.getInt(0)
                val task = params.get(1) as ObjectValue<BehaviorControl<in LivingEntity>>
                activityConfigurationContext.tasks.add(Pair(priority, task.obj))
            }
        return struct
    }
}