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
import org.ant.game.gameimpl.gameframe.BoardGame
import org.ant.game.gameimpl.gameframe.Method
import java.util.concurrent.CompletableFuture

class GameCommand(private val pluginInstance: AntGamePlugin, private val commands: ReloadableRegistrarEvent<Commands>) {
    val executor = Executor(pluginInstance)

    init {
        registerCommand()
    }

    private fun registerCommand() {
        val gameOperateCommand = Commands.literal("game_operate")

        for (gameName in GamesManager.games.keys) {
            val gameCommand = Commands.literal(gameName)
            when (gameName) {
                "chess", "connectFour", "scoreFour" -> {
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
                                                                .executes { ctx -> executor.setupGame(ctx, gameName) }
                                                        )
                                                )
                                        )
                                )
                        )
                }
                "lightsOut" -> {
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
                                                                .then(
                                                                    Commands.argument("orientation", OrientationArgument())
                                                                        .then(
                                                                            Commands.argument("size", IntegerArgumentType.integer())
                                                                                .executes { ctx -> executor.setupGame(ctx, gameName) }
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                }
                else -> {
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
                                                                .then(
                                                                    Commands.argument("orientation", OrientationArgument())
                                                                        .executes { ctx -> executor.setupGame(ctx, gameName) }
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                }
            }
            gameCommand
                .then(
                    Commands.literal("reset")
                        .then(
                            Commands.argument("group_name", StringArgumentType.string())
                                .suggests { _, builder -> gameGroupSuggestions(builder, gameName) }
                                .executes { ctx -> executor.resetBoard(ctx, gameName) }
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("group_name", StringArgumentType.string())
                                .suggests { _, builder -> gameGroupSuggestions(builder, gameName) }
                                .executes { ctx -> executor.removeBoard(ctx, gameName) }
                                .then(
                                    Commands.argument("name", StringArgumentType.string())
                                        .suggests { ctx, builder -> gameBoardSuggestions(ctx, builder, gameName) }
                                        .executes { ctx -> executor.removeBoard(ctx, gameName) }
                                )
                        )
                )
                .then(
                    Commands.literal("list")
                        .executes { ctx -> executor.listGame(ctx, gameName) }
                        .then(
                            Commands.argument("group_name", StringArgumentType.string())
                                .suggests { _, builder -> gameGroupSuggestions(builder, gameName) }
                                .executes { ctx -> executor.listGame(ctx, gameName) }
                        )

                )
            gameOperateCommand.then(gameCommand)
        }

        val setCommand = Commands.literal("set")

        for ((key, setting) in pluginInstance.gameAreaManager.settings) {
            setCommand.then(
                Commands.literal(key)
                    .then(
                        Commands.argument("value", Method.getArgumentType(setting.value))
                            .executes { ctx ->
                                val input = ctx.getArgument("value", setting.value::class.java)
                                setting.value = input
                                ctx.source.sender.sendMessage(Component.text("已設定 $key = $input", NamedTextColor.GREEN))
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
        }

        val antGameCommand = Commands.literal("antgame")
            .requires { sender -> sender.sender.hasPermission("antgame.command") }
            .then(
                gameOperateCommand
            )
            .then(
                Commands.literal("save")
                    .then(
                        Commands.literal("game_config")
                            .executes { pluginInstance.gameConfig.save() }
                    )
                    .then(
                        Commands.literal("game_record")
                            .executes { pluginInstance.gameRecord.save() }
                    )
                    .then(
                        Commands.literal("game_area")
                            .executes { pluginInstance.gameAreaManager.save() }
                    )
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
                        setCommand
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

    private fun gameGroupSuggestions(builder: SuggestionsBuilder, gameName: String): CompletableFuture<Suggestions> {
        GamesManager.games[gameName]?.keys?.forEach { text ->
            if (text.startsWith(builder.remainingLowerCase)) builder.suggest(text)
        }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun gameBoardSuggestions(context: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder, gameName: String): CompletableFuture<Suggestions> {
        val groupName = context.getArgument("group_name", String::class.java)
        (GamesManager.games[gameName]?.get(groupName) as? BoardGame)?.boards?.keys?.forEach { text ->
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
