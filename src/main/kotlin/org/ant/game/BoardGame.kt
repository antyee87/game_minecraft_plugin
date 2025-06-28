package org.ant.game

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

abstract class BoardGame(
    @JvmField var location: Location,
    @JvmField var displayLocation: Location?,
    @JvmField var displayAlign: String?,
    @JvmField var size: Int
) {
    @JvmField
    var center: Location

    init {
        displayLocation.let { this.displayLocation = it }
        displayAlign.let { this.displayAlign = it }
        this.size = size
        this.center = location.clone()
        center.add(size.toDouble() / 2, 0.0, size.toDouble() / 2)
    }

    open fun move(x: Int, z: Int, player: Player): Boolean {
        return false
    }

    open fun setDisplay(location: Location?, displayAlign: String?) {
        this.displayLocation = location
        this.displayAlign = displayAlign
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
        if (this.displayLocation != null) {
            for (x in 0..<size) {
                for (y in 0..<size) {
                    if (this.displayAlign == "x") {
                        this.displayLocation!!.world.setType(
                            this.displayLocation!!.blockX + x,
                            this.displayLocation!!.blockY + y,
                            this.displayLocation!!.blockZ,
                            Material.AIR
                        )
                    } else if (this.displayAlign == "z") {
                        this.displayLocation!!.world.setType(
                            this.displayLocation!!.blockX,
                            this.displayLocation!!.blockY + x,
                            this.displayLocation!!.blockZ + y,
                            Material.AIR
                        )
                    }
                }
            }
        }
        this.displayLocation = null
        this.displayAlign = null
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
