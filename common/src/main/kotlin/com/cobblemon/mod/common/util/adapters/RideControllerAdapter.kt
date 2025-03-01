/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.pokemon.riding.controllers.BirdAirController
import com.cobblemon.mod.common.pokemon.riding.controllers.FallToFlightCompositeController
import com.cobblemon.mod.common.pokemon.riding.controllers.GenericLandController
import com.cobblemon.mod.common.pokemon.riding.controllers.GenericLiquidController
import com.cobblemon.mod.common.pokemon.riding.controllers.GliderAirController
import com.cobblemon.mod.common.pokemon.riding.controllers.HelicopterAirController
import com.cobblemon.mod.common.pokemon.riding.controllers.RunUpToFlightCompositeController
import com.cobblemon.mod.common.pokemon.riding.controllers.SwimDashController
import com.cobblemon.mod.common.pokemon.riding.controllers.VehicleLandController
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import net.minecraft.resources.ResourceLocation

/**
 * Adapter for deserializing [com.cobblemon.mod.common.api.riding.controller.RideController] types.
 *
 * @author Hiroku
 * @since April 30th, 2024
 */
object RideControllerAdapter : JsonDeserializer<RideController> {
    val types: MutableMap<ResourceLocation, Class<out RideController>> = mutableMapOf(
        GenericLandController.Companion.KEY to GenericLandController::class.java,
        GenericLiquidController.Companion.KEY to GenericLiquidController::class.java,
        SwimDashController.Companion.KEY to SwimDashController::class.java,
        RunUpToFlightCompositeController.Companion.KEY to RunUpToFlightCompositeController::class.java,
        BirdAirController.Companion.KEY to BirdAirController::class.java,
        HelicopterAirController.Companion.KEY to HelicopterAirController::class.java,
        GliderAirController.Companion.KEY to GliderAirController::class.java,
        FallToFlightCompositeController.Companion.KEY to FallToFlightCompositeController::class.java,
        VehicleLandController.Companion.KEY to VehicleLandController::class.java
    )

    override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): RideController {
        val root = element.asJsonObject
        val key = root.get("key").asString
        val controllerType = types[key.asIdentifierDefaultingNamespace()] ?: throw IllegalArgumentException("Unknown controller: $key")
        return context.deserialize(element, controllerType)
    }
}