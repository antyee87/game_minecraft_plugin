package org.ant.game.gameimpl

import org.ant.game.AntGamePlugin
import org.ant.game.gameimpl.gameframe.BoardGame
import org.ant.game.gameimpl.gameframe.GameDeSerializable
import org.ant.game.gameimpl.gameframe.GameSerializable
import org.ant.game.gameimpl.gameframe.GameState
import org.ant.game.gameimpl.gameframe.Method
import org.ant.game.gameimpl.gameframe.RecordSerializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.type.CopperBulb
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class LightsOut(size: Int) :
    BoardGame(size),
    GameSerializable,
    RecordSerializable {
    companion object : GameDeSerializable {
        var vectors = arrayOf<IntArray>(intArrayOf(0, 0), intArrayOf(1, 0), intArrayOf(0, 1), intArrayOf(-1, 0), intArrayOf(0, -1))

        override fun deserialize(pluginInstance: AntGamePlugin, args: Map<String, Any?>): GameSerializable {
            val lightsOut = LightsOut(args["size"] as Int)

            @Suppress("UNCHECKED_CAST")
            val boards = args["boards"] as Map<String, Map<String, Any?>>
            for ((key, value) in boards) {
                @Suppress("UNCHECKED_CAST")
                lightsOut.setBoard(
                    value["origin"] as Location,
                    value["xAxis"] as Vector,
                    value["yAxis"] as Vector,
                    key
                )
            }
            return lightsOut
        }
    }

    var boardState = Array(size) { BooleanArray(size) }

    init {
        reset(null)
    }

    override fun serialize(): MutableMap<String, Any?> {
        val data = hashMapOf<String, Any?>()
        data["size"] = size
        for ((name, board) in boards) {
            data["boards.$name"] = mapOf<String, Any?>(
                "origin" to board.origin,
                "xAxis" to board.xAxis,
                "yAxis" to board.yAxis
            )
        }
        return data
    }

    override fun deserializeRecord(data: Map<String, Any?>) {
        @Suppress("UNCHECKED_CAST")
        val boardPreset = Method.deserialize2dBooleanBoard(data["board"] as List<String>?)
        reset(GameState(boardPreset, 0, false))
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        data["board"] = Method.serialize2dBooleanBoard(this.boardState)
        return data
    }

    override fun reset(gamePreset: GameState?) {
        @Suppress("UNCHECKED_CAST")
        if (gamePreset != null) {
            boardState = gamePreset.boardState as Array<BooleanArray>
        } else {
            boardState = Array(size) { BooleanArray(size) }
            for (x in 0..<size) {
                for (y in 0..<size) {
                    val random = (Math.random().roundToInt())
                    if (random == 0) {
                        move(x, y)
                    }
                }
            }
        }
        display()
    }

    override fun display() {
        for (board in boards.values) {
            for (x in 0..<size) {
                val location = board.origin.clone().add(board.xAxis.clone().multiply(x))
                for (y in 0..<size) {
                    val block = location.block

                    block.type = Material.WAXED_COPPER_BULB
                    val lightable: Lightable = block.blockData as CopperBulb
                    lightable.isLit = boardState[x][y]
                    block.blockData = lightable

                    location.add(board.yAxis)
                }
            }
        }
    }

    fun move(x: Int, y: Int): Boolean {
        for (vector in vectors) {
            if (isInside(x + vector[0], y + vector[1])) {
                boardState[x + vector[0]][y + vector[1]] = !boardState[x + vector[0]][y + vector[1]]
            }
        }
        display()
        return true
    }

    override fun move(x: Int, y: Int, z: Int, movePlayer: Player): Boolean {
        return move(x, y)
    }
}
