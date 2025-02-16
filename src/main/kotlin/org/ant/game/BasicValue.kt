package org.ant.game

import org.bukkit.Location

interface BasicValue {
    val location: Location
    val center: Location
    val size: Int
}
