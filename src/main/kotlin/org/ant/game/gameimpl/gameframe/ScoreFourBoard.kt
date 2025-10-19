package org.ant.game.gameimpl.gameframe

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector

class ScoreFourBoard(
    origin: Location,
    center: Location,
    xAxis: Vector,
    yAxis: Vector,
    size: Int
) : Board(
    origin,
    center,
    xAxis,
    yAxis,
    size
) {
    override fun remove() {
        for (x in 0..<size) {
            for (y in 0..<size) {
                val location = origin.clone()
                    .add(xAxis.clone().multiply(2 * x))
                    .add(yAxis.clone().multiply(2 * y))
                for (z in 0..<size) {
                    location.block.type = Material.AIR
                    location.add(0.0, 1.0, 0.0)
                }
            }
        }
    }
}
