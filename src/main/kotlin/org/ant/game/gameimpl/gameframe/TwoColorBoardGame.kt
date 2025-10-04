package org.ant.game.gameimpl.gameframe

import org.ant.game.AntGamePlugin
import org.ant.game.gameimpl.Gomoku.Companion.SIZE
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.scheduler.BukkitTask

abstract class TwoColorBoardGame(
    var pluginInstance: AntGamePlugin,
    size: Int
) : BoardGame(size) {

    var boardState = Array(SIZE) { IntArray(SIZE) }
    var player: Int = 0
    var end: Boolean = false
    val uuidPair = UUIDPair()
    var displaySelectedTask: BukkitTask? = null

    private fun material(status: Int): Material {
        return when (status) {
            0 -> Material.GLASS
            1 -> Material.BLACK_STAINED_GLASS
            2 -> Material.WHITE_STAINED_GLASS
            3 -> Material.YELLOW_STAINED_GLASS
            else -> Material.AIR
        }
    }

    var visible = true
    fun select(x: Int, y: Int, player: Int, init: Int) {
        visible = true
        displaySelectedTask = Bukkit.getScheduler().runTaskTimer(
            pluginInstance,
            Runnable {
                if (visible) {
                    displaySingle(x, y, player)
                } else {
                    displaySingle(x, y, init)
                }
                visible = !visible
            },
            0,
            10
        )
    }

    override fun display() {
        for (board in boards.values) {
            for (x in 0..<size) {
                val location = board.origin.clone().add(board.xAxis.clone().multiply(x))
                for (y in 0..<size) {
                    location.getWorld().setType(
                        location,
                        material(boardState[x][y])
                    )
                    location.add(board.yAxis)
                }
            }
        }
    }

    fun displaySingle(x: Int, y: Int, player: Int) {
        for (board in boards.values) {
            val location = board.origin.clone()
                .add(board.xAxis.clone().multiply(x))
                .add(board.yAxis.clone().multiply(y))
            location.getWorld().setType(
                location,
                material(player)
            )
        }
    }
}
