package com.cobblemon.mod.common.api.riding.behaviour

import com.cobblemon.mod.common.api.riding.behaviour.types.*
import net.minecraft.resources.ResourceLocation

object RidingBehaviours {
    val behaviours = mutableMapOf<ResourceLocation, RidingBehaviour<*, *>>(
        BirdAirBehaviour.KEY to BirdAirBehaviour(),
        DolphinBehaviour.KEY to DolphinBehaviour(),
        FallToGlideCompositeBehaviour.KEY to FallToGlideCompositeBehaviour(),
        GenericLandBehaviour.KEY to GenericLandBehaviour(),
        GenericSwimBehaviour.KEY to GenericSwimBehaviour(),
        GliderAirBehaviour.KEY to GliderAirBehaviour(),
        HelicopterBehaviour.KEY to HelicopterBehaviour(),
        JetAirBehaviour.KEY to JetAirBehaviour(),
        JumpToFlightCompositeBehaviour.KEY to JumpToFlightCompositeBehaviour(),
        RunToJetCompositeBehaviour.KEY to RunToJetCompositeBehaviour(),
        SwimDashBehaviour.KEY to SwimDashBehaviour(),
        VehicleLandBehaviour.KEY to VehicleLandBehaviour(),
    )

    init {
        register(BirdAirBehaviour.KEY, BirdAirBehaviour())
        register(DolphinBehaviour.KEY, DolphinBehaviour())
        register(FallToGlideCompositeBehaviour.KEY, FallToGlideCompositeBehaviour())
        register(GenericLandBehaviour.KEY, GenericLandBehaviour())
        register(GenericSwimBehaviour.KEY, GenericSwimBehaviour())
        register(GliderAirBehaviour.KEY, GliderAirBehaviour())
        register(HelicopterBehaviour.KEY, HelicopterBehaviour())
        register(JetAirBehaviour.KEY, JetAirBehaviour())
        register(JumpToFlightCompositeBehaviour.KEY, JumpToFlightCompositeBehaviour())
        register(RunToJetCompositeBehaviour.KEY, RunToJetCompositeBehaviour())
        register(SwimDashBehaviour.KEY, SwimDashBehaviour())
        register(VehicleLandBehaviour.KEY, VehicleLandBehaviour())
    }

    fun register(key: ResourceLocation, behaviour: RidingBehaviour<*, *>) {
        if (behaviours.contains(key)) error("Behaviour already registered to key $key")
        behaviours[key] = RidingController(behaviour)
    }

    fun get(key: ResourceLocation): RidingBehaviour<*, *> {
        if (!behaviours.contains(key)) error("Behaviour not registered to key $key")
        return behaviours[key]!!
    }
}
