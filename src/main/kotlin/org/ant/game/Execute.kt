package org.ant.game

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.ant.plugin.Chess
import org.ant.plugin.ConnectFour
import org.ant.plugin.Gomoku
import org.ant.plugin.LightsOut
import org.ant.plugin.Reversi
import org.ant.plugin.ScoreFour
import java.util.Optional

@Suppress("UnstableApiUsage")
class Execute(private val instance: Game) {
    fun setBoard(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        ctx.source.executor?.let { executor ->
            val location = executor.location.block.location
            val name = ctx.getArgument("name", String::class.java)
            when (mode) {
                "chess" -> instance.chess_games[name] = Chess(location)
                "gomoku" -> instance.gomoku_games[name] = Gomoku(location, Optional.empty(), Optional.empty())
                "reversi" -> instance.reversi_games[name] = Reversi(location, Optional.empty(), Optional.empty())
                "lights_out" -> {
                    val size = ctx.getArgument("size", Int::class.javaPrimitiveType)
                    instance.lightsOut_games[name] = LightsOut(location, size, Optional.empty(), Optional.empty())
                }
                "connect_four" -> {
                    val align = ctx.getArgument("align", String::class.java)
                    instance.connectFour_games[name] = ConnectFour(location, align)
                }
                "score_four" -> instance.scoreFour_games.put(name, ScoreFour(location))
                else -> {}
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun setDisplay(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        val align = ctx.getArgument("align", String::class.java)
        val executor = ctx.source.executor
        if (executor != null) {
            val location = executor.location.block.location
            when (mode) {
                "gomoku" -> if (instance.gomoku_games.containsKey(name)) {
                    val gomoku = instance.gomoku_games[name]!!
                    gomoku.remove_display()
                    gomoku.set_display(location, align)
                }

                "reversi" -> if (instance.reversi_games.containsKey(name)) {
                    val reversi = instance.reversi_games[name]!!
                    reversi.remove_display()
                    reversi.set_display(location, align)
                }

                "lights_out" -> if (instance.lightsOut_games.containsKey(name)) {
                    val lightsOut = instance.lightsOut_games[name]!!
                    lightsOut.remove_display()
                    lightsOut.set_display(location, align)
                }
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun resetBoard(ctx: CommandContext<CommandSourceStack?>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "chess" -> instance.chess_games[name]?.reset()
            "gomoku" -> instance.gomoku_games[name]?.reset()
            "reversi" -> instance.reversi_games[name]?.reset()
            "lights_out" -> instance.lightsOut_games[name]?.reset()
            "connect_four" -> instance.connectFour_games[name]?.reset()
            "score_four" -> instance.scoreFour_games[name]?.reset()
        }
        return Command.SINGLE_SUCCESS
    }

    fun removeBoard(ctx: CommandContext<CommandSourceStack?>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "chess" -> if (instance.chess_games.containsKey(name)) {
                instance.chess_games[name]!!.remove()
                instance.chess_games.remove(name)
            }

            "gomoku" -> if (instance.gomoku_games.containsKey(name)) {
                instance.gomoku_games[name]!!.remove()
                instance.gomoku_games[name]!!.remove_display()
                instance.gomoku_games.remove(name)
            }

            "reversi" -> if (instance.reversi_games.containsKey(name)) {
                instance.reversi_games[name]!!.remove()
                instance.reversi_games[name]!!.remove_display()
                instance.reversi_games.remove(name)
            }

            "lights_out" -> if (instance.lightsOut_games.containsKey(name)) {
                instance.lightsOut_games[name]!!.remove()
                instance.lightsOut_games[name]!!.remove_display()
                instance.lightsOut_games.remove(name)
            }

            "connect_four" -> if (instance.connectFour_games.containsKey(name)) {
                instance.connectFour_games[name]!!.reset()
                instance.connectFour_games.remove(name)
            }

            "score_four" -> if (instance.scoreFour_games.containsKey(name)) {
                instance.scoreFour_games[name]!!.remove()
                instance.scoreFour_games.remove(name)
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun removeDisplay(ctx: CommandContext<CommandSourceStack?>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "gomoku" -> instance.gomoku_games[name]?.remove_display()
            "reversi" -> instance.reversi_games[name]?.remove_display()
            "lights_out" -> instance.lightsOut_games[name]?.remove_display()
        }
        return Command.SINGLE_SUCCESS
    }

    fun getPlayer(ctx: CommandContext<CommandSourceStack?>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "chess" -> {
                if (instance.chess_games.containsKey(name)) return instance.chess_games[name]!!.player + 1
                if (instance.gomoku_games.containsKey(name)) return instance.gomoku_games[name]!!.player
                if (instance.reversi_games.containsKey(name)) return instance.reversi_games[name]!!.player
                if (instance.connectFour_games.containsKey(name)) return instance.connectFour_games[name]!!.player
                if (instance.scoreFour_games.containsKey(name)) return instance.scoreFour_games[name]!!.player
            }
            "gomoku" -> {
                if (instance.gomoku_games.containsKey(name)) return instance.gomoku_games[name]!!.player
                if (instance.reversi_games.containsKey(name)) return instance.reversi_games[name]!!.player
                if (instance.connectFour_games.containsKey(name)) return instance.connectFour_games[name]!!.player
                if (instance.scoreFour_games.containsKey(name)) return instance.scoreFour_games[name]!!.player
            }
            "reversi" -> {
                if (instance.reversi_games.containsKey(name)) return instance.reversi_games[name]!!.player
                if (instance.connectFour_games.containsKey(name)) return instance.connectFour_games[name]!!.player
                if (instance.scoreFour_games.containsKey(name)) return instance.scoreFour_games[name]!!.player
            }
            "connect_four" -> {
                if (instance.connectFour_games.containsKey(name)) return instance.connectFour_games[name]!!.player
                if (instance.scoreFour_games.containsKey(name)) return instance.scoreFour_games[name]!!.player
            }
            "score_four" -> if (instance.scoreFour_games.containsKey(name)) return instance.scoreFour_games[name]!!.player
        }
        return -1
    }

    fun connectFourMove(ctx: CommandContext<CommandSourceStack>): Int {
        val name = ctx.getArgument("name", String::class.java)
        val line = ctx.getArgument("line", Int::class.java)
        instance.connectFour_games[name]?.move(line)
        return Command.SINGLE_SUCCESS
    }
}
