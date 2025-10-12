package org.ant.game
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.ant.game.command.GameCommand
import org.ant.game.gameimpl.Chess
import org.ant.game.gameimpl.ConnectFour
import org.ant.game.gameimpl.Go
import org.ant.game.gameimpl.Gomoku
import org.ant.game.gameimpl.LightsOut
import org.ant.game.gameimpl.Reversi
import org.ant.game.gameimpl.ScoreFour
import org.bukkit.plugin.java.JavaPlugin

class AntGamePlugin : JavaPlugin() {
    private var initSucceed = false
    lateinit var gameAreaManager: GameAreaManager

    init {
        GamesManager
            .addGame("chess", Chess::class)
            .addGame("gomoku", Gomoku::class)
            .addGame("reversi", Reversi::class)
            .addGame("lightsOut", LightsOut::class)
            .addGame("connectFour", ConnectFour::class)
            .addGame("scoreFour", ScoreFour::class)
//            .addGame("go", Go::class)
    }

    @Suppress("UNCHECKED_CAST")
    val recordSerializableGames: List<String> = listOf(
        "chess",
        "gomoku",
        "reversi",
        "lightsOut",
        "connectFour",
        "scoreFour"
    )

    lateinit var gameConfig: GameConfig
    lateinit var gameRecord: GameRecord

    override fun onEnable() {
        logger.info("Ant遊戲插件已啟用")
        gameAreaManager = GameAreaManager(this)

        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            GameCommand(this, commands)
        }

        server.pluginManager.registerEvents(OperateListener(this), this)
        server.pluginManager.registerEvents(gameAreaManager, this)

        saveDefaultConfig()
        gameConfig = GameConfig(this, "game.yml")
        gameRecord = GameRecord(this, "record.yml")

        initSucceed = true
    }

    override fun onDisable() {
        if (initSucceed) {
            gameConfig.save()
            gameRecord.save()
            gameAreaManager.save()
        }
        GamesManager.games.clear()
        logger.info("Ant遊戲插件已停用")
    }
}
