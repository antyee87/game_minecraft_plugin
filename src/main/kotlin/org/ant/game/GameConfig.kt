package org.ant.game

import com.mojang.brigadier.Command
import org.ant.game.gameimpl.gameframe.Method.toMapRecursively
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class GameConfig(private val pluginInstance: AntGamePlugin, fileName: String) {
    private val file = File(pluginInstance.dataFolder, fileName)
    private val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

    init {
        load()
    }

    fun load() {
        GamesManager.games.forEach { (configKey, gameInstances) ->
            val section = config.getConfigurationSection(GamesManager.gameNames[configKey]!!)
            if (section != null) {
                for (key in section.getKeys(false)) {
                    val data = section.getConfigurationSection(key)?.toMapRecursively() ?: continue
                    gameInstances[key] = GamesManager.gameCompanionObjects[configKey]!!.deserialize(pluginInstance, data)
                }
            }
        }
    }

    fun save(): Int {
        GamesManager.games.forEach { (configKey, gameInstances) ->
            config[GamesManager.gameNames[configKey]!!] = null
            for ((key, value) in gameInstances) {
                config["${GamesManager.gameNames[configKey]!!}.$key"] = value.serialize()
            }
        }

        config.save(file)

        return Command.SINGLE_SUCCESS
    }
}
