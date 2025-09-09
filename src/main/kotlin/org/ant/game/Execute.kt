package org.ant.game

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import org.ant.game.gameimpl.Chess
import org.ant.game.gameimpl.ConnectFour
import org.ant.game.gameimpl.Gomoku
import org.ant.game.gameimpl.LightsOut
import org.ant.game.gameimpl.Reversi
import org.ant.game.gameimpl.ScoreFour

class Execute(private val instance: Game) {
    fun setBoard(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        val executor = ctx.source.executor
        if (executor != null) {
            val location = executor.location.block.location
            val name = ctx.getArgument("name", String::class.java)
            when (mode) {
                "chess" -> instance.chessGames[name] = Chess(location)
                "gomoku" -> instance.gomokuGames[name] = Gomoku(instance, location, null, null)
                "reversi" -> instance.reversiGames[name] = Reversi(instance, location, null, null)
                "lights_out" -> {
                    val size = ctx.getArgument("size", Int::class.java)
                    instance.lightsOutGames[name] = LightsOut(location, size, null, null)
                }
                "connect_four" -> {
                    val align = ctx.getArgument("align", String::class.java)
                    instance.connectFourGames[name] = ConnectFour(instance, location, align)
                }
                "score_four" -> instance.scoreFourGames[name] = ScoreFour(instance, location)
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
                "gomoku" -> if (instance.gomokuGames.containsKey(name)) {
                    val gomoku = instance.gomokuGames[name]!!
                    gomoku.removeDisplay()
                    gomoku.setDisplay(location, align)
                }
                "reversi" -> if (instance.reversiGames.containsKey(name)) {
                    val reversi = instance.reversiGames[name]!!
                    reversi.removeDisplay()
                    reversi.setDisplay(location, align)
                }
                "lights_out" -> if (instance.lightsOutGames.containsKey(name)) {
                    val lightsOut = instance.lightsOutGames[name]!!
                    lightsOut.removeDisplay()
                    lightsOut.setDisplay(location, align)
                }
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun resetBoard(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "chess" -> instance.chessGames[name]?.reset()
            "gomoku" -> instance.gomokuGames[name]?.reset(null)
            "reversi" -> instance.reversiGames[name]?.reset(null)
            "lights_out" -> instance.lightsOutGames[name]?.reset(null)
            "connect_four" -> instance.connectFourGames[name]?.reset(null, null)
            "score_four" -> instance.scoreFourGames[name]?.reset(null, null)
        }
        return Command.SINGLE_SUCCESS
    }

    fun removeBoard(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "chess" -> if (instance.chessGames.containsKey(name)) {
                instance.chessGames[name]!!.remove()
                instance.chessGames.remove(name)
            }
            "gomoku" -> if (instance.gomokuGames.containsKey(name)) {
                instance.gomokuGames[name]!!.remove()
                instance.gomokuGames[name]!!.removeDisplay()
                instance.gomokuGames.remove(name)
            }
            "reversi" -> if (instance.reversiGames.containsKey(name)) {
                instance.reversiGames[name]!!.remove()
                instance.reversiGames[name]!!.removeDisplay()
                instance.reversiGames.remove(name)
            }
            "lights_out" -> if (instance.lightsOutGames.containsKey(name)) {
                instance.lightsOutGames[name]!!.remove()
                instance.lightsOutGames[name]!!.removeDisplay()
                instance.lightsOutGames.remove(name)
            }
            "connect_four" -> if (instance.connectFourGames.containsKey(name)) {
                instance.connectFourGames[name]!!.remove()
                instance.connectFourGames.remove(name)
            }
            "score_four" -> if (instance.scoreFourGames.containsKey(name)) {
                instance.scoreFourGames[name]!!.remove()
                instance.scoreFourGames.remove(name)
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun removeDisplay(ctx: CommandContext<CommandSourceStack>, mode: String): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            "gomoku" -> if (instance.gomokuGames.containsKey(name)) instance.gomokuGames[name]!!.removeDisplay()
            "reversi" -> if (instance.reversiGames.containsKey(name)) instance.reversiGames[name]!!.removeDisplay()
            "lights_out" -> if (instance.lightsOutGames.containsKey(name)) instance.lightsOutGames[name]!!.removeDisplay()
        }
        return Command.SINGLE_SUCCESS
    }
}
