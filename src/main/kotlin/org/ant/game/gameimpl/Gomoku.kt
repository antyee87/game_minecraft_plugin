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
import kotlin.String

class Gomoku(pluginInstance: AntGamePlugin) :
    TwoColorBoardGame(pluginInstance, SIZE),
    GameSerializable,
    RecordSerializable {
    companion object : GameDeSerializable {
        const val SIZE = 15

        override fun deserialize(pluginInstance: AntGamePlugin, args: Map<String, Any?>): Gomoku {
            val gomoku = Gomoku(pluginInstance)

            @Suppress("UNCHECKED_CAST")
            val boards = args["boards"] as Map<String, Map<String, Any?>>
            for ((key, value) in boards) {
                gomoku.setBoard(
                    value["origin"] as Location,
                    value["xAxis"] as Vector,
                    value["yAxis"] as Vector,
                    key
                )
            }
            return gomoku
        }
    }

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
                Method.deserialize2dBoard(data["board"] as List<String>),
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
            player = 1
            end = false
        }
        selected = null
        displaySelectedTask?.cancel()

        uuidPair.clear()
        display()
    }

    override fun move(x: Int, y: Int, z: Int, movePlayer: Player): Boolean {
        if (!end && boardState[x][y] == 0) {
            val playerUUID = movePlayer.uniqueId
            if (uuidPair.getPlayerUUID(player) == playerUUID || uuidPair.putPlayerUUID(playerUUID)) {
                displaySelectedTask?.cancel()
                if (selected == null || selected!!.x != x || selected!!.y != y) {
                    if (selected != null) displaySingle(selected!!.x, selected!!.y, 0)
                    selected = Pos(x, y)
                    select(x, y, player, 0)
                } else {
                    boardState[x][y] = player
                    displaySingle(selected!!.x, selected!!.y, player)
                    selected = null
                    if (isWin(x, y)) {
                        val component: Component?
                        if (player == 1) {
                            component = Component.text("黑棋勝利").color(NamedTextColor.GRAY)
                            for (board in boards.values) {
                                if (board.yAxis.y == 0.0) {
                                    Method.blackWhiteFirework(board.center.clone().add(0.0, 1.0, 0.0), true)
                                }
                            }
                        } else {
                            component = Component.text("白棋勝利").color(NamedTextColor.WHITE)
                            for (board in boards.values) {
                                if (board.yAxis.y == 0.0) {
                                    Method.blackWhiteFirework(board.center.clone().add(0.0, 1.0, 0.0), false)
                                }
                            }
                        }
                        broadcast(component)
                        end = true
                    }
                    if (isTie()) {
                        broadcast("平手")
                        end = true
                    }
                    player = if (player == 1) {
                        2
                    } else {
                        1
                    }
                }
                return true
            } else {
                val component = Component.text("已被玩家 " + Bukkit.getPlayer(uuidPair.getPlayerUUID(player)!!)?.name + " 綁定").color(NamedTextColor.RED)
                movePlayer.sendMessage(component)
                movePlayer.playSound(movePlayer, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
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
                checkX += GameConstants.eightDirection[i][0]
                checkY += GameConstants.eightDirection[i][1]
                if (isInside(checkX, checkY)) {
                    if (boardState[checkX][checkY] == player) {
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
                if (boardState[i][j] == 0) {
                    return false
                }
            }
        }
        return true
    }
}
