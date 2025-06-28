package org.ant.game

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.scheduler.BukkitTask

abstract class TwoColorBoardGame(
    @JvmField var gameInstance: Game,
    location: Location,
    displayLocation: Location?,
    displayAlign: String?,
    size: Int
) : BoardGame(location, displayLocation, displayAlign, size) {
    var selectedPoint: IntArray? = null

    @JvmField
    var displaySelectedTask: BukkitTask? = null

    override fun setDisplay(location: Location?, displayAlign: String?) {
        super.setDisplay(location, displayAlign)
        this.displayLocation = location
        this.displayAlign = displayAlign
    }

    override fun removeDisplay() {
        super.removeDisplay()
        displayLocation = null
        displayAlign = null
    }

    private fun material(status: Int): Material {
        return when (status) {
            0 -> Material.GLASS
            1 -> Material.BLACK_STAINED_GLASS
            2 -> Material.WHITE_STAINED_GLASS
            3 -> Material.YELLOW_STAINED_GLASS
            else -> Material.AIR
        }
    }

    var visible: Boolean = false

    init {
        this.center = location.clone()
        this.center.add(size.toDouble() / 2, 0.0, size.toDouble() / 2)
    }

    fun select(x: Int, y: Int, player: Int, init: Int) {
        selectedPoint = intArrayOf(x, y)
        visible = true
        displaySelectedTask = Bukkit.getScheduler().runTaskTimer(
            gameInstance,
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

    fun display(board: Array<IntArray?>) {
        for (x in 0..<size) {
            for (y in 0..<size) {
                displaySingle(x, y, board[x]!![y])
            }
        }
    }

    fun displaySingle(x: Int, y: Int, player: Int) {
        location.getWorld().setType(
            location.blockX + x,
            location.blockY,
            location.blockZ + y,
            material(player)
        )
        if (displayLocation != null) {
            if (displayAlign == "x") {
                displayLocation!!.getWorld().setType(
                    displayLocation!!.blockX + x,
                    displayLocation!!.blockY + y,
                    displayLocation!!.blockZ,
                    material(player)
                )
            } else if (displayAlign == "z") {
                displayLocation!!.getWorld().setType(
                    displayLocation!!.blockX,
                    displayLocation!!.blockY + x,
                    displayLocation!!.blockZ + y,
                    material(player)
                )
            }
        }
    }
}
