package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.Game
import org.ant.game.RecordSerializable
import org.ant.game.TwoColorBoardGame
import org.ant.game.gameimpl.Method.isInRange
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player

@Suppress("DuplicatedCode")
class Reversi(gameInstance: Game, location: Location, displayLocation: Location?, displayAlign: String?) :
    TwoColorBoardGame(gameInstance, location, displayLocation, displayAlign, SIZE),
    ConfigurationSerializable,
    RecordSerializable {
    companion object {
        const val SIZE = 8
        var vectors = arrayOf<IntArray?>(intArrayOf(1, 0), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 0), intArrayOf(0, -1), intArrayOf(-1, -1), intArrayOf(-1, 1))

        fun deserialize(gameInstance: Game, args: Map<String?, Any?>): Reversi {
            return Reversi(
                gameInstance,
                args["location"] as Location,
                args["display_location"] as Location?,
                args["display_align"] as String?
            )
        }
    }

    var board: Array<IntArray>? = null
    var canFlip: Array<Array<BooleanArray?>?> = Array(8) { Array(SIZE) { BooleanArray(SIZE) } }
    var selected: IntArray? = null
    var player: Int = 0
    var end: Boolean = false
    var moveable: Boolean = false
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

    fun reset(boardPreset: Array<IntArray>?) {
        if (boardPreset != null) {
            board = boardPreset
        } else {
            board = Array(SIZE) { IntArray(SIZE) }
            board!![3][3] = 1
            board!![4][4] = 1
            board!![3][4] = 2
            board!![4][3] = 2
        }
        selected = null
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        player = 1
        end = false
        findCanMove()
        minecraftPlayers = arrayOfNulls(2)
        display(board!!)
    }

    override fun remove() {
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        super.remove()
    }

    private fun findCanMove() {
        moveable = false
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                if (board!![x][y] == 3) board!![x][y] = 0
                for (i in 0..7) canFlip[i]!![x]!![y] = false
            }
        }
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                if (board!![x][y] == player) {
                    for (i in 0..7) {
                        var checkX = x
                        var checkY = y
                        var hasOpponent = false
                        checkX += vectors[i]!![0]
                        checkY += vectors[i]!![1]
                        while (isInside(checkX, checkY)) {
                            if (board!![checkX][checkY] != player && isInRange(board!![checkX][checkY], 1, 2)) {
                                hasOpponent = true
                            } else {
                                break
                            }
                            checkX += vectors[i]!![0]
                            checkY += vectors[i]!![1]
                        }
                        if (hasOpponent && isInside(checkX, checkY) && !isInRange(board!![checkX][checkY], 1, 2)) {
                            board!![checkX][checkY] = 3
                            canFlip[i]!![checkX]!![checkY] = true
                            moveable = true
                        }
                    }
                }
            }
        }
    }

    override fun move(x: Int, z: Int, minecraftPlayer: Player): Boolean {
        if (!end) {
            if (board!![x][z] == 3) {
                if (minecraftPlayers[player - 1] == null || minecraftPlayers[player - 1] == minecraftPlayer) {
                    if (minecraftPlayers[player - 1] == null) {
                        minecraftPlayers[player - 1] = minecraftPlayer
                    }
                    if (selected == null || selected!![0] != x || selected!![1] != z) {
                        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
                        if (selected != null) displaySingle(selected!![0], selected!![1], 3)
                        selected = intArrayOf(x, z)
                        select(x, z, player, 3)
                    } else {
                        displaySelectedTask!!.cancel()
                        board!![x][z] = player
                        selected = null
                        for (i in 0..7) {
                            var checkX = x
                            var checkY = z
                            checkX += vectors[i]!![0]
                            checkY += vectors[i]!![1]
                            while (canFlip[(i + 4) % 8]!![x]!![z] && isInside(checkX, checkY)) {
                                if (board!![checkX][checkY] != player) {
                                    board!![checkX][checkY] = player
                                } else {
                                    break
                                }
                                checkX += vectors[i]!![0]
                                checkY += vectors[i]!![1]
                            }
                        }
                        @Suppress("UNUSED_PARAMETER")
                        for (i in 0..1) {
                            player = if (player == 1) {
                                2
                            } else {
                                1
                            }
                            findCanMove()
                            if (moveable) break
                        }
                        display(board!!)

                        if (!moveable) {
                            var blackPiece = 0
                            var whitePiece = 0
                            for (i in 0..<SIZE) {
                                for (j in 0..<SIZE) {
                                    if (board!![i][j] == 1) {
                                        blackPiece++
                                    } else if (board!![i][j] == 2) {
                                        whitePiece++
                                    }
                                }
                            }
                            var component: Component?
                            if (blackPiece > whitePiece) {
                                component = Component.text("黑棋勝利").color(NamedTextColor.GRAY)
                                firework(center, true)
                            } else if (whitePiece > blackPiece) {
                                component = Component.text("白棋勝利").color(NamedTextColor.WHITE)
                                firework(center, false)
                            } else {
                                component = Component.text("平手")
                            }
                            broadcast(component)
                            component = Component.text(blackPiece).color(NamedTextColor.GRAY)
                                .append(Component.text(" : ").color(NamedTextColor.GREEN))
                                .append(Component.text(whitePiece).color(NamedTextColor.WHITE))
                            broadcast(component)
                            end = true
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
        }
        return false
    }
}
