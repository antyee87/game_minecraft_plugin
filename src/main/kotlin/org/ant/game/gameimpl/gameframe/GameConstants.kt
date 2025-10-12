package org.ant.game.gameimpl.gameframe

object GameConstants {
    enum class CardinalDirection {
        EAST,
        SOUTH,
        WEST,
        NORTH
    }

    enum class Orientation {
        VERTICAL_POSITIVE,
        VERTICAL_NEGATIVE,
        HORIZONTAL,
    }

    val eightDirection = arrayOf(
        intArrayOf(1, 0),
        intArrayOf(0, 1),
        intArrayOf(1, 1),
        intArrayOf(1, -1),
        intArrayOf(-1, 0),
        intArrayOf(0, -1),
        intArrayOf(-1, -1),
        intArrayOf(-1, 1)
    )
}
