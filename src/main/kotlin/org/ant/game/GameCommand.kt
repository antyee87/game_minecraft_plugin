package org.ant.game

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage")
class GameCommand(private val instance: Game) {
    fun register() {
        val execute = Execute(instance)
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().dispatcher.register(
                Commands.literal("antgame")
                    .requires { sender: CommandSourceStack -> sender.sender.hasPermission("antgame.antgame") }
                    .then(
                        Commands.literal("chess")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .executes { ctx -> execute.setBoard(ctx, "chess") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> chessSuggestions(builder) }
                                                    .executes { ctx -> execute.resetBoard(ctx, "chess") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> chessSuggestions(builder) }
                                                    .executes { ctx -> execute.removeBoard(ctx, "chess") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("get_player")
                                    .then(
                                        Commands.argument("name", StringArgumentType.word())
                                            .suggests { _, builder -> chessSuggestions(builder) }
                                            .executes { ctx -> execute.getPlayer(ctx, "chess") }
                                    )
                            )
                    ).then(
                        Commands.literal("gomoku")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .executes { ctx -> execute.setBoard(ctx, "gomoku") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> gomokuSuggestions(builder) }
                                                    .executes { ctx -> execute.resetBoard(ctx, "gomoku") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> gomokuSuggestions(builder) }
                                                    .executes { ctx -> execute.removeBoard(ctx, "gomoku") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("display")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> gomokuSuggestions(builder) }
                                                    .then(
                                                        Commands.argument("align", StringArgumentType.word())
                                                            .suggests { _, builder -> alignSuggestions(builder) }
                                                            .executes { ctx -> execute.setDisplay(ctx, "gomoku") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> gomokuSuggestions(builder) }
                                                    .executes { ctx -> execute.removeDisplay(ctx, "gomoku") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("get_player")
                                    .then(
                                        Commands.argument("name", StringArgumentType.word())
                                            .suggests { _, builder -> gomokuSuggestions(builder) }
                                            .executes { ctx -> execute.getPlayer(ctx, "gomoku") }
                                    )
                            )
                    )
                    .then(
                        Commands.literal("reversi")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .executes { ctx -> execute.setBoard(ctx, "reversi") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> reversiSuggestions(builder) }
                                                    .executes { ctx -> execute.resetBoard(ctx, "reversi") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> reversiSuggestions(builder) }
                                                    .executes { ctx -> execute.removeBoard(ctx, "reversi") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("display")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> reversiSuggestions(builder) }
                                                    .then(
                                                        Commands.argument("align", StringArgumentType.word())
                                                            .suggests { _, builder -> alignSuggestions(builder) }
                                                            .executes { ctx -> execute.setDisplay(ctx, "reversi") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> reversiSuggestions(builder) }
                                                    .executes { ctx -> execute.removeDisplay(ctx, "reversi") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("get_player")
                                    .then(
                                        Commands.argument("name", StringArgumentType.word())
                                            .suggests { _, builder -> reversiSuggestions(builder) }
                                            .executes { ctx -> execute.getPlayer(ctx, "reversi") }
                                    )
                            )
                    )
                    .then(
                        Commands.literal("lights_out")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .then(
                                                        Commands.argument("size", IntegerArgumentType.integer())
                                                            .executes { ctx -> execute.setBoard(ctx, "lights_out") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> lightsOutSuggestions(builder) }
                                                    .executes { ctx -> execute.resetBoard(ctx, "lights_out") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> lightsOutSuggestions(builder) }
                                                    .executes { ctx -> execute.removeBoard(ctx, "lights_out") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("display")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> lightsOutSuggestions(builder) }
                                                    .then(
                                                        Commands.argument("align", StringArgumentType.word())
                                                            .suggests { _, builder -> alignSuggestions(builder) }
                                                            .executes { ctx -> execute.setDisplay(ctx, "lights_out") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> lightsOutSuggestions(builder) }
                                                    .executes { ctx -> execute.removeDisplay(ctx, "lights_out") }
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("connect_four")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .then(
                                                        Commands.argument("align", StringArgumentType.word())
                                                            .suggests { _, builder -> alignSuggestions(builder) }
                                                            .executes { ctx -> execute.setBoard(ctx, "connect_four") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> connectFourSuggestions(builder) }
                                                    .executes { ctx -> execute.resetBoard(ctx, "connect_four") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> connectFourSuggestions(builder) }
                                                    .executes { ctx -> execute.removeBoard(ctx, "connect_four") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("move")
                                    .then(
                                        Commands.argument("name", StringArgumentType.word())
                                            .suggests { _, builder -> connectFourSuggestions(builder) }
                                            .then(
                                                Commands.argument("line", IntegerArgumentType.integer())
                                                    .executes { ctx -> execute.connectFourMove(ctx) }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("get_player")
                                    .then(
                                        Commands.argument("name", StringArgumentType.word())
                                            .suggests { _, builder -> connectFourSuggestions(builder) }
                                            .executes { ctx -> execute.getPlayer(ctx, "connect_four") }
                                    )
                            )
                    )
                    .then(
                        Commands.literal("score_four")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("set")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .executes { ctx -> execute.setBoard(ctx, "score_four") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> scoreFourSuggestions(builder) }
                                                    .executes { ctx -> execute.resetBoard(ctx, "score_four") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> scoreFourSuggestions(builder) }
                                                    .executes { ctx -> execute.removeBoard(ctx, "score_four") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("get_player")
                                    .then(
                                        Commands.argument("name", StringArgumentType.word())
                                            .suggests { _, builder -> scoreFourSuggestions(builder) }
                                            .executes { ctx -> execute.getPlayer(ctx, "score_four") }
                                    )
                            )
                    )
                    .then(
                        Commands.literal("save_config")
                            .executes { _ -> GameConfig.save(instance) }
                    )
            )
        }
    }

    private fun alignSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions?> {
        builder.suggest("x")
        builder.suggest("z")
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun chessSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions?> {
        instance.chess_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun gomokuSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions?> {
        instance.gomoku_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun reversiSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions?> {
        instance.reversi_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun lightsOutSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions?> {
        instance.lightsOut_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun connectFourSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions?> {
        instance.connectFour_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun scoreFourSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions?> {
        instance.scoreFour_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }
}
