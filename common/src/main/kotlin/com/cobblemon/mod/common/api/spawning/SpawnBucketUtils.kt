package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.util.weightedSelection

object SpawnBucketUtils {
    fun chooseAdjustedSpawnBucket(buckets: List<SpawnBucket>, bucketLureStrength: Int): SpawnBucket {
        val baseValues = listOf(88.5F, 8.5F, 2.4F, 0.6F)
        val adjustments = listOf(-6.9F, 3.6F, 2.3F, 1F)

        val adjustedWeights = buckets.mapIndexed { index, bucket ->
            if (index >= baseValues.size) {
                bucket to bucket.weight
            } else {
                val base = baseValues[index]
                val adjustment = adjustments[index]
                val adjusted = base + adjustment * bucketLureStrength
                bucket to adjusted.coerceAtLeast(0f) // we do not want the weights to be negative
            }
        }.toMap()

        return buckets.weightedSelection { adjustedWeights[it]!! }!!
    }
}