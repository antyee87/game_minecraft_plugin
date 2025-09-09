package org.ant.game

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.dedicated.DedicatedServer
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftServer
import java.util.concurrent.CompletableFuture

class GameCommand(private val instance: Game) {
    val nmsServer: DedicatedServer = (Bukkit.getServer() as CraftServer).server
    val commands: Commands = nmsServer.commands
    val dispatcher: CommandDispatcher<CommandSourceStack> = commands.dispatcher

    lateinit var registeredCommands: List<CommandNode<CommandSourceStack>>

    fun register() {
        val antGameCommand = Commands.literal("antgame")
            .then(
                Commands.literal("game_operate")
                    .requires { ctx -> ctx.sender.hasPermission("antgame.command.game_operate") }
                    .then(
                        Commands.literal("chess")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .executes { ctx -> Execute(instance).setBoard(ctx, "chess") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> chessSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).resetBoard(ctx, "chess") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> chessSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeBoard(ctx, "chess") }
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("gomoku")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .executes { ctx -> Execute(instance).setBoard(ctx, "gomoku") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> gomokuSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).resetBoard(ctx, "gomoku") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> gomokuSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeBoard(ctx, "gomoku") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("display")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> gomokuSuggestions(builder) }
                                                    .then(
                                                        Commands.argument("align", StringArgumentType.word())
                                                            .suggests { _, builder -> alignSuggestions(builder) }
                                                            .executes { ctx -> Execute(instance).setDisplay(ctx, "gomoku") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> gomokuSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeDisplay(ctx, "gomoku") }
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("reversi")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .executes { ctx -> Execute(instance).setBoard(ctx, "reversi") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> reversiSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).resetBoard(ctx, "reversi") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> reversiSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeBoard(ctx, "reversi") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("display")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> reversiSuggestions(builder) }
                                                    .then(
                                                        Commands.argument("align", StringArgumentType.word())
                                                            .suggests { _, builder -> alignSuggestions(builder) }
                                                            .executes { ctx -> Execute(instance).setDisplay(ctx, "reversi") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> reversiSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeDisplay(ctx, "reversi") }
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("lights_out")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .then(
                                                        Commands.argument("size", IntegerArgumentType.integer())
                                                            .executes { ctx -> Execute(instance).setBoard(ctx, "lights_out") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> lightsOutSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).resetBoard(ctx, "lights_out") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> lightsOutSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeBoard(ctx, "lights_out") }
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("display")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> lightsOutSuggestions(builder) }
                                                    .then(
                                                        Commands.argument("align", StringArgumentType.word())
                                                            .suggests { _, builder -> alignSuggestions(builder) }
                                                            .executes { ctx -> Execute(instance).setDisplay(ctx, "lights_out") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> lightsOutSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeDisplay(ctx, "lights_out") }
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("connect_four")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .then(
                                                        Commands.argument("align", StringArgumentType.word())
                                                            .suggests { _, builder -> alignSuggestions(builder) }
                                                            .executes { ctx -> Execute(instance).setBoard(ctx, "connect_four") }
                                                    )
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> connectFourSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).resetBoard(ctx, "connect_four") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> connectFourSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeBoard(ctx, "connect_four") }
                                            )
                                    )
                            )
                    )
                    .then(
                        Commands.literal("score_four")
                            .then(
                                Commands.literal("board")
                                    .then(
                                        Commands.literal("setup")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .executes { ctx -> Execute(instance).setBoard(ctx, "score_four") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("reset")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> scoreFourSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).resetBoard(ctx, "score_four") }
                                            )
                                    )
                                    .then(
                                        Commands.literal("remove")
                                            .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                    .suggests { _, builder -> scoreFourSuggestions(builder) }
                                                    .executes { ctx -> Execute(instance).removeBoard(ctx, "score_four") }
                                            )
                                    )
                            )
                    )
            )
            .then(
                Commands.literal("save_config")
                    .requires { sender -> sender.sender.hasPermission("antgame.command.save_config") }
                    .executes { instance.gameConfig.save() }
            )
        registeredCommands = listOf(
            dispatcher.register(antGameCommand)
        )
    }

    private fun alignSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        builder.suggest("x")
        builder.suggest("z")
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun chessSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.chessGames.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun gomokuSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.gomokuGames.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun reversiSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.reversiGames.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun lightsOutSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.lightsOutGames.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun connectFourSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.connectFourGames.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    private fun scoreFourSuggestions(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        instance.scoreFourGames.keys.forEach { text -> builder.suggest(text) }
        return CompletableFuture.completedFuture(builder.build())
    }

    fun unregister() {
        if (::registeredCommands.isInitialized) {
            registeredCommands.forEach {
                dispatcher.root.removeCommand(it.name)
            }
        }
    }
}
