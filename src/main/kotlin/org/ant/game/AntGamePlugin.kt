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
import org.ant.game.gameimpl.gameframe.GameSerializable
import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass

class AntGamePlugin : JavaPlugin() {
    private var initSucceed = false
    lateinit var settingsManager: SettingsManager
    lateinit var gameAreaManager: GameAreaManager
    lateinit var gameConfig: GameConfig
    lateinit var gameRecord: GameRecord

    init {
        GamesManager
            .addGame("chess", Chess::class)
            .addGame("gomoku", Gomoku::class)
            .addGame("reversi", Reversi::class)
            .addGame("lights_out", LightsOut::class)
            .addGame("connect_four", ConnectFour::class)
            .addGame("score_four", ScoreFour::class)
            .addGame("go", Go::class)
    }

    @Suppress("UNCHECKED_CAST")
    val recordSerializableGames: List<KClass<GameSerializable>> = listOf(
        Chess::class,
        Gomoku::class,
        Reversi::class,
        LightsOut::class,
        ConnectFour::class,
        ScoreFour::class,
        Go::class
    ) as List<KClass<GameSerializable>>

    override fun onEnable() {
        logger.info("Ant遊戲插件已啟用")
        settingsManager = SettingsManager(this)
        gameAreaManager = GameAreaManager(this)

        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            GameCommand(this, commands)
        }

        server.pluginManager.registerEvents(OperateListener(this), this)
        server.pluginManager.registerEvents(gameAreaManager, this)

        gameConfig = GameConfig(this, "game.yml")
        gameRecord = GameRecord(this, "record.yml")

        initSucceed = true
    }

    override fun onDisable() {
        if (initSucceed) {
            gameConfig.save()
            gameRecord.save()
            settingsManager.save()
            gameAreaManager.save()
        }
        GamesManager.games.clear()
        logger.info("Ant遊戲插件已停用")
    }
}
