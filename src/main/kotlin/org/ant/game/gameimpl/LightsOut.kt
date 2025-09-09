package org.ant.game.gameimpl

import org.ant.game.BoardGame
import org.ant.game.RecordSerializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.type.CopperBulb
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import kotlin.math.roundToInt

class LightsOut(location: Location, size: Int, displayLocation: Location?, displayAlign: String?) :
    BoardGame(location, displayLocation, displayAlign, size),
    ConfigurationSerializable,
    RecordSerializable {
    companion object {
        var vectors = arrayOf<IntArray>(intArrayOf(0, 0), intArrayOf(1, 0), intArrayOf(0, 1), intArrayOf(-1, 0), intArrayOf(0, -1))

        fun deserialize(args: Map<String, Any?>): LightsOut {
            return LightsOut(
                args["location"] as Location,
                args["size"] as Int,
                args["display_location"] as Location?,
                args["display_align"] as String?
            )
        }
    }

    var board: Array<BooleanArray>? = null

    init {
        this.location = location
        this.size = size
        this.center = location.clone()
        this.center.add(size.toDouble() / 2, 0.0, size.toDouble() / 2)
        this.displayLocation = displayLocation
        this.displayAlign = displayAlign
        reset(board)
    }

    override fun serialize(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        data["location"] = this.location
        data["size"] = this.size
        data["display_location"] = this.displayLocation
        data["display_align"] = this.displayAlign
        return data
    }

    override fun deserializeRecord(data: Map<String, Any?>) {
        @Suppress("UNCHECKED_CAST")
        val boardPreset = Method.deserialize2dBooleanBoard(data["board"] as List<String>?)
        reset(boardPreset)
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        data["board"] = Method.serialize2dBooleanBoard(this.board)
        return data
    }

    override fun setDisplay(location: Location?, displayAlign: String?) {
        super.setDisplay(location, displayAlign)
        this.displayLocation = location
        this.displayAlign = displayAlign
    }

    override fun removeDisplay() {
        super.removeDisplay()
        this.displayLocation = null
        this.displayAlign = null
    }

    fun reset(boardPreset: Array<BooleanArray>?) {
        if (boardPreset != null) {
            board = boardPreset
        } else {
            board = Array(size) { BooleanArray(size) }
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

    private fun display() {
        for (x in 0..<size) {
            for (y in 0..<size) {
                var block = location.clone().add(x.toDouble(), 0.0, y.toDouble()).block
                block.type = Material.WAXED_COPPER_BULB
                var lightable: Lightable = block.blockData as CopperBulb
                lightable.isLit = board!![x][y]
                block.blockData = lightable
                if (displayLocation != null) {
                    if (displayAlign == "x") {
                        block = displayLocation!!.clone().add(x.toDouble(), y.toDouble(), 0.0).block
                        block.type = Material.WAXED_COPPER_BULB
                        lightable = block.blockData as CopperBulb
                        lightable.isLit = board!![x][y]
                        block.blockData = lightable
                    } else if (displayAlign == "z") {
                        block = displayLocation!!.clone().add(0.0, x.toDouble(), y.toDouble()).block
                        block.type = Material.WAXED_COPPER_BULB
                        lightable = block.blockData as CopperBulb
                        lightable.isLit = board!![x][y]
                        block.blockData = lightable
                    }
                }
            }
        }
    }

    fun move(x: Int, z: Int): Boolean {
        for (vector in vectors) {
            if (isInside(x + vector[0], z + vector[1])) {
                board!![x + vector[0]][z + vector[1]] = !board!![x + vector[0]][z + vector[1]]
            }
        }
        display()
        return true
    }

    override fun move(x: Int, z: Int, minecraftPlayer: Player): Boolean {
        return move(x, z)
    }
}
