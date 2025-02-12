package org.ant.game

import org.ant.plugin.Chess
import org.ant.plugin.ConnectFour
import org.ant.plugin.Gomoku
import org.ant.plugin.LightsOut
import org.ant.plugin.Reversi
import org.ant.plugin.ScoreFour
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

@Suppress("PropertyName", "ktlint:standard:property-naming")
class Game : JavaPlugin() {
    private val instance = this
    var chess_games = HashMap<String, Chess>()
    var gomoku_games = HashMap<String, Gomoku>()
    var reversi_games = HashMap<String, Reversi>()
    var lightsOut_games = HashMap<String, LightsOut>()
    var connectFour_games = HashMap<String, ConnectFour>()
    var scoreFour_games = HashMap<String, ScoreFour>()

    override fun onEnable() {
        logger.info("Ant遊戲插件已啟用")

        GameCommand(instance).register()

        ConfigurationSerialization.registerClass(Chess::class.java)
        ConfigurationSerialization.registerClass(Gomoku::class.java)
        ConfigurationSerialization.registerClass(Reversi::class.java)
        ConfigurationSerialization.registerClass(LightsOut::class.java)
        ConfigurationSerialization.registerClass(ConnectFour::class.java)
        ConfigurationSerialization.registerClass(ScoreFour::class.java)

        server.pluginManager.registerEvents(OperateListener(instance), instance)
        saveDefaultConfig()

        // chess games deserialize
        var section = config.getConfigurationSection("chess_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                chess_games[key] = Chess.deserialize(data)
            }
        }
        // gomoku games deserialize
        section = config.getConfigurationSection("gomoku_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                gomoku_games[key] = Gomoku.deserialize(data)
            }
        }
        // reversi games deserialize
        section = config.getConfigurationSection("reversi_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                reversi_games[key] = Reversi.deserialize(data)
            }
        }
        // lightsOut games deserialize
        section = config.getConfigurationSection("lightsOut_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                lightsOut_games[key] = LightsOut.deserialize(data)
            }
        }
        // connectFour games deserialize
        section = config.getConfigurationSection("connectFour_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                connectFour_games[key] = ConnectFour.deserialize(data)
            }
        }
        // scoreFour games deserialize
        section = config.getConfigurationSection("scoreFour_games")
        if (section != null) {
            for (key in section.getKeys(false)) {
                val data = section.getConfigurationSection(key)!!.getValues(false)
                scoreFour_games[key] = ScoreFour.deserialize(data)
            }
        }
    }

    override fun onDisable() {
        GameConfig.save(instance)
        logger.info("Ant遊戲插件已停用")
    }
}
