package org.ant.game.gameimpl.gameframe

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector

open class Board(
    override val origin: Location,
    override val center: Location,
    override val xAxis: Vector,
    override val yAxis: Vector,
    override val size: Int
) : BasicValue {
    open fun remove() {
        for (x in 0..<size) {
            val location = origin.clone().add(xAxis.clone().multiply(x))
            for (y in 0..<size) {
                origin.world.setType(
                    location,
                    Material.AIR
                )
                location.add(yAxis)
            }
        }
    }
}
