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

@Suppress("UnstableApiUsage", "FunctionName")
class Execute(private val instance: Game) {
    fun set_board(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        val executor = ctx.source.executor
        if (executor != null) {
            val location = executor.location.block.location
            val name = ctx.getArgument("name", String::class.java)
            when (mode) {
                "chess" -> instance.chess_games[name] = Chess(location)
                "gomoku" -> instance.gomoku_games[name] = Gomoku(instance, location, Optional.empty(), Optional.empty())
                "reversi" -> instance.reversi_games[name] = Reversi(instance, location, Optional.empty(), Optional.empty())
                "lights_out" -> {
                    val size = ctx.getArgument("size", Int::class.java)
                    instance.lightsOut_games[name] = LightsOut(location, size, Optional.empty(), Optional.empty())
                }
                "connect_four" -> {
                    val align = ctx.getArgument("align", String::class.java)
                    instance.connectFour_games[name] = ConnectFour(instance, location, align)
                }
                "score_four" -> instance.scoreFour_games[name] = ScoreFour(instance, location)
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun set_display(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
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

    fun reset_board(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "chess" -> if (instance.chess_games.containsKey(name)) instance.chess_games[name]!!.reset()
            "gomoku" -> if (instance.gomoku_games.containsKey(name)) instance.gomoku_games[name]!!.reset()
            "reversi" -> if (instance.reversi_games.containsKey(name)) instance.reversi_games[name]!!.reset()
            "lights_out" -> if (instance.lightsOut_games.containsKey(name)) instance.lightsOut_games[name]!!.reset()
            "connect_four" -> if (instance.connectFour_games.containsKey(name)) instance.connectFour_games[name]!!.reset()
            "score_four" -> if (instance.scoreFour_games.containsKey(name)) instance.scoreFour_games[name]!!.reset()
        }
        return Command.SINGLE_SUCCESS
    }

    fun remove_board(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
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
                instance.connectFour_games[name]!!.remove()
                instance.connectFour_games.remove(name)
            }
            "score_four" -> if (instance.scoreFour_games.containsKey(name)) {
                instance.scoreFour_games[name]!!.remove()
                instance.scoreFour_games.remove(name)
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun remove_display(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "gomoku" -> if (instance.gomoku_games.containsKey(name)) instance.gomoku_games[name]!!.remove_display()
            "reversi" -> if (instance.reversi_games.containsKey(name)) instance.reversi_games[name]!!.remove_display()
            "lights_out" -> if (instance.lightsOut_games.containsKey(name)) instance.lightsOut_games[name]!!.remove_display()
        }
        return Command.SINGLE_SUCCESS
    }
}
