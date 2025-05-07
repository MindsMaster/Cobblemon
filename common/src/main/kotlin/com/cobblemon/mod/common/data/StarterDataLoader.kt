package com.cobblemon.mod.common.data

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.config.starter.StarterCategory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

private val GSON: Gson = GsonBuilder()
    .setPrettyPrinting()
    .create()

object StarterDataLoader : SimpleJsonResourceReloadListener(GSON, "starters") {

    private val categories = mutableListOf<StarterCategory>()
    fun getAllCategories(): List<StarterCategory> = categories.toList()

    override fun prepare(
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ): Map<ResourceLocation, JsonElement> {
        val validJsons = mutableMapOf<ResourceLocation, JsonElement>()

        // Load every JSON under data/namespace/starters
        resourceManager.listResources(
            "starters",
            { filePath -> filePath.path.endsWith(".json") }
        ).forEach { (resourceId, resource) ->
            val fileName = resourceId.path.substringAfterLast('/')
            if (fileName != fileName.lowercase()) {
                LOGGER.warn("Skipping '{}': File name must be lowercase", resourceId)
                return@forEach
            }

            resource.openAsReader().use { reader ->
                runCatching {
                    GSON.fromJson(reader, JsonElement::class.java)
                }.onSuccess { element ->
                    validJsons[resourceId] = element
                }.onFailure { error ->
                    LOGGER.warn(
                        "Skipping: '{}': {}: {}",
                        resourceId,
                        (if (error is JsonParseException) "malformed JSON" else "parse error"),
                        error.message
                    )
                }
            }
        }
        return validJsons
    }

    override fun apply(
        jsons: Map<ResourceLocation, JsonElement>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        categories.clear()

        // If enabled, start with default built-in starters
        if (Cobblemon.starterConfig.useConfigStarters) {
            categories += Cobblemon.starterConfig.starters
        }

        val seenNames = mutableSetOf<String>()
        val loadedCategories = mutableListOf<StarterCategory>()

        jsons.forEach { (resourceId, element) ->
            val obj = element.asJsonObject

            if (!obj.has("displayName") || !obj.has("pokemon")) {
                LOGGER.warn("Skipping '{}': Missing 'displayName' or 'pokemon' field", resourceId)
                return@forEach
            }

            val displayName = obj.get("displayName").asString
            val entries = obj.getAsJsonArray("pokemon")
                .map { PokemonProperties.parse(it.asString) }
            val category = StarterCategory(resourceId.path, displayName, entries)

            // Warn on duplicate category name
            if (!seenNames.add(category.name.lowercase())) {
                LOGGER.warn("Duplicate datapack starter '{}' - this will override the previous one", category.name)
            }
            loadedCategories += category
        }

        if (!Cobblemon.starterConfig.useConfigStarters) {
            // Default: If datapacks exist, use only them, otherwise fall back to built-in starters
            if (loadedCategories.isNotEmpty()) {
                categories.clear()
                categories += loadedCategories
            } else {
                categories += Cobblemon.starterConfig.starters
                LOGGER.info("No datapack starters found; defaulting to built-in")
            }
        } else {
            // Merge: Replace matching entries in-place, otherwise append
            loadedCategories.forEach { newCategory ->
                val existingIndex = categories.indexOfFirst { it.name.equals(newCategory.name, ignoreCase = true) }
                if (existingIndex >= 0) {
                    // Overwrite built-in category at same position
                    categories[existingIndex] = newCategory
                    LOGGER.info("Replaced starter category '{}' at position {}", newCategory.name, existingIndex)
                } else {
                    // No existing entry so append
                    categories += newCategory
                    LOGGER.info("Appended starter category '{}'", newCategory.name)
                }
            }
        }
    }
}