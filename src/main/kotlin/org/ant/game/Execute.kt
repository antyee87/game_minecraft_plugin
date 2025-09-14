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
    fun setBoard(ctx: CommandContext<CommandSourceStack>, mode: GameDeSerializable): Int {
        val executor = ctx.source.executor
        if (executor != null) {
            val location = executor.location.block.location
            val name = ctx.getArgument("name", String::class.java)
            when (mode) {
                Chess -> instance.chessGames[name] = Chess(location)
                Gomoku -> instance.gomokuGames[name] = Gomoku(instance, location, null, null)
                Reversi -> instance.reversiGames[name] = Reversi(instance, location, null, null)
                LightsOut -> {
                    val size = ctx.getArgument("size", Int::class.java)
                    instance.lightsOutGames[name] = LightsOut(location, size, null, null)
                }
                ConnectFour -> {
                    val align = ctx.getArgument("align", String::class.java)
                    instance.connectFourGames[name] = ConnectFour(instance, location, align)
                }
                ScoreFour -> instance.scoreFourGames[name] = ScoreFour(instance, location)
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun setDisplay(ctx: CommandContext<CommandSourceStack>, mode: GameDeSerializable): Int {
        val name = ctx.getArgument("name", String::class.java)
        val align = ctx.getArgument("align", String::class.java)
        val executor = ctx.source.executor
        if (executor != null) {
            val location = executor.location.block.location
            when (mode) {
                Gomoku -> if (instance.gomokuGames.containsKey(name)) {
                    val gomoku = instance.gomokuGames[name]!!
                    gomoku.removeDisplay()
                    gomoku.setDisplay(location, align)
                }
                Reversi -> if (instance.reversiGames.containsKey(name)) {
                    val reversi = instance.reversiGames[name]!!
                    reversi.removeDisplay()
                    reversi.setDisplay(location, align)
                }
                LightsOut -> if (instance.lightsOutGames.containsKey(name)) {
                    val lightsOut = instance.lightsOutGames[name]!!
                    lightsOut.removeDisplay()
                    lightsOut.setDisplay(location, align)
                }
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun resetBoard(ctx: CommandContext<CommandSourceStack>, mode: GameDeSerializable): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            Chess -> instance.chessGames[name]?.reset()
            Gomoku -> instance.gomokuGames[name]?.reset(null)
            Reversi -> instance.reversiGames[name]?.reset(null)
            LightsOut -> instance.lightsOutGames[name]?.reset(null)
            ConnectFour -> instance.connectFourGames[name]?.reset(null, null)
            ScoreFour -> instance.scoreFourGames[name]?.reset(null, null)
        }
        return Command.SINGLE_SUCCESS
    }

    fun removeBoard(ctx: CommandContext<CommandSourceStack>, mode: GameDeSerializable): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            Chess -> if (instance.chessGames.containsKey(name)) {
                instance.chessGames[name]!!.remove()
                instance.chessGames.remove(name)
            }
            Gomoku -> if (instance.gomokuGames.containsKey(name)) {
                instance.gomokuGames[name]!!.remove()
                instance.gomokuGames[name]!!.removeDisplay()
                instance.gomokuGames.remove(name)
            }
            Reversi -> if (instance.reversiGames.containsKey(name)) {
                instance.reversiGames[name]!!.remove()
                instance.reversiGames[name]!!.removeDisplay()
                instance.reversiGames.remove(name)
            }
            LightsOut -> if (instance.lightsOutGames.containsKey(name)) {
                instance.lightsOutGames[name]!!.remove()
                instance.lightsOutGames[name]!!.removeDisplay()
                instance.lightsOutGames.remove(name)
            }
            ConnectFour -> if (instance.connectFourGames.containsKey(name)) {
                instance.connectFourGames[name]!!.remove()
                instance.connectFourGames.remove(name)
            }
            ScoreFour -> if (instance.scoreFourGames.containsKey(name)) {
                instance.scoreFourGames[name]!!.remove()
                instance.scoreFourGames.remove(name)
            }
        }
        return Command.SINGLE_SUCCESS
    }

    fun removeDisplay(ctx: CommandContext<CommandSourceStack>, mode: GameDeSerializable): Int {
        val name = ctx.getArgument("name", String::class.java)
        when (mode) {
            Gomoku -> if (instance.gomokuGames.containsKey(name)) instance.gomokuGames[name]!!.removeDisplay()
            Reversi -> if (instance.reversiGames.containsKey(name)) instance.reversiGames[name]!!.removeDisplay()
            LightsOut -> if (instance.lightsOutGames.containsKey(name)) instance.lightsOutGames[name]!!.removeDisplay()
        }
        return Command.SINGLE_SUCCESS
    }
}
