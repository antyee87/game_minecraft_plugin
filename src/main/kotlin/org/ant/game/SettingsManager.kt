package org.ant.game

import com.mojang.brigadier.Command
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class SettingsManager(private val pluginInstance: AntGamePlugin) {
    val settings = hashMapOf<String, Any>(
        "visible" to false,
        "flyable" to true,
        "sgf_directory" to "",
        "sgf_share_url" to "",
        "game_interaction_range" to 64.0
    )

    init {
        pluginInstance.saveDefaultConfig()
        load()
    }

    fun load() {
        for (key in pluginInstance.config.getKeys(false)) {
            @Suppress("UNCHECKED_CAST")
            settings[key] = pluginInstance.config[key] as Any
        }
    }

    fun save(): Int {
        settings.forEach { (key, value) ->
            pluginInstance.config[key] = value
        }
        pluginInstance.saveConfig()
        return Command.SINGLE_SUCCESS
    }
}
