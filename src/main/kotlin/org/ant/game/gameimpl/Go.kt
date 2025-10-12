package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.AntGamePlugin
import org.ant.game.gameimpl.gameframe.GameDeSerializable
import org.ant.game.gameimpl.gameframe.GameSerializable
import org.ant.game.gameimpl.gameframe.GameState
import org.ant.game.gameimpl.gameframe.Pos
import org.ant.game.gameimpl.gameframe.RecordSerializable
import org.ant.game.gameimpl.gameframe.TwoColorBoardGame
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class Go(pluginInstance: AntGamePlugin) :
    TwoColorBoardGame(pluginInstance, SIZE),
    GameSerializable,
    RecordSerializable {
    companion object : GameDeSerializable {
        const val SIZE = 19

        val vectors = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(0, 1),
            intArrayOf(-1, 0),
            intArrayOf(0, -1)
        )

        override fun deserialize(pluginInstance: AntGamePlugin, args: Map<String, Any?>): Go {
            val go = Go(pluginInstance)

            @Suppress("UNCHECKED_CAST")
            val boards = args["boards"] as Map<String, Map<String, Any?>>
            for ((key, value) in boards) {
                go.setBoard(
                    key,
                    value["origin"] as Location,
                    value["xAxis"] as Vector,
                    value["yAxis"] as Vector
                )
            }
            return go
        }
    }

    data class PieceNode(
        var identity: Int,
        var next: Int,
    )

    var pieceString: Array<PieceNode?> = arrayOfNulls(SIZE * SIZE)
    var pieceStringLiberty = IntArray(SIZE * SIZE) { -1 }
    var totalCapture = IntArray(2)
    var ko: Int? = null // 劫
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
        TODO("Not yet implemented")
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        TODO("Not yet implemented")
    }

    private fun countLiberty(vertex: Int): Int {
        val libertySet = HashSet<Int>()
        var curVertex = vertex
        do {
            for (v in vectors) {
                val dx = v[0]
                val dy = v[1]
                if (isInside(curVertex % SIZE + dx, curVertex / SIZE + dy) && boardState[curVertex % SIZE + dx][curVertex / SIZE + dy] == 0) {
                    libertySet.add(curVertex + (dx + SIZE * dy))
                }
            }
            curVertex = pieceString[curVertex]!!.next
        } while (curVertex != vertex)
        return libertySet.count()
    }

    override fun move(
        x: Int,
        y: Int,
        z: Int,
        movePlayer: Player
    ): Boolean {
        if (!end && boardState[x][y] == 0) {
            val playerUUID = movePlayer.uniqueId
            if (uuidPair.getPlayerUUID(player) == playerUUID || uuidPair.putPlayerUUID(playerUUID)) {
                if (selected == null || selected!!.x != x || selected!!.y != y) {
                    val moveVertex = x + SIZE * y
                    var isValid = false
                    for (v in vectors) {
                        val dx = v[0]
                        val dy = v[1]
                        if (isInside(x + dx, y + dy)) {
                            if (boardState[x + dx][y + dy] in 1..2) {
                                val adjacencyVertex = moveVertex + (dx + SIZE * dy)
                                if (adjacencyVertex == ko) {
                                    return false
                                }
                                if (boardState[x + dx][y + dy] == player) {
                                    if (pieceStringLiberty[pieceString[adjacencyVertex]!!.identity] > 1) {
                                        isValid = true
                                        break
                                    }
                                } else {
                                    if (pieceStringLiberty[pieceString[adjacencyVertex]!!.identity] == 1) {
                                        isValid = true
                                        break
                                    }
                                }
                            } else {
                                isValid = true
                                break
                            }
                        }
                    }
                    if (!isValid) return false

                    displaySelectedTask?.cancel()
                    if (selected != null) displaySingle(selected!!.x, selected!!.y, 0)
                    selected = Pos(x, y)
                    select(x, y, player, 0)
                } else {
                    displaySelectedTask?.cancel()
                    boardState[x][y] = player
                    val moveVertex = x + SIZE * y
                    pieceString[moveVertex] = PieceNode(moveVertex, moveVertex)
                    pieceStringLiberty[moveVertex] = 0
                    var adjacent = false
                    for (v in vectors) {
                        val dx = v[0]
                        val dy = v[1]
                        if (isInside(x + dx, y + dy)) {
                            if (boardState[x + dx][y + dy] in 1..2) {
                                val adjacencyVertex = moveVertex + (dx + SIZE * dy)
                                if (boardState[x + dx][y + dy] == player) {
                                    pieceStringLiberty[adjacencyVertex] = -1
                                    val tmp = pieceString[moveVertex]!!.next
                                    pieceString[moveVertex]!!.next = pieceString[adjacencyVertex]!!.next
                                    pieceString[adjacencyVertex]!!.next = tmp

                                    var curNode = pieceString[moveVertex]!!
                                    do {
                                        curNode.identity = moveVertex
                                        curNode = pieceString[curNode.next]!!
                                    } while (curNode.identity != moveVertex)
                                    adjacent = true
                                } else {
                                    pieceStringLiberty[pieceString[adjacencyVertex]!!.identity] = countLiberty(adjacencyVertex)
                                }
                            }
                        }
                    }
                    var captureCount = 0
                    for (identity in 0..<(SIZE * SIZE)) {
                        if (pieceStringLiberty[identity] == -1) continue
                        if (boardState[identity % SIZE][identity / SIZE] != player) {
                            if (pieceStringLiberty[identity] == 0) {
                                var curVertex = identity
                                while (pieceString[curVertex] != null) {
                                    boardState[curVertex % SIZE][curVertex / SIZE] = 0
                                    ++captureCount
                                    val tmpCurVertex = curVertex
                                    curVertex = pieceString[curVertex]!!.next
                                    pieceString[tmpCurVertex] = null
                                }
                                pieceStringLiberty[identity] = -1
                            }
                        }
                    }
                    if (captureCount > 0) {
                        for (identity in 0..<(SIZE * SIZE)) {
                            if (pieceStringLiberty[identity] == -1) continue
                            pieceStringLiberty[identity] = countLiberty(identity)
                        }
                    }
                    totalCapture[player - 1] += captureCount

                    ko = null
                    if (adjacent) {
                        // TODO
                    } else {
                        if (pieceStringLiberty[moveVertex] == 1 && captureCount == 1) ko = moveVertex
                    }

//                    var result = ""
//                    for (i in 0..<(SIZE * SIZE)) {
//                        if (i % SIZE == 0) result += "\n"
//                        result += String.format("%02d ", pieceStringLiberty[i])
//                    }
//                    pluginInstance.logger.info(result)

                    display()
                    selected = null

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

    @Suppress("UNCHECKED_CAST")
    override fun reset(gamePreset: GameState?) {
        if (gamePreset != null) {
            boardState = gamePreset.boardState as Array<IntArray>
            player = gamePreset.player
            end = gamePreset.end
            TODO("Not yet implemented")
        } else {
            boardState = Array(SIZE) { IntArray(SIZE) }
            player = 1
            end = false

            pieceString = arrayOfNulls(SIZE * SIZE)
            pieceStringLiberty = IntArray(SIZE * SIZE) { -1 }
            totalCapture = IntArray(2)
            ko = null
        }
        selected = null
        displaySelectedTask?.cancel()

        uuidPair.clear()
        display()
    }
}
