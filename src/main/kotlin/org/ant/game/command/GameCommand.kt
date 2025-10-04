package org.ant.game.command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import org.ant.game.AntGamePlugin
import org.ant.game.GamesManager
import org.ant.game.gameimpl.gameframe.BoardGame
import java.util.concurrent.CompletableFuture

class GameCommand(private val instance: AntGamePlugin, private val commands: ReloadableRegistrarEvent<Commands>) {
    val executor = Executor(instance)

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
                                    Commands.argument("origin", ArgumentTypes.blockPosition())
                                        .then(
                                            Commands.argument("cardinal_direction", CardinalDirectionArgument())
                                                .then(
                                                    Commands.argument("group_name", StringArgumentType.string())
                                                        .then(
                                                            Commands.argument("name", StringArgumentType.string())
                                                                .executes { ctx -> executor.setBoard(ctx, gameName) }
                                                        )
                                                )
                                        )
                                )

                        )
                }
                "gomoku", "reversi" -> {
                    gameCommand
                        .then(
                            Commands.literal("setup")
                                .then(
                                    Commands.argument("origin", ArgumentTypes.blockPosition())
                                        .then(
                                            Commands.argument("cardinal_direction", CardinalDirectionArgument())
                                                .then(
                                                    Commands.argument("orientation", OrientationArgument())
                                                        .then(
                                                            Commands.argument("group_name", StringArgumentType.string())
                                                                .then(
                                                                    Commands.argument("name", StringArgumentType.string())
                                                                        .executes { ctx -> executor.setBoard(ctx, gameName) }
                                                                )
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
                                    Commands.argument("origin", ArgumentTypes.blockPosition())
                                        .then(
                                            Commands.argument("cardinal_direction", CardinalDirectionArgument())
                                                .then(
                                                    Commands.argument("orientation", OrientationArgument())
                                                        .then(
                                                            Commands.argument("group_name", StringArgumentType.string())
                                                                .then(
                                                                    Commands.argument("name", StringArgumentType.string())
                                                                        .executes { ctx -> executor.setBoard(ctx, gameName) }
                                                                        .then(
                                                                            Commands.argument("size", IntegerArgumentType.integer())
                                                                                .executes { ctx -> executor.setBoard(ctx, gameName) }
                                                                        )
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
            gameOperateCommand.then(gameCommand)
        }

        val antGameCommand = Commands.literal("antgame")
            .requires { sender -> sender.sender.hasPermission("antgame.command") }
            .then(
                gameOperateCommand
            )
            .then(
                Commands.literal("save_config")
                    .executes { instance.gameConfig.save() }
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

    private fun <S> gameBoardSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder, gameName: String): CompletableFuture<Suggestions> {
        val groupName = context.getArgument("group_name", String::class.java)
        (GamesManager.games[gameName]?.get(groupName) as? BoardGame)?.boards?.keys?.forEach { text ->
            if (text.startsWith(builder.remainingLowerCase)) builder.suggest(text)
        }
        return CompletableFuture.completedFuture(builder.build())
    }
}
