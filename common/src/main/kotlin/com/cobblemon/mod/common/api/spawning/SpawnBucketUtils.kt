package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.util.weightedSelection

object SpawnBucketUtils {
    fun chooseAdjustedSpawnBucket(buckets: List<SpawnBucket>, bucketLureStrength: Int): SpawnBucket {
        val baseValues = listOf(91.6F, 7.0F, 1.0F, 0.4F)
        val adjustments = listOf(-5.5F, 3.5F, 1.2F, 0.8F)

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