package org.ant.game

import org.ant.game.gameimpl.Chess
import org.ant.game.gameimpl.ConnectFour
import org.ant.game.gameimpl.Gomoku
import org.ant.game.gameimpl.LightsOut
import org.ant.game.gameimpl.Reversi
import org.ant.game.gameimpl.ScoreFour
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

class Game : JavaPlugin() {
    private val instance = this
    private val commandInstance = GameCommand(instance)
    private var initSucceed = false

    val chessGames = HashMap<String, Chess>()
    val gomokuGames = HashMap<String, Gomoku>()
    val reversiGames = HashMap<String, Reversi>()
    val lightsOutGames = HashMap<String, LightsOut>()
    val connectFourGames = HashMap<String, ConnectFour>()
    val scoreFourGames = HashMap<String, ScoreFour>()

    val gameConfig = GameConfig(instance)
    val gameRecord = GameRecord(instance, "record.yml")

    override fun onEnable() {
        logger.info("Ant遊戲插件已啟用")

        commandInstance.register()

        ConfigurationSerialization.registerClass(Chess::class.java)
        ConfigurationSerialization.registerClass(Gomoku::class.java)
        ConfigurationSerialization.registerClass(Reversi::class.java)
        ConfigurationSerialization.registerClass(LightsOut::class.java)
        ConfigurationSerialization.registerClass(ConnectFour::class.java)
        ConfigurationSerialization.registerClass(ScoreFour::class.java)

        server.pluginManager.registerEvents(OperateListener(instance), instance)

        saveDefaultConfig()
        gameConfig.load()
        gameRecord.load()

        initSucceed = true
    }

    override fun onDisable() {
        // commandInstance.unregister()
        if (initSucceed) {
            gameConfig.save()
            gameRecord.save()
        }
        logger.info("Ant遊戲插件已停用")
    }
}
