package org.ant.game.gameimpl.gameframe

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

abstract class BoardGame(val size: Int) : GameSerializable {
    val boards = hashMapOf<String, Board>()

    abstract fun move(x: Int, y: Int, z: Int, movePlayer: Player): Boolean

    abstract fun display()

    open fun setBoard(origin: Location, cardinalDirection: GameConstants.CardinalDirection, orientation: GameConstants.Orientation, name: String) {
        if (boards.containsKey(name)) boards[name]!!.remove()
        val axisPair = Method.getAxis(cardinalDirection, orientation)
        val center = origin.clone()
            .add(axisPair.first.clone().multiply(size / 2.0))
            .add(axisPair.second.clone().multiply(size / 2.0))
        boards[name] = Board(origin, center, axisPair.first, axisPair.second, size)
        display()
    }

    open fun setBoard(origin: Location, xAxis: Vector, yAxis: Vector, name: String) {
        val center = origin.clone()
            .add(xAxis.clone().multiply(size / 2.0))
            .add(yAxis.clone().multiply(size / 2.0))
        boards[name] = Board(origin, center, xAxis, yAxis, size)
        display()
    }

    open fun isInside(x: Int, y: Int): Boolean {
        return x in 0..<size && y in 0..<size
    }

    abstract fun reset(gamePreset: GameState?)

    open fun remove(removed: String?) {
        val removed: List<String> = if (removed == null) {
            boards.keys.toList()
        } else {
            listOf(removed)
        }
        for (name in removed) {
            boards[name]?.remove()
            boards.remove(name)
        }
    }

    fun broadcast(message: Component) {
        for (board in boards.values) {
            Method.broadcast(message, board.center, size)
        }
    }

    fun broadcast(message: String) {
        for (board in boards.values) {
            Method.broadcast(message, board.center, size)
        }
    }
}
