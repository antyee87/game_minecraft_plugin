package org.ant.game

import com.mojang.brigadier.Command
import org.ant.game.gameimpl.Chess
import org.ant.game.gameimpl.ConnectFour
import org.ant.game.gameimpl.Gomoku
import org.ant.game.gameimpl.LightsOut
import org.ant.game.gameimpl.Reversi
import org.ant.game.gameimpl.ScoreFour

class GameConfig(private val instance: Game) {
    val config = instance.config

    fun load() {
        // chess games deserialize
        var section = config.getConfigurationSection("chess_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.chessGames[key] = Chess.deserialize(data)
            }
        }
        // gomoku games deserialize
        section = config.getConfigurationSection("gomoku_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.gomokuGames[key] = Gomoku.deserialize(instance, data)
            }
        }
        // reversi games deserialize
        section = config.getConfigurationSection("reversi_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.reversiGames[key] = Reversi.deserialize(instance, data)
            }
        }
        // lightsOut games deserialize
        section = config.getConfigurationSection("lightsOut_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.lightsOutGames[key] = LightsOut.deserialize(data)
            }
        }
        // connectFour games deserialize
        section = config.getConfigurationSection("connectFour_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.connectFourGames[key] = ConnectFour.deserialize(instance, data)
            }
        }
        // scoreFour games deserialize
        section = config.getConfigurationSection("scoreFour_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                instance.scoreFourGames[key] = ScoreFour.deserialize(instance, data)
            }
        }
    }

    fun save(): Int {
        // chess games serialize
        config["chess_games"] = null
        for ((key, value) in instance.chessGames) {
            config["chess_games.$key"] = value.serialize()
        }
        // gomoku games serialize
        config["gomoku_games"] = null
        for ((key, value) in instance.gomokuGames) {
            config["gomoku_games.$key"] = value.serialize()
        }
        // reversi games serialize
        config["reversi_games"] = null
        for ((key, value) in instance.reversiGames) {
            config["reversi_games.$key"] = value.serialize()
        }
        // lightOut games serialize
        config["lightsOut_games"] = null
        for ((key, value) in instance.lightsOutGames) {
            config["lightsOut_games.$key"] = value.serialize()
        }
        // connectFour games serialize
        config["connectFour_games"] = null
        for ((key, value) in instance.connectFourGames) {
            config["connectFour_games.$key"] = value.serialize()
        }
        // scoreFour games serialize
        config["scoreFour_games"] = null
        for ((key, value) in instance.scoreFourGames) {
            config["scoreFour_games.$key"] = value.serialize()
        }

        instance.saveConfig()

        return Command.SINGLE_SUCCESS
    }
}
