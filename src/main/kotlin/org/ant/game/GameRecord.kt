package org.ant.game

import com.mojang.brigadier.Command
import org.ant.game.gameimpl.gameframe.Method.toMapRecursively
import org.ant.game.gameimpl.gameframe.RecordSerializable
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class GameRecord(instance: AntGamePlugin, fileName: String) {
    val file = File(instance.dataFolder, fileName)
    val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
    val recordSerializableGames = instance.recordSerializableGames

    init {
        load()
    }

    fun load(): Int {
        recordSerializableGames.forEach { configKey ->
            val gameInstances = GamesManager.games[configKey]!!
            val section = config.getConfigurationSection(configKey)
            if (section != null) {
                for (key in section.getKeys(false)) {
                    val data = section.getConfigurationSection(key)!!.toMapRecursively()
                    (gameInstances[key]!! as RecordSerializable).deserializeRecord(data)
                }
            }
        }

        return Command.SINGLE_SUCCESS
    }

    fun save(): Int {
        recordSerializableGames.forEach { configKey ->
            config[configKey] = null
            val gameInstances = GamesManager.games[configKey]!!
            for ((key, value) in gameInstances) {
                config["$configKey.$key"] = (value as RecordSerializable).serializeRecord()
            }
        }

        config.save(file)

        return Command.SINGLE_SUCCESS
    }
}
