package org.ant.game

import com.mojang.brigadier.Command
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class GameRecord(instance: Game, fileName: String) {
    val file = File(instance.dataFolder, fileName)
    val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

    val configs = mapOf<String, Map<String, RecordSerializable>>(
        // "chess_games" to instance.chessGames,
        "gomoku_games" to instance.gomokuGames,
        "reversi_games" to instance.reversiGames,
        "lightsOut_games" to instance.lightsOutGames,
        "connectFour_games" to instance.connectFourGames,
        "scoreFour_games" to instance.scoreFourGames
    )

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
