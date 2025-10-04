package org.ant.game.gameimpl.gameframe

import org.bukkit.Location
import org.bukkit.util.Vector

interface BasicValue {
    val origin: Location
    val center: Location
    val xAxis: Vector
    val yAxis: Vector
    val size: Int
}
