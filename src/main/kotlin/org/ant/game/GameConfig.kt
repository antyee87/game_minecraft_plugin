package org.ant.game

import com.mojang.brigadier.Command
import org.ant.game.gameimpl.gameframe.Method.toMapRecursively
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class GameConfig(private val instance: AntGamePlugin, fileName: String) {
    val file = File(instance.dataFolder, fileName)
    val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

    init {
        load()
    }

    fun load() {
        GamesManager.games.forEach { (configKey, gameInstances) ->
            val section = config.getConfigurationSection(configKey)
            if (section != null) {
                for (key in section.getKeys(false)) {
                    val data = section.getConfigurationSection(key)!!.toMapRecursively()
                    gameInstances[key] = GamesManager.gameCompanionObjects[configKey]!!.deserialize(instance, data)
                }
            }
        }
    }

    fun save(): Int {
        GamesManager.games.forEach { (configKey, gameInstances) ->
            config[configKey] = null
            for ((key, value) in gameInstances) {
                config["$configKey.$key"] = value.serialize()
            }
        }

        config.save(file)

        return Command.SINGLE_SUCCESS
    }
}
