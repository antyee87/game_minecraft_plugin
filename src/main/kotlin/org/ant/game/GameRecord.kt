package org.ant.game

import com.mojang.brigadier.Command
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class GameRecord(instance: Game, fileName: String) {
    val file = File(instance.dataFolder, fileName)
    val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
    val configs = instance.configs

    init {
        load()
    }

    fun load(): Int {
        configs.forEach { (configKey, gameInstances) ->
            val section = config.getConfigurationSection(configKey)
            if (section != null) {
                for (key in section.getKeys(false)) {
                    val data = section.getConfigurationSection(key)!!.getValues(false)
                    gameInstances[key]!!.deserializeRecord(data)
                }
            }
        }

        return Command.SINGLE_SUCCESS
    }

    fun save(): Int {
        configs.forEach { (configKey, gameInstances) ->
            config[configKey] = null
            for ((key, value) in gameInstances) {
                config["$configKey.$key"] = value.serializeRecord()
            }
        }

        config.save(file)

        return Command.SINGLE_SUCCESS
    }
}
