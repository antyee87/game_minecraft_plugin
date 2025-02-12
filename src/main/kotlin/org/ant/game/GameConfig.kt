package org.ant.game

import com.mojang.brigadier.Command

object GameConfig {
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
