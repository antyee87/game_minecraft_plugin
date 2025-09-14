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
    private lateinit var commandInstance: GameCommand
    private var initSucceed = false

    val chessGames = HashMap<String, Chess>()
    val gomokuGames = HashMap<String, Gomoku>()
    val reversiGames = HashMap<String, Reversi>()
    val lightsOutGames = HashMap<String, LightsOut>()
    val connectFourGames = HashMap<String, ConnectFour>()
    val scoreFourGames = HashMap<String, ScoreFour>()

    @Suppress("UNCHECKED_CAST")
    val configs: Map<String, HashMap<String, GameSerializable>> = mapOf(
        "chess_games" to instance.chessGames,
        "gomoku_games" to instance.gomokuGames,
        "reversi_games" to instance.reversiGames,
        "lightsOut_games" to instance.lightsOutGames,
        "connectFour_games" to instance.connectFourGames,
        "scoreFour_games" to instance.scoreFourGames
    ).mapValues { it.value as HashMap<String, GameSerializable> }

    val games: Map<String, GameDeSerializable> = mapOf(
        "chess_games" to Chess,
        "gomoku_games" to Gomoku,
        "reversi_games" to Reversi,
        "lightsOut_games" to LightsOut,
        "connectFour_games" to ConnectFour,
        "scoreFour_games" to ScoreFour
    )

    lateinit var gameConfig: GameConfig
    lateinit var gameRecord: GameRecord

    override fun onEnable() {
        logger.info("Ant遊戲插件已啟用")

        commandInstance = GameCommand(instance)

        ConfigurationSerialization.registerClass(Chess::class.java)
        ConfigurationSerialization.registerClass(Gomoku::class.java)
        ConfigurationSerialization.registerClass(Reversi::class.java)
        ConfigurationSerialization.registerClass(LightsOut::class.java)
        ConfigurationSerialization.registerClass(ConnectFour::class.java)
        ConfigurationSerialization.registerClass(ScoreFour::class.java)

        server.pluginManager.registerEvents(OperateListener(instance), instance)

        saveDefaultConfig()
        gameConfig = GameConfig(instance)
        gameRecord = GameRecord(instance, "record.yml")

        initSucceed = true
    }

    override fun onDisable() {
        commandInstance.unregister()
        if (initSucceed) {
            gameConfig.save()
            gameRecord.save()
        }
        logger.info("Ant遊戲插件已停用")
    }
}
