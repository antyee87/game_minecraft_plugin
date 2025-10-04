package org.ant.game.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import net.kyori.adventure.text.Component
import org.ant.game.AntGamePlugin
import org.ant.game.GamesManager
import org.ant.game.gameimpl.gameframe.BoardGame
import org.ant.game.gameimpl.gameframe.GameConstants
import kotlin.reflect.full.primaryConstructor

class Executor(private val instance: AntGamePlugin) {
    companion object {
        val ERROR_SIZE = SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("No valid size provided!"))
        )
    }
    fun setBoard(ctx: CommandContext<CommandSourceStack>, gameName: String): Int {
        @Suppress("UnstableApiUsage")
        val origin = ctx.getArgument(
            "origin",
            BlockPositionResolver::class.java
        ).resolve(ctx.source).toLocation(ctx.source.location.world)
        val cardinalDirection = ctx.getArgument(
            "cardinal_direction",
            GameConstants.CardinalDirection::class.java
        )
        val orientation = runCatching {
            ctx.getArgument(
                "orientation",
                GameConstants.Orientation::class.java
            )
        }.getOrNull()
        val groupName = ctx.getArgument("group_name", String::class.java)
        val name = ctx.getArgument("name", String::class.java)
        val gameInstances = GamesManager.games[gameName]

        if (gameInstances == null) {
            instance.logger.warning("[Executor] Invalid game name")
            return 0
        }

        val gameClass = GamesManager.gameClasses[gameName] ?: return 0
        if (!(gameInstances.containsKey(groupName))) {
            when (gameName) {
                "lightsOut" -> {
                    val size = runCatching { ctx.getArgument("size", Int::class.java) }.getOrNull() ?: throw ERROR_SIZE.create()
                    gameInstances[groupName] = gameClass.primaryConstructor!!.call(size)
                }

                else -> gameInstances[groupName] = gameClass.primaryConstructor!!.call(instance)
            }
        }
        val game = gameInstances[groupName]
        when (gameName) {
            "chess", "scoreFour" -> (game as BoardGame).setBoard(origin, cardinalDirection, GameConstants.Orientation.HORIZONTAL, name)
            "gomoku", "reversi", "lightsOut" -> (game as BoardGame).setBoard(origin, cardinalDirection, orientation!!, name)
            "connectFour" -> (game as BoardGame).setBoard(origin, cardinalDirection, GameConstants.Orientation.VERTICAL, name)
        }
        instance.logger.info("[Executor] Setup board($gameName/$groupName/$name) success!")

        return Command.SINGLE_SUCCESS
    }

    fun resetBoard(ctx: CommandContext<CommandSourceStack>, gameName: String): Int {
        val groupName = ctx.getArgument("group_name", String::class.java)
        val game = GamesManager.games[gameName]?.get(groupName) ?: return 0
        if (game is BoardGame) {
            game.reset(null)
            return Command.SINGLE_SUCCESS
        } else {
            return 0
        }
    }

    fun removeBoard(ctx: CommandContext<CommandSourceStack>, gameName: String): Int {
        val groupName = ctx.getArgument("group_name", String::class.java)
        val name = runCatching {
            ctx.getArgument("name", String::class.java)
        }.getOrNull()
        val gameInstances = GamesManager.games[gameName] ?: return 0
        val game = GamesManager.games[gameName]!![groupName] ?: return 0
        if (game is BoardGame) {
            game.remove(name)
            if (game.boards.isEmpty()) gameInstances.remove(groupName)
            return Command.SINGLE_SUCCESS
        } else {
            return 0
        }
    }
}
