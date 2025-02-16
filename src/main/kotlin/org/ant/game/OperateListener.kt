package org.ant.game

import org.ant.plugin.Chess
import org.ant.plugin.ConnectFour
import org.ant.plugin.ScoreFour
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
import java.util.UUID

@Suppress("PrivatePropertyName")
class OperateListener(private val instance: Game) : Listener {
    private val chess_games = instance.chess_games.values
    private val gomoku_games = instance.gomoku_games.values
    private val reversi_games = instance.reversi_games.values
    private val lightsOut_games = instance.lightsOut_games.values
    private val connectFours_games = instance.connectFour_games.values
    private val scoreFour_games = instance.scoreFour_games.values

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
                    val point = block.location
                    found = false

                    chess_games.forEach { game ->
                        val board = game.location
                        val x = point.blockX - board.blockX
                        val y = point.blockY - board.blockY
                        val z = point.blockZ - board.blockZ
                        if (Method.isInRange(y, 0, 1) && game.promotable == null) {
                            if (game.is_inside(x, z)) {
                                if (game.move(x, z, y, player)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                                found = true
                            }
                        } else if (Method.isInRange(y, 2, 5) && game.promotable != null) {
                            if (game.is_inside(x, z)) {
                                game.promote(y, player)
                                block.world.playSound(block.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                                return@forEach
                            }
                        }
                    }
                    if (found) return

                    simpleGameClick(gomoku_games, block, player)
                    if (found) return

                    simpleGameClick(reversi_games, block, player)
                    if (found) return

                    simpleGameClick(lightsOut_games, block, player)
                    if (found) return

                    connectFours_games.forEach { game: ConnectFour ->
                        val board = game.location
                        val x = point.blockX - board.blockX
                        val y = point.blockY - board.blockY
                        val z = point.blockZ - board.blockZ
                        if (y <= 6) {
                            if (game.align == "x" && point.blockZ == board.blockZ && game.is_inside(0, x)) {
                                if (game.move(x, player)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                                found = true
                            } else if (game.align == "z" && point.blockX == board.blockX && game.is_inside(0, z)) {
                                if (game.move(z, player)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                                found = true
                            }
                        }
                    }
                    if (found) return

                    scoreFour_games.forEach { game: ScoreFour ->
                        val board = game.location
                        val x = (point.blockX - board.blockX) / 2
                        val y = point.blockY - board.blockY
                        val z = (point.blockZ - board.blockZ) / 2
                        if (Method.isInRange(y, 0, 4)) {
                            if (game.is_inside(x, z, 0)) {
                                if (game.move(x, z, player)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                                found = true
                            }
                        }
                    }
                    if (found) return
                }
            }
        }
    }

    private fun <T : BoardGame> simpleGameClick(games: Collection<T>, block: Block, player: Player) {
        val point = block.location
        games.forEach { game: T ->
            val board = game.location
            val x = point.blockX - board.blockX
            val z = point.blockZ - board.blockZ
            if (point.blockY == board.blockY && game.is_inside(x, z)) {
                if (game.move(x, z, player)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                found = true
            }
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.hasChangedPosition()) {
            val player = event.player
            if (player.gameMode == GameMode.ADVENTURE || player.gameMode == GameMode.SURVIVAL) {
                set_fly(scoreFour_games, player)
            }
        }
    }

    @Suppress("FunctionName")
    private fun <T : BasicValue> set_fly(games: Collection<T>, player: Player) {
        val location = player.location

        games.forEach { game: T ->
            val center = game.center
            val distance = location.distance(center)
            if (distance < game.size.toDouble() / 2 + 5) {
                player.setMetadata(center.toString() + "is_flying", FixedMetadataValue(instance, true))
                player.allowFlight = true
            } else if (player.hasMetadata(center.toString() + "is_flying")) {
                player.removeMetadata(center.toString() + "is_flying", instance)
                player.allowFlight = false
            }
        }
    }
}
