package org.ant.game.command
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.AntGamePlugin
import org.ant.game.GamesManager
import org.ant.game.gameimpl.Chess
import org.ant.game.gameimpl.ConnectFour
import org.ant.game.gameimpl.Go
import org.ant.game.gameimpl.LightsOut
import org.ant.game.gameimpl.ScoreFour
import org.ant.game.gameimpl.gameframe.BoardGame
import org.ant.game.gameimpl.gameframe.GameSerializable
import org.ant.game.gameimpl.gameframe.Method
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

class GameCommand(private val pluginInstance: AntGamePlugin, private val commands: ReloadableRegistrarEvent<Commands>) {
    val executor = Executor(pluginInstance)

    init {
        registerCommand()
    }

    private fun registerCommand() {
        val gameOperateCommand = Commands.literal("game_operate")

        for (gameClass in GamesManager.games.keys) {
            val gameCommand = Commands.literal(GamesManager.gameNames[gameClass]!!)
            gameCommand
                .then(
                    Commands.literal("setup")
                        .then(
                            Commands.argument("group_name", StringArgumentType.string())
                                .then(
                                    Commands.argument("name", StringArgumentType.string())
                                        .then(
                                            Commands.argument("origin", ArgumentTypes.blockPosition())
                                                .then(
                                                    Commands.argument("cardinal_direction", CardinalDirectionArgument())
                                                        .let { it1 ->
                                                            when(gameClass) {
                                                                Chess::class, ConnectFour::class, ScoreFour::class -> {
                                                                    it1.executes { ctx -> executor.setupGame(ctx, gameClass) }
                                                                }
                                                                else -> it1.then(
                                                                Commands.argument("orientation", OrientationArgument())
                                                                    .let { it2 ->
                                                                        when(gameClass) {
                                                                            LightsOut::class -> {
                                                                                it2.then(
                                                                                    Commands.argument("size", IntegerArgumentType.integer())
                                                                                        .executes { ctx -> executor.setupGame(ctx, gameClass) }
                                                                                )
                                                                            }
                                                                            else -> {
                                                                                it2.executes { ctx -> executor.setupGame(ctx, gameClass) }
                                                                            }
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                        }
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("reset")
                        .then(
                            Commands.argument("group_name", StringArgumentType.string())
                                .suggests { _, builder -> gameGroupSuggestions(builder, gameClass) }
                                .executes { ctx -> executor.resetBoard(ctx, gameClass) }
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("group_name", StringArgumentType.string())
                                .suggests { _, builder -> gameGroupSuggestions(builder, gameClass) }
                                .executes { ctx -> executor.removeBoard(ctx, gameClass) }
                                .then(
                                    Commands.argument("name", StringArgumentType.string())
                                        .suggests { ctx, builder -> gameBoardSuggestions(ctx, builder, gameClass) }
                                        .executes { ctx -> executor.removeBoard(ctx, gameClass) }
                                )
                        )
                )
                .then(
                    Commands.literal("list")
                        .executes { ctx -> executor.listGame(ctx, gameClass) }
                        .then(
                            Commands.argument("group_name", StringArgumentType.string())
                                .suggests { _, builder -> gameGroupSuggestions(builder, gameClass) }
                                .executes { ctx -> executor.listGame(ctx, gameClass) }
                        )

                )
            gameOperateCommand.then(gameCommand)
        }

        gameOperateCommand
            .then(
                Commands.literal("go")
                    .then(
                        Commands.literal("get_sgf")
                            .then(
                                Commands.argument("group_name", StringArgumentType.string())
                                    .suggests { _, builder -> gameGroupSuggestions(builder, Go::class) }
                                    .executes { ctx -> executor.getSgf(ctx) }
                            )
                    )
            )

        val settingsCommand = Commands.literal("settings")
        for (key in pluginInstance.settingsManager.settings.keys) {
            settingsCommand.then(
                Commands.literal(key)
                    .executes { ctx ->
                        ctx.source.sender.sendMessage(Component.text("$key = ${pluginInstance.settingsManager.settings[key]}", NamedTextColor.GREEN))
                        Command.SINGLE_SUCCESS
                    }
                    .then(
                        Commands.argument("value", Method.getArgumentType(pluginInstance.settingsManager.settings[key]!!))
                            .executes { ctx ->
                                val input = ctx.getArgument("value", pluginInstance.settingsManager.settings[key]!!::class.java)
                                pluginInstance.settingsManager.settings[key] = input
                                ctx.source.sender.sendMessage(Component.text("已設定 $key = $input", NamedTextColor.GREEN))
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
        }

        val antGameCommand = Commands.literal("antgame")
            .requires { sender -> sender.sender.hasPermission("antgame.command") }
            .then(
                Commands.literal("save")
                    .then(
                        Commands.literal("settings")
                            .executes { pluginInstance.settingsManager.save() }
                    )
                    .then(
                        Commands.literal("game")
                            .executes {
                                pluginInstance.gameConfig.save()
                                pluginInstance.gameRecord.save()
                            }
                    )
                    .then(
                        Commands.literal("game_area")
                            .executes { pluginInstance.gameAreaManager.save() }
                    )
            )
            .then(
                gameOperateCommand

            )
            .then(
                settingsCommand
            )
            .then(
                Commands.literal("game_area")
                    .then(
                        Commands.literal("setup")
                            .then(
                                Commands.argument("name", StringArgumentType.string())
                                    .then(
                                        Commands.argument("pos1", ArgumentTypes.blockPosition())
                                            .then(
                                                Commands.argument("pos2", ArgumentTypes.blockPosition())
                                                    .executes { ctx -> executor.setupGameArea(ctx) }
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("remove")
                            .then(
                                Commands.argument("name", StringArgumentType.string())
                                    .suggests { _, builder -> gameAreaSuggestions(builder) }
                                    .executes { ctx -> executor.removeGameArea(ctx) }
                            )
                    )
                    .then(
                        Commands.literal("list")
                            .then(
                                Commands.argument("name", StringArgumentType.string())
                                    .suggests { _, builder -> gameAreaSuggestions(builder) }
                                    .executes { ctx -> executor.listGameArea(ctx) }
                            )
                    )
            )
            .build()
        commands.registrar().register(antGameCommand)
    }

    private fun gameGroupSuggestions(builder: SuggestionsBuilder, gameClass: KClass<out GameSerializable>): CompletableFuture<Suggestions> {
        GamesManager.games[gameClass]?.keys?.forEach { text ->
            if (text.startsWith(builder.remainingLowerCase)) builder.suggest(text)
        }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun gameBoardSuggestions(context: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder, gameClass: KClass<out GameSerializable>): CompletableFuture<Suggestions> {
        val groupName = context.getArgument("group_name", String::class.java)
        (GamesManager.games[gameClass]?.get(groupName) as? BoardGame)?.boards?.keys?.forEach { text ->
            if (text.startsWith(builder.remainingLowerCase)) builder.suggest(text)
        }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun gameAreaSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        pluginInstance.gameAreaManager.gameAreas.keys.forEach { text ->
            if (text.startsWith(builder.remainingLowerCase)) builder.suggest(text)
        }
        return CompletableFuture.completedFuture(builder.build())
    }
}
