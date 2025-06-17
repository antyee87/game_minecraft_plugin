package org.ant.game

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

open class BoardGame(var location: Location, displayLocation: Location?, displayAlign: String?, size: Int) {
    var center: Location
    var displayLocation: Location? = null
    var displayAlign: String? = null
    var size: Int

    init {
        displayLocation.let { this.displayLocation = it }
        displayAlign.let { this.displayAlign = it }
        this.size = size
        this.center = location.clone()
        center.add(size.toDouble() / 2, 0.0, size.toDouble() / 2)
    }

    open fun move(x: Int, z: Int, minecraft_player: Player): Boolean {
        return false
    }

    open fun setDisplay(location: Location?, display_align: String?) {
        this.displayLocation = location
        this.displayAlign = display_align
    }

    open fun isInside(x: Int, y: Int): Boolean {
        return x in 0..<size && y in 0..<size
    }

    open fun remove() {
        for (x in 0..<size) {
            for (y in 0..<size) {
                location.world.setType(
                    location.blockX + x,
                    location.blockY,
                    location.blockZ + y,
                    Material.AIR
                )
            }
        }
    }

    open fun removeDisplay() {
        if (this@BoardGame.displayLocation != null) {
            for (x in 0..<size) {
                for (y in 0..<size) {
                    if (this@BoardGame.displayAlign == "x") {
                        this@BoardGame.displayLocation!!.world.setType(
                            this@BoardGame.displayLocation!!.blockX + x,
                            this@BoardGame.displayLocation!!.blockY + y,
                            this@BoardGame.displayLocation!!.blockZ,
                            Material.AIR
                        )
                    } else if (this@BoardGame.displayAlign == "z") {
                        this@BoardGame.displayLocation!!.world.setType(
                            this@BoardGame.displayLocation!!.blockX,
                            this@BoardGame.displayLocation!!.blockY + x,
                            this@BoardGame.displayLocation!!.blockZ + y,
                            Material.AIR
                        )
                    }
                }
            }
        }
        this@BoardGame.displayLocation = null
        this@BoardGame.displayAlign = null
    }

    fun broadcast(message: Component) {
        Method.broadcast(message, center, size)
    }

    fun broadcast(message: String) {
        Method.broadcast(message, center, size)
    }

    fun firework(location: Location, isBlack: Boolean) {
        Method.firework(location, isBlack, Color.BLACK, Color.WHITE)
    }
}
