package org.ant.game

import org.ant.plugin.BoardGame
import org.ant.plugin.Chess
import org.ant.plugin.Method
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

@Suppress("PrivatePropertyName", "ktlint:standard:property-naming")
class OperateListener(instance: Game) : Listener {
    private var chess_games = instance.chess_games.values
    private var gomoku_games = instance.gomoku_games.values
    private var reversi_games = instance.reversi_games.values
    private var lightsOut_games = instance.lightsOut_games.values
    private var scoreFour_games = instance.scoreFour_games.values

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.hand == EquipmentSlot.HAND) {
            if (block == null) {
                return
            } else {
                val point = block.location
                chess_games.forEach { game: Chess ->
                    val board = game.location
                    val x = point.blockX - board.blockX
                    val y = point.blockY - board.blockY
                    val z = point.blockZ - board.blockZ
                    if (Method.isInRange(y, 0, 1) && game.promotable == null) {
                        if (game.is_inside(x, z)) {
                            if (game.move(x, z, y)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                            return@forEach
                        }
                    } else if (Method.isInRange(y, 2, 5) && game.promotable != null) {
                        if (game.is_inside(x, z)) {
                            game.promote(y)
                            block.world.playSound(block.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                            return@forEach
                        }
                    }
                }
                simpleGameClick(gomoku_games, block, point)
                simpleGameClick(reversi_games, block, point)
                simpleGameClick(lightsOut_games, block, point)
                scoreFour_games.forEach { game ->
                    val board = game.location
                    val x = (point.blockX - board.blockX) / 2
                    val y = point.blockY - board.blockY
                    val z = (point.blockZ - board.blockZ) / 2
                    if (Method.isInRange(y, 0, 4)) {
                        if (game.is_inside(x, z, 0)) {
                            if (game.move(x, z)) block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                        }
                    }
                }
            }
        }
    }

    private fun <T : BoardGame?> simpleGameClick(games: Collection<T>, block: Block, point: Location) {
        games.forEach { game: T ->
            val board = game!!.location
            val x = point.blockX - board.blockX
            val z = point.blockZ - board.blockZ
            if (point.blockY == board.blockY && game.is_inside(x, z)) {
                if (game.move(x, z)) {
                    block.world.playSound(block.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
                }
            }
        }
    }
}
