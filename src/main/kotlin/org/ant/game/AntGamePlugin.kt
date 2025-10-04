package org.ant.game
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.ant.game.command.GameCommand
import org.ant.game.gameimpl.Chess
import org.ant.game.gameimpl.ConnectFour
import org.ant.game.gameimpl.Gomoku
import org.ant.game.gameimpl.LightsOut
import org.ant.game.gameimpl.Reversi
import org.ant.game.gameimpl.ScoreFour
import org.bukkit.plugin.java.JavaPlugin
import javax.xml.stream.Location

class AntGamePlugin : JavaPlugin() {
    private val instance = this
    private var initSucceed = false

    val flyableArea = mutableListOf<Pair<Location, Location>>()

    init {
        GamesManager
            .addGame<Chess>("chess")
            .addGame<Gomoku>("gomoku")
            .addGame<Reversi>("reversi")
            .addGame<LightsOut>("lightsOut")
            .addGame<ConnectFour>("connectFour")
            .addGame<ScoreFour>("scoreFour")
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
        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            GameCommand(instance, commands)
        }

        server.pluginManager.registerEvents(OperateListener(instance), instance)

        saveDefaultConfig()
        gameConfig = GameConfig(instance, "game.yml")
        gameRecord = GameRecord(instance, "record.yml")
        initSucceed = true
    }

    override fun onDisable() {
        if (initSucceed) {
            gameConfig.save()
            gameRecord.save()
        }
        logger.info("Ant遊戲插件已停用")
    }
}
