package org.ant.game

import org.ant.plugin.Chess
import org.ant.plugin.ConnectFour
import org.ant.plugin.Gomoku
import org.ant.plugin.LightsOut
import org.ant.plugin.Reversi
import org.ant.plugin.ScoreFour
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

@Suppress("PropertyName")
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

        GameConfig.load(instance)
    }

    override fun onDisable() {
        GameConfig.save(instance)
        logger.info("Ant遊戲插件已停用")
    }
}
