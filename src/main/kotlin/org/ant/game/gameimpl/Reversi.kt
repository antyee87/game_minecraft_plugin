package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.AntGamePlugin
import org.ant.game.gameimpl.gameframe.GameConstants
import org.ant.game.gameimpl.gameframe.GameDeSerializable
import org.ant.game.gameimpl.gameframe.GameSerializable
import org.ant.game.gameimpl.gameframe.GameState
import org.ant.game.gameimpl.gameframe.Method
import org.ant.game.gameimpl.gameframe.Pos
import org.ant.game.gameimpl.gameframe.RecordSerializable
import org.ant.game.gameimpl.gameframe.TwoColorBoardGame
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class Reversi(pluginInstance: AntGamePlugin) :
    TwoColorBoardGame(pluginInstance, SIZE),
    GameSerializable,
    RecordSerializable {
    companion object : GameDeSerializable {
        const val SIZE = 8

        override fun deserialize(pluginInstance: AntGamePlugin, args: Map<String, Any?>): Reversi {
            val reversi = Reversi(pluginInstance)

            @Suppress("UNCHECKED_CAST")
            val boards = args["boards"] as Map<String, Map<String, Any?>>
            for ((key, value) in boards) {
                reversi.setBoard(
                    key,
                    value["origin"] as Location,
                    value["xAxis"] as Vector,
                    value["yAxis"] as Vector
                )
            }
            return reversi
        }
    }

    var canFlip: Array<Array<BooleanArray>> = Array(8) { Array(SIZE) { BooleanArray(SIZE) } }
    var selected: Pos? = null

    init {
        reset(null)
    }

    override fun serialize(): MutableMap<String, Any?> {
        val data = hashMapOf<String, Any?>()
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
        reset(
            GameState(
                Method.deserialize2dBoard(data["board"] as List<String>?),
                data["player"] as Int,
                data["end"] as Boolean
            )
        )
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        data["board"] = Method.serialize2dBoard(this.boardState)
        data["player"] = player
        data["end"] = end
        return data
    }

    @Suppress("UNCHECKED_CAST")
    override fun reset(gamePreset: GameState?) {
        if (gamePreset != null) {
            boardState = gamePreset.boardState as Array<IntArray>
            player = gamePreset.player
            end = gamePreset.end
        } else {
            boardState = Array(SIZE) { IntArray(SIZE) }
            boardState[3][3] = 1
            boardState[4][4] = 1
            boardState[3][4] = 2
            boardState[4][3] = 2

            player = 1
            end = false
        }
        selected = null
        displaySelectedTask?.cancel()

        findCanMove()
        uuidPair.clear()
        display()
    }

    private fun findCanMove(): Boolean {
        var moveable = false
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                if (boardState[x][y] == 3) boardState[x][y] = 0
                for (i in 0..7) canFlip[i][x][y] = false
            }
        }
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                if (boardState[x][y] == player) {
                    for (i in 0..7) {
                        var dx = GameConstants.eightDirection[i][0]
                        var dy = GameConstants.eightDirection[i][1]
                        var hasOpponent = false
                        while (isInside(x + dx, y + dy)) {
                            if (boardState[x + dx][y + dy] in 1..2 && boardState[x + dx][y + dy] != player) {
                                hasOpponent = true
                            } else {
                                break
                            }
                            dx += GameConstants.eightDirection[i][0]
                            dy += GameConstants.eightDirection[i][1]
                        }
                        if (hasOpponent && isInside(x + dx, y + dy) && boardState[x + dx][y + dy] !in 1..2) {
                            boardState[x + dx][y + dy] = 3
                            canFlip[i][x + dx][y + dy] = true
                            moveable = true
                        }
                    }
                }
            }
        }
        return moveable
    }

    override fun move(x: Int, y: Int, z: Int, movePlayer: Player): Boolean {
        if (!end) {
            if (boardState[x][y] == 3) {
                val playerUUID = movePlayer.uniqueId
                if (uuidPair.getPlayerUUID(player) == playerUUID || uuidPair.putPlayerUUID(playerUUID)) {
                    displaySelectedTask?.cancel()
                    if (selected == null || selected!!.x != x || selected!!.y != y) {
                        if (selected != null) displaySingle(selected!!.x, selected!!.y, 3)
                        selected = Pos(x, y)
                        select(x, y, player, 3)
                    } else {
                        boardState[x][y] = player
                        selected = null
                        for (i in 0..7) {
                            if (!canFlip[(i + 4) % 8][x][y]) continue
                            var dx = GameConstants.eightDirection[i][0]
                            var dy = GameConstants.eightDirection[i][1]
                            while (isInside(x + dx, y + dy)) {
                                if (boardState[x + dx][y + dy] != player) {
                                    boardState[x + dx][y + dy] = player
                                } else {
                                    break
                                }
                                dx += GameConstants.eightDirection[i][0]
                                dy += GameConstants.eightDirection[i][1]
                            }
                        }

                        var moveable = false
                        @Suppress("UNUSED_PARAMETER")
                        for (i in 0..1) {
                            player = if (player == 1) {
                                2
                            } else {
                                1
                            }
                            moveable = findCanMove()
                            if (moveable) break
                        }
                        display()

                        if (!moveable) {
                            var blackPiece = 0
                            var whitePiece = 0
                            for (i in 0..<SIZE) {
                                for (j in 0..<SIZE) {
                                    if (boardState[i][j] == 1) {
                                        blackPiece++
                                    } else if (boardState[i][j] == 2) {
                                        whitePiece++
                                    }
                                }
                            }
                            var component: Component?
                            if (blackPiece > whitePiece) {
                                component = Component.text("黑棋勝利").color(NamedTextColor.GRAY)
                                for (board in boards.values) {
                                    if (board.yAxis.y == 0.0) {
                                        Method.blackWhiteFirework(board.center.clone().add(0.0, 1.0, 0.0), true)
                                    }
                                }
                            } else if (whitePiece > blackPiece) {
                                component = Component.text("白棋勝利").color(NamedTextColor.WHITE)
                                for (board in boards.values) {
                                    if (board.yAxis.y == 0.0) {
                                        Method.blackWhiteFirework(board.center.clone().add(0.0, 1.0, 0.0), false)
                                    }
                                }
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
                    val component = Component.text("已被玩家 " + Bukkit.getPlayer(uuidPair.getPlayerUUID(player)!!)?.name + " 綁定").color(NamedTextColor.RED)
                    movePlayer.sendMessage(component)
                    movePlayer.playSound(movePlayer, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                    return false
                }
            }
        }
        return false
    }
}
