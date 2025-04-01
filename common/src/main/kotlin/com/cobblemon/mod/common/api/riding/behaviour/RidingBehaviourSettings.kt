package com.cobblemon.mod.common.api.riding.behaviour

import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable

/**
 * Represents static settings of a riding behaviour.
 * Values in this class are intended to be constant and not change during the riding process.
 * Typically this will be initialized for each pokemon form during deserialization
 * to determine how they should ride.
 *
 * @author landonjw
 */
interface RidingBehaviourSettings: Encodable, Decodable
