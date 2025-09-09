package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.Game
import org.ant.game.RecordSerializable
import org.ant.game.TwoColorBoardGame
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player

class Gomoku(gameInstance: Game, location: Location, displayLocation: Location?, displayAlign: String?) :
    TwoColorBoardGame(gameInstance, location, displayLocation, displayAlign, SIZE),
    ConfigurationSerializable,
    RecordSerializable {
    companion object {
        const val SIZE = 15
        var vectors = arrayOf<IntArray>(intArrayOf(1, 0), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 0), intArrayOf(0, -1), intArrayOf(-1, -1), intArrayOf(-1, 1))

        fun deserialize(gameInstance: Game, args: Map<String?, Any?>): Gomoku {
            return Gomoku(
                gameInstance,
                args["location"] as Location,
                args["display_location"] as Location?,
                args["display_align"] as String?
            )
        }
    }

    var board: Array<IntArray>? = null
    var selected: IntArray? = null
    var player: Int = 0
    var end: Boolean = false
    lateinit var minecraftPlayers: Array<Player?>

    init {
        this.gameInstance = gameInstance
        this.location = location
        this.center = location.clone()
        this.center.add(SIZE.toDouble() / 2, 0.0, SIZE.toDouble() / 2)
        this.displayLocation = displayLocation
        this.displayAlign = displayAlign
        reset(board)
    }

    override fun serialize(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        data["location"] = this.location
        data["display_location"] = this.displayLocation
        data["display_align"] = this.displayAlign
        return data
    }

    override fun deserializeRecord(data: Map<String, Any?>) {
        @Suppress("UNCHECKED_CAST")
        val boardPreset = Method.deserialize2dBoard(data["board"] as List<String>?)
        reset(boardPreset)
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        if (!end) {
            data["board"] = Method.serialize2dBoard(this.board)
        }
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

    override fun remove() {
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        super.remove()
    }

    fun reset(boardPreset: Array<IntArray>?) {
        board = boardPreset ?: Array(SIZE) { IntArray(SIZE) }
        selected = null
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        player = 1
        end = false
        minecraftPlayers = arrayOfNulls(2)
        display(board!!)
    }

    override fun move(x: Int, z: Int, minecraftPlayer: Player): Boolean {
        if (!end && board!![x][z] == 0) {
            if (minecraftPlayers[player - 1] == null || minecraftPlayers[player - 1] == minecraftPlayer) {
                if (minecraftPlayers[player - 1] == null) minecraftPlayers[player - 1] = minecraftPlayer

                if (selected == null || selected!![0] != x || selected!![1] != z) {
                    if (displaySelectedTask != null) displaySelectedTask!!.cancel()
                    if (selected != null) displaySingle(selected!![0], selected!![1], 0)
                    selected = intArrayOf(x, z)
                    select(x, z, player, 0)
                } else {
                    displaySelectedTask!!.cancel()
                    board!![x][z] = player
                    selected = null
                    if (isWin(x, z)) {
                        val component: Component?
                        if (player == 1) {
                            component = Component.text("黑棋勝利").color(NamedTextColor.GRAY)
                            firework(center, true)
                        } else {
                            component = Component.text("白棋勝利").color(NamedTextColor.WHITE)
                            firework(center, false)
                        }
                        broadcast(component)
                        end = true
                    }
                    if (isTie()) {
                        broadcast("平手")
                        end = true
                    }
                    display(board!!)
                    player = if (player == 1) {
                        2
                    } else {
                        1
                    }
                }
                return true
            } else {
                val component = Component.text("已被玩家 " + minecraftPlayers[player - 1]!!.name + " 綁定").color(NamedTextColor.RED)
                minecraftPlayer.sendMessage(component)
                minecraftPlayer.playSound(minecraftPlayer, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                return false
            }
        }
        return false
    }

    private fun isWin(x: Int, y: Int): Boolean {
        val counter = IntArray(4)
        for (i in 0..7) {
            var checkX = x
            var checkY = y
            @Suppress("UNUSED_PARAMETER")
            for (j in 1..4) {
                checkX += vectors[i][0]
                checkY += vectors[i][1]
                if (isInside(checkX, checkY)) {
                    if (board!![checkX][checkY] == player) {
                        counter[i % 4]++
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            if (counter[i % 4] >= 4) return true
        }
        return false
    }

    private fun isTie(): Boolean {
        for (i in 0..<SIZE) {
            for (j in 0..<SIZE) {
                if (board!![i][j] == 0) {
                    return false
                }
            }
        }
        return true
    }
}
