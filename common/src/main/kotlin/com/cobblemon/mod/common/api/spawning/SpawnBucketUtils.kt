package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.util.weightedSelection

object SpawnBucketUtils {
    fun chooseAdjustedSpawnBucket(buckets: List<SpawnBucket>, bucketLureStrength: Int): SpawnBucket {
        val baseValues = listOf(94.3F, 5.0F, 0.5F, 0.2F)
        val adjustments = listOf(-4.1F, 2.5F, 1.0F, 0.6F)

        val adjustedWeights = buckets.mapIndexed { index, bucket ->
            if (index >= baseValues.size) {
                bucket to bucket.weight
            } else {
                val base = baseValues[index]
                val adjustment = adjustments[index]
                bucket to (base + adjustment * bucketLureStrength)
            }
        }.toMap()

        return buckets.weightedSelection { adjustedWeights[it]!! }!!
    }
}