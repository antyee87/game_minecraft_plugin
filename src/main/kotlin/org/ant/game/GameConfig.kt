package org.ant.game

import com.mojang.brigadier.Command
import org.ant.plugin.Chess
import org.ant.plugin.ConnectFour
import org.ant.plugin.Gomoku
import org.ant.plugin.LightsOut
import org.ant.plugin.Reversi
import org.ant.plugin.ScoreFour

object GameConfig {
    fun load(instance: Game) {
        val config = instance.config
        // chess games deserialize
        var section = config.getConfigurationSection("chess_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.chess_games[key] = Chess.deserialize(data)
            }
        }
        // gomoku games deserialize
        section = config.getConfigurationSection("gomoku_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.gomoku_games[key] = Gomoku.deserialize(instance, data)
            }
        }
        // reversi games deserialize
        section = config.getConfigurationSection("reversi_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.reversi_games[key] = Reversi.deserialize(instance, data)
            }
        }
        // lightsOut games deserialize
        section = config.getConfigurationSection("lightsOut_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.lightsOut_games[key] = LightsOut.deserialize(data)
            }
        }
        // connectFour games deserialize
        section = config.getConfigurationSection("connectFour_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.connectFour_games[key] = ConnectFour.deserialize(instance, data)
            }
        }
        // scoreFour games deserialize
        section = config.getConfigurationSection("scoreFour_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.scoreFour_games[key] = ScoreFour.deserialize(instance, data)
            }
        }
    }

    fun save(instance: Game): Int {
        val config = instance.config
        // chess games serialize
        config["chess_games"] = null
        for ((key, value) in instance.chess_games) {
            config["chess_games.$key"] = value.serialize()
        }
        // gomoku games serialize
        config["gomoku_games"] = null
        for ((key, value) in instance.gomoku_games) {
            config["gomoku_games.$key"] = value.serialize()
        }
        // reversi games serialize
        config["reversi_games"] = null
        for ((key, value) in instance.reversi_games) {
            config["reversi_games.$key"] = value.serialize()
        }
        // lightOut games serialize
        config["lightsOut_games"] = null
        for ((key, value) in instance.lightsOut_games) {
            config["lightsOut_games.$key"] = value.serialize()
        }
        // connectFour games serialize
        config["connectFour_games"] = null
        for ((key, value) in instance.connectFour_games) {
            config["connectFour_games.$key"] = value.serialize()
        }
        // scoreFour games serialize
        config["scoreFour_games"] = null
        for ((key, value) in instance.scoreFour_games) {
            config["scoreFour_games.$key"] = value.serialize()
        }
        instance.saveConfig()

        return Command.SINGLE_SUCCESS
    }
}
