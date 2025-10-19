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
    var ko: Int? = null // 劫
    var selected: Pos? = null

    var sgfContent = ""

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
        reset(GameState(data["board"] as String, 1, false))
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        return hashMapOf("board" to sgfContent)
    }

    override fun reset(gamePreset: GameState?) {
        sgfContent = ""
        player = 1
        pieceString = arrayOfNulls(SIZE * SIZE)
        pieceStringLiberty = IntArray(SIZE * SIZE) { -1 }
        ko = null
        if (gamePreset != null) {
            val boardSgf = gamePreset.boardState as String
            var operate = ""
            var argument = ""
            var getOperate = true
            for (it in 0..<boardSgf.length) {
                when (boardSgf[it]) {
                    ';' -> continue
                    '[' -> getOperate = false
                    ']' -> {
                        if (!getOperate) {
                            getOperate = true
                        } else {
                            break
                        }
                        when (operate) {
                            "B" -> {
                                player = 1
                                if (argument.length == 2) {
                                    val x = argument[0] - 'a'
                                    val y = argument[1] - 'a'
                                    moveImpl(x, y)
                                }
                            }
                            "W" -> {
                                player = 2
                                if (argument.length == 2) {
                                    val x = argument[0] - 'a'
                                    val y = argument[1] - 'a'
                                    moveImpl(x, y)
                                }
                            }
                        }
                        operate = ""
                        argument = ""
                    }
                    else -> {
                        if (getOperate) {
                            operate += boardSgf[it]
                        } else {
                            argument += boardSgf[it]
                        }
                    }
                }
            }
        } else {
            boardState = Array(SIZE) { IntArray(SIZE) }
        }
        selected = null
        displaySelectedTask?.cancel()

        uuidPair.clear()
        display()
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

    private fun moveImpl(x: Int, y: Int) {
        boardState[x][y] = player
        val moveVertex = x + SIZE * y
        pieceString[moveVertex] = PieceNode(moveVertex, moveVertex)
        pieceStringLiberty[moveVertex] = 1
        var adjacent = false
        for (v in vectors) {
            val dx = v[0]
            val dy = v[1]
            if (isInside(x + dx, y + dy)) {
                if (boardState[x + dx][y + dy] in 1..2) {
                    val adjacencyVertex = moveVertex + (dx + SIZE * dy)
                    if (boardState[x + dx][y + dy] == player) {
                        if (pieceString[adjacencyVertex]!!.identity == moveVertex) continue
                        pieceStringLiberty[pieceString[adjacencyVertex]!!.identity] = -1
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
        for (identity in 0..<(SIZE * SIZE)) {
            if (pieceStringLiberty[identity] == -1) continue
            pieceStringLiberty[identity] = countLiberty(identity)
        }

        ko = null
        if (!adjacent && pieceStringLiberty[moveVertex] == 1 && captureCount == 1) ko = moveVertex

        val color = if (player == 1) "B" else "W"
        sgfContent += ";$color[${'a' + x}${'a' + y}]"
    }

    override fun move(
        x: Int,
        y: Int,
        z: Int,
        movePlayer: Player
    ): Boolean {
        val playerUUID = movePlayer.uniqueId
        if (uuidPair.getPlayerUUID(player) == playerUUID || uuidPair.putPlayerUUID(playerUUID)) {
            if (selected == null || selected!!.x != x || selected!!.y != y) {
                displaySelectedTask?.cancel()
                if (selected != null) displaySingle(selected!!.x, selected!!.y, boardState[selected!!.x][selected!!.y])
                if (boardState[x][y] == 0) {
                    val moveVertex = x + SIZE * y
                    var isValid = false
                    for (v in vectors) {
                        val dx = v[0]
                        val dy = v[1]
                        if (isInside(x + dx, y + dy)) {
                            if (boardState[x + dx][y + dy] in 1..2) {
                                val adjacencyVertex = moveVertex + (dx + SIZE * dy)
                                if (adjacencyVertex == ko) return false
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

                    selected = Pos(x, y)
                    select(x, y, player, 0)
                } else {
                    val component =
                        Component.text("此點位已有棋子，再次點擊視為虛手!")
                            .color(NamedTextColor.RED)
                    movePlayer.sendMessage(component)
                    movePlayer.playSound(movePlayer, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                    selected = Pos(x, y)
                }
            } else {
                displaySelectedTask?.cancel()
                selected = null
                if (boardState[x][y] == 0) {
                    moveImpl(x, y)
                } else {
                    val color = if (player == 1) "B" else "W"
                    sgfContent += ";$color[]"
                }
                display()
                switchPlayer()
            }
            return true
        } else {
            val component =
                Component.text("已被玩家 " + Bukkit.getPlayer(uuidPair.getPlayerUUID(player)!!)?.name + " 綁定")
                    .color(NamedTextColor.RED)
            movePlayer.sendMessage(component)
            movePlayer.playSound(movePlayer, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
            return false
        }
    }

    private fun switchPlayer() {
        player = if (player == 1) {
            2
        } else {
            1
        }
    }

    fun exportToSGF(): String {
        val pb = uuidPair.getPlayerUUID(0)?.let { Bukkit.getPlayer(it)?.name } ?: ""
        val pw = uuidPair.getPlayerUUID(1)?.let { Bukkit.getPlayer(it)?.name } ?: ""
        return "(;GM[1]FF[4]SZ[19]KM[7.5]PB[$pb]PW[$pw]AP[AntGame]$sgfContent)"
    }
}
