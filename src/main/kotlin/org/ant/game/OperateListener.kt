package org.ant.game

import org.ant.game.gameimpl.Chess
import org.ant.game.gameimpl.ScoreFour
import org.ant.game.gameimpl.gameframe.BoardGame
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import java.util.UUID

class OperateListener(private val pluginInstance: AntGamePlugin) : Listener {

    private val cooldowns = HashMap<UUID, Long>()

    var found = false

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action == Action.LEFT_CLICK_BLOCK || event.action == Action.LEFT_CLICK_AIR) {
            val currentTime = System.currentTimeMillis()
            val player = event.player
            if (!cooldowns.containsKey(player.uniqueId) || currentTime - cooldowns[player.uniqueId]!! > 100) {
                cooldowns[player.uniqueId] = currentTime
                val block = player.rayTraceBlocks(pluginInstance.settingsManager.settings["game_interaction_range"] as Double)?.hitBlock ?: return
                found = false
                for (gameClass in GamesManager.games.keys) {
                    val gameInstances = GamesManager.games[gameClass] ?: continue
                    when (gameClass) {
                        Chess::class -> {
                            @Suppress("UNCHECKED_CAST")
                            (gameInstances.values as Collection<BoardGame>).forEach { game ->
                                for (board in game.boards.values) {
                                    val vector = block.location.toVector().subtract(board.origin.toVector())
                                    val dx = vector.dot(board.xAxis).toInt()
                                    val dy = vector.dot(board.yAxis).toInt()
                                    val dz = vector.dot(Vector(0.0, 1.0, 0.0)).toInt()
                                    if (dz in 0..5 && game.isInside(dx, dy)) {
                                        if (game.move(dx, dy, dz, player)) {
                                            block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                                        }
                                        return
                                    }
                                }
                            }
                        }
                        ScoreFour::class -> {
                            @Suppress("UNCHECKED_CAST")
                            (gameInstances.values as Collection<BoardGame>).forEach { game ->
                                for (board in game.boards.values) {
                                    if (block.world != board.origin.world) {
                                        continue
                                    }
                                    val vector = block.location.toVector().subtract(board.origin.toVector())
                                    var dx = vector.dot(board.xAxis).toInt()
                                    var dy = vector.dot(board.yAxis).toInt()
                                    if (dx % 2 == 0 && dy % 2 == 0) {
                                        dx /= 2
                                        dy /= 2
                                    } else {
                                        continue
                                    }
                                    val dz = vector.dot(Vector(0.0, 1.0, 0.0)).toInt()
                                    if (dz in 0..4 && game.isInside(dx, dy)) {
                                        if (game.move(dx, dy, 0, player)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                                        return
                                    }
                                }
                            }
                        }
                        else -> {
                            @Suppress("UNCHECKED_CAST")
                            found = simpleGameClick(gameInstances.values as Collection<BoardGame>, block, player)
                            if (found) return
                        }
                    }
                }
            }
        }
    }

    /**
     * If board is square, then the game can do move by this method.
     * Calculate orthographic projection on xAxis, yAxis, zAxis(xAxis cross yAxis)
     */
    private fun <T : BoardGame> simpleGameClick(games: Collection<T>, block: Block, player: Player): Boolean {
        val clickedPoint = block.location
        games.forEach { game ->
            for (board in game.boards.values) {
                if (clickedPoint.world != board.origin.world) {
                    continue
                }
                val vector = clickedPoint.block.location.toVector().subtract(board.origin.toVector())
                val dx = vector.dot(board.xAxis).toInt()
                val dy = vector.dot(board.yAxis).toInt()
                val dz = vector.dot(board.xAxis.clone().crossProduct(board.yAxis)).toInt()
                if (dz == 0 && game.isInside(dx, dy)) {
                    if (game.move(dx, dy, 0, player)) {
                        block.world.playSound(
                            block.location,
                            Sound.BLOCK_STONE_BUTTON_CLICK_ON,
                            1.0f,
                            1.0f
                        )
                    }
                    return true
                }
            }
        }
        return false
    }
}
