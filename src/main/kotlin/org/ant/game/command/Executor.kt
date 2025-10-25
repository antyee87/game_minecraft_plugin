package org.ant.game.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.ant.game.AntGamePlugin
import org.ant.game.GamesManager
import org.ant.game.gameimpl.Chess
import org.ant.game.gameimpl.ConnectFour
import org.ant.game.gameimpl.Go
import org.ant.game.gameimpl.LightsOut
import org.ant.game.gameimpl.ScoreFour
import org.ant.game.gameimpl.gameframe.BoardGame
import org.ant.game.gameimpl.gameframe.GameConstants
import org.ant.game.gameimpl.gameframe.GameSerializable
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class Executor(private val pluginInstance: AntGamePlugin) {
    companion object {
        val ERROR_SIZE = SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Invalid size!"))
        )
        val ERROR_GROUP_NAME = SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Invalid group name!"))
        )
    }
    fun setupGame(ctx: CommandContext<CommandSourceStack>, gameClass: KClass<out GameSerializable>): Int {
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
        val gameInstances = GamesManager.games[gameClass]

        if (gameInstances == null) {
            pluginInstance.logger.warning("[Executor] Invalid game name")
            return 0
        }

        if (!(gameInstances.containsKey(groupName))) {
            when (gameClass) {
                LightsOut::class -> {
                    val size = runCatching { ctx.getArgument("size", Int::class.java) }.getOrNull() ?: throw ERROR_SIZE.create()
                    gameInstances[groupName] = gameClass.primaryConstructor!!.call(size)
                }

                else -> gameInstances[groupName] = gameClass.primaryConstructor!!.call(pluginInstance)
            }
        }
        val game = gameInstances[groupName]as BoardGame
        when (gameClass) {
            Chess::class, ScoreFour::class -> game.setBoard(
                name,
                origin,
                cardinalDirection,
                GameConstants.Orientation.HORIZONTAL
            )
            ConnectFour::class -> game.setBoard(
                name,
                origin,
                cardinalDirection,
                GameConstants.Orientation.VERTICAL_POSITIVE
            )
            else -> game.setBoard(
                name,
                origin,
                cardinalDirection,
                orientation!!
            )
        }
        pluginInstance.logger.info("[Executor] Setup board($gameClass/$groupName/$name) success!")

        return Command.SINGLE_SUCCESS
    }

    fun resetBoard(ctx: CommandContext<CommandSourceStack>, gameClass: KClass<out GameSerializable>): Int {
        val groupName = ctx.getArgument("group_name", String::class.java)
        val game = GamesManager.games[gameClass]?.get(groupName) ?: throw ERROR_GROUP_NAME.create()
        if (game is BoardGame) {
            game.reset(null)
            return Command.SINGLE_SUCCESS
        } else {
            return 0
        }
    }

    fun removeBoard(ctx: CommandContext<CommandSourceStack>, gameClass: KClass<out GameSerializable>): Int {
        val groupName = ctx.getArgument("group_name", String::class.java)
        val name = runCatching {
            ctx.getArgument("name", String::class.java)
        }.getOrNull()
        val gameInstances = GamesManager.games[gameClass] ?: return 0
        val game = GamesManager.games[gameClass]!![groupName] ?: throw ERROR_GROUP_NAME.create()
        if (game is BoardGame) {
            game.remove(name)
            if (game.boards.isEmpty()) gameInstances.remove(groupName)
            return Command.SINGLE_SUCCESS
        } else {
            return 0
        }
    }

    fun listGame(ctx: CommandContext<CommandSourceStack>, gameClass: KClass<out GameSerializable>): Int {
        val groupName = runCatching {
            ctx.getArgument("group_name", String::class.java)
        }.getOrNull()
        val gameInstances = GamesManager.games[gameClass] ?: return 0
        val game = GamesManager.games[gameClass]!![groupName]
        val component = Component.text()
        component
            .append(Component.text("Game Name: ", NamedTextColor.BLUE))
            .append(Component.text(GamesManager.gameNames[gameClass]!!, NamedTextColor.GREEN))
        if (game == null) {
            if (groupName != null) throw ERROR_GROUP_NAME.create()
            for (groupName in gameInstances.keys) {
                component.append(
                    Component.text("\n- ")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.BOLD, true)
                ).append(
                    Component.text(groupName)
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.UNDERLINED, true)
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text {
                                    val previewBoards = 5
                                    val boards = (gameInstances[groupName] as BoardGame).boards
                                    var count = 0
                                    it.append(Component.text("Board list: ", NamedTextColor.BLUE))
                                    for (boardName in boards.keys) {
                                        it.append(
                                            Component.text("\n- ")
                                                .color(NamedTextColor.GOLD)
                                                .decoration(TextDecoration.BOLD, true)
                                        ).append(
                                            Component.text(boardName)
                                                .color(NamedTextColor.GREEN)
                                        )
                                        ++count
                                        if (count >= previewBoards) break
                                    }
                                    if (boards.count() > previewBoards) {
                                        it.append(
                                            Component.text("\n... Other ${boards.count() - 5}")
                                        )
                                    }
                                }
                            )
                        )
                        .clickEvent(ClickEvent.runCommand("/antgame game_operate ${GamesManager.gameNames[gameClass]} list $groupName"))
                )
            }
        } else {
            component.append(
                Component.text(" Group Name: ", NamedTextColor.BLUE)
                    .append(Component.text(groupName!!, NamedTextColor.GREEN))
            )

            for ((boardName, board) in (game as BoardGame).boards) {
                component.append(
                    Component.text("\n- ")
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.BOLD, true)
                ).append(
                    Component.text(boardName)
                        .decoration(TextDecoration.UNDERLINED, true)
                        .color(NamedTextColor.GREEN)
                        .hoverEvent(
                            HoverEvent.showText(
                                Component.text("Tp to ")
                                    .append(Component.text("World: ", NamedTextColor.BLUE))
                                    .append(Component.text(board.origin.world.key.asString(), NamedTextColor.GREEN))
                                    .append(Component.text(", Position: ", NamedTextColor.BLUE))
                                    .append(Component.text("${board.origin.x} ${board.origin.y} ${board.origin.z}", NamedTextColor.GREEN))
                            )
                        )
                        .clickEvent(ClickEvent.runCommand("/execute in ${board.origin.world.key.asString()} positioned ${board.origin.x + 0.5} ${board.origin.y + 0.5} ${board.origin.z + 0.5} run tp @s ~ ~ ~"))
                )
            }
        }
        ctx.source.sender.sendMessage(component)
        return Command.SINGLE_SUCCESS
    }

    fun setupGameArea(ctx: CommandContext<CommandSourceStack>): Int {
        val name = ctx.getArgument("name", String::class.java)

        @Suppress("UnstableApiUsage")
        val pos1 = ctx.getArgument("pos1", BlockPositionResolver::class.java).resolve(ctx.source).toLocation(ctx.source.location.world)

        @Suppress("UnstableApiUsage")
        val pos2 = ctx.getArgument("pos2", BlockPositionResolver::class.java).resolve(ctx.source).toLocation(ctx.source.location.world)

        pluginInstance.gameAreaManager.setupGameArea(name, pos1, pos2)

        return Command.SINGLE_SUCCESS
    }

    fun removeGameArea(ctx: CommandContext<CommandSourceStack>): Int {
        val name = ctx.getArgument("name", String::class.java)
        if (pluginInstance.gameAreaManager.gameAreas.containsKey(name)) {
            pluginInstance.gameAreaManager.gameAreas.remove(name)
            return Command.SINGLE_SUCCESS
        } else {
            return 0
        }
    }

    fun listGameArea(ctx: CommandContext<CommandSourceStack>): Int {
        val name = ctx.getArgument("name", String::class.java)
        val component = Component.text()
        component.append(
            Component.text("Game Area Name: ", NamedTextColor.BLUE)
                .append(Component.text(name, NamedTextColor.GREEN))
        )

        for ((name, gameArea) in pluginInstance.gameAreaManager.gameAreas) {
            component.append(
                Component.text("\n- ")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true)
            ).append(
                Component.text(name)
                    .decoration(TextDecoration.UNDERLINED, true)
                    .color(NamedTextColor.GREEN)
                    .hoverEvent(
                        HoverEvent.showText(
                            Component.text("Tp to ")
                                .append(Component.text("World: ", NamedTextColor.BLUE))
                                .append(Component.text(gameArea.first.world.key.asString(), NamedTextColor.GREEN))
                                .append(Component.text(", Position: ", NamedTextColor.BLUE))
                                .append(Component.text("${gameArea.first.x} ${gameArea.first.y} ${gameArea.first.z}", NamedTextColor.GREEN))
                        )
                    )
                    .clickEvent(ClickEvent.runCommand("/execute in ${gameArea.first.world.key.asString()} positioned ${gameArea.first.x + 0.5} ${gameArea.first.y + 0.5} ${gameArea.first.z + 0.5} run tp @s ~ ~ ~"))
            )
        }
        ctx.source.sender.sendMessage(component)
        return Command.SINGLE_SUCCESS
    }

    fun getSgf(ctx: CommandContext<CommandSourceStack>): Int {
        val groupName = ctx.getArgument("group_name", String::class.java)
        val sgfText = (GamesManager.games[Go::class]!![groupName] as? Go)?.exportToSGF() ?: throw ERROR_GROUP_NAME.create()
        ctx.source.executor?.sendMessage(
            Component.text("點此複製sgf", NamedTextColor.GREEN)
                .decoration(TextDecoration.UNDERLINED, true)
                .clickEvent(ClickEvent.copyToClipboard(sgfText))
        )
        if (pluginInstance.settingsManager.settings["sgf_directory"] != "") {
            val directoryPath = pluginInstance.settingsManager.settings["sgf_directory"] as String
            if (Path(directoryPath).exists()) {
                File("$directoryPath/$groupName.sgf").writeText(sgfText)
                val sgfShareUrl = pluginInstance.settingsManager.settings["sgf_share_url"] as String
                if (sgfShareUrl != "") {
                    ctx.source.executor?.sendMessage(
                        Component.text("點此下載sgf", NamedTextColor.GREEN)
                            .decoration(TextDecoration.UNDERLINED, true)
                            .clickEvent(ClickEvent.openUrl("$sgfShareUrl/$groupName.sgf"))
                    )
                }
            }
        }
        return Command.SINGLE_SUCCESS
    }
}
