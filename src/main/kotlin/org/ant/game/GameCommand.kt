package org.ant.game

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import java.util.concurrent.CompletableFuture

@Suppress("UnstableApiUsage", "FunctionName")
class GameCommand(private val instance: Game) {
    fun register() {
        instance.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands: ReloadableRegistrarEvent<Commands> ->
            commands.registrar().dispatcher.register(
                Commands.literal("antgame")
                    .then(
                        Commands.literal("game_operate")
                            .requires { ctx -> ctx.sender.hasPermission("antgame.command.game_operate") }
                            .then(
                                Commands.literal("chess")
                                    .then(
                                        Commands.literal("board")
                                            .then(
                                                Commands.literal("set")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .executes { ctx -> Execute(instance).set_board(ctx, "chess") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("reset")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> chess_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).reset_board(ctx, "chess") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> chess_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_board(ctx, "chess") }
                                                    )
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("gomoku")
                                    .then(
                                        Commands.literal("board")
                                            .then(
                                                Commands.literal("set")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .executes { ctx -> Execute(instance).set_board(ctx, "gomoku") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("reset")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> gomoku_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).reset_board(ctx, "gomoku") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> gomoku_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_board(ctx, "gomoku") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("display")
                                            .then(
                                                Commands.literal("set")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> gomoku_suggestions(builder) }
                                                            .then(
                                                                Commands.argument("align", StringArgumentType.word())
                                                                    .suggests { _, builder -> align_suggestions(builder) }
                                                                    .executes { ctx -> Execute(instance).set_display(ctx, "gomoku") }
                                                            )
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> gomoku_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_display(ctx, "gomoku") }
                                                    )
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
                                                            .executes { ctx -> Execute(instance).set_board(ctx, "reversi") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("reset")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> reversi_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).reset_board(ctx, "reversi") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> reversi_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_board(ctx, "reversi") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("display")
                                            .then(
                                                Commands.literal("set")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> reversi_suggestions(builder) }
                                                            .then(
                                                                Commands.argument("align", StringArgumentType.word())
                                                                    .suggests { _, builder -> align_suggestions(builder) }
                                                                    .executes { ctx -> Execute(instance).set_display(ctx, "reversi") }
                                                            )
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> reversi_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_display(ctx, "reversi") }
                                                    )
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
                                                                    .executes { ctx -> Execute(instance).set_board(ctx, "lights_out") }
                                                            )
                                                    )
                                            )
                                            .then(
                                                Commands.literal("reset")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> lightsOut_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).reset_board(ctx, "lights_out") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> lightsOut_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_board(ctx, "lights_out") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("display")
                                            .then(
                                                Commands.literal("set")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> lightsOut_suggestions(builder) }
                                                            .then(
                                                                Commands.argument("align", StringArgumentType.word())
                                                                    .suggests { _, builder -> align_suggestions(builder) }
                                                                    .executes { ctx -> Execute(instance).set_display(ctx, "lights_out") }
                                                            )
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> lightsOut_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_display(ctx, "lights_out") }
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
                                                                    .suggests { _, builder -> align_suggestions(builder) }
                                                                    .executes { ctx -> Execute(instance).set_board(ctx, "connect_four") }
                                                            )
                                                    )
                                            )
                                            .then(
                                                Commands.literal("reset")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> connectFour_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).reset_board(ctx, "connect_four") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> connectFour_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_board(ctx, "connect_four") }
                                                    )
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
                                                            .executes { ctx -> Execute(instance).set_board(ctx, "score_four") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("reset")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> scoreFour_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).reset_board(ctx, "score_four") }
                                                    )
                                            )
                                            .then(
                                                Commands.literal("remove")
                                                    .then(
                                                        Commands.argument("name", StringArgumentType.word())
                                                            .suggests { _, builder -> scoreFour_suggestions(builder) }
                                                            .executes { ctx -> Execute(instance).remove_board(ctx, "score_four") }
                                                    )
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("save_config")
                            .requires { sender -> sender.sender.hasPermission("antgame.command.save_config") }
                            .executes { GameConfig.save(instance) }
                    )
            )
        }
    }

    private fun align_suggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        builder.suggest("x")
        builder.suggest("z")
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun chess_suggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.chess_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun gomoku_suggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.gomoku_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun reversi_suggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.reversi_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun lightsOut_suggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.lightsOut_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun connectFour_suggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.connectFour_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun scoreFour_suggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.scoreFour_games.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }
}
