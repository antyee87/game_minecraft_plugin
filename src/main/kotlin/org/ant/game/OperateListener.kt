package org.ant.game

import org.ant.game.gameimpl.gameframe.BasicValue
import org.ant.game.gameimpl.gameframe.BoardGame
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector
import java.util.UUID

class OperateListener(private val instance: AntGamePlugin) : Listener {

    private val cooldowns = HashMap<UUID, Long>()

    var found = false

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock
        val player = event.player
        val playerId = player.uniqueId
        val currentTime = System.currentTimeMillis()

        if (!cooldowns.containsKey(playerId) || currentTime - cooldowns[playerId]!! > 100) {
            cooldowns[playerId] = currentTime
            if (event.action == Action.RIGHT_CLICK_BLOCK && event.hand == EquipmentSlot.HAND) {
                if (block == null) {
                    return
                } else {
                    val location = block.location
                    found = false
                    for (gameName in GamesManager.games.keys) {
                        val gameInstances = GamesManager.games[gameName] ?: continue
                        when (gameName) {
                            "chess" -> {
                                @Suppress("UNCHECKED_CAST")
                                (gameInstances.values as Collection<BoardGame>).forEach { game ->
                                    for (board in game.boards.values) {
                                        val vector = location.block.location.toVector().subtract(board.origin.toVector())
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
                            "gomoku", "reversi", "lightsOut", "connectFour" -> {
                                @Suppress("UNCHECKED_CAST")
                                found = simpleGameClick(gameInstances.values as Collection<BoardGame>, block, player)
                                if (found) return
                            }
                            "scoreFour" -> {
                                @Suppress("UNCHECKED_CAST")
                                (gameInstances.values as Collection<BoardGame>).forEach { game ->
                                    for (board in game.boards.values) {
                                        if (location.world != board.origin.world) {
                                            continue
                                        }
                                        val vector = location.block.location.toVector().subtract(board.origin.toVector())
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
                    if (game.move(dx, dy, 0, player)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                    return true
                }
            }
        }
        return false
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.hasChangedPosition()) {
            val player = event.player
            if (player.gameMode == GameMode.ADVENTURE || player.gameMode == GameMode.SURVIVAL) {
            }
        }
    }

    private fun <T : BasicValue> setFly(games: Collection<T>, player: Player) {
        val location = player.location

        games.forEach { game: T ->
            val center = game.center
            val distance = location.distance(center)
            if (distance < game.size.toDouble() / 2 + 5) {
                if (player.allowFlight) return@forEach
                player.setMetadata(center.toString() + "is_flying", FixedMetadataValue(instance, true))
                player.allowFlight = true
            } else if (player.hasMetadata(center.toString() + "is_flying")) {
                player.removeMetadata(center.toString() + "is_flying", instance)
                player.allowFlight = false
            }
        }
    }
}
