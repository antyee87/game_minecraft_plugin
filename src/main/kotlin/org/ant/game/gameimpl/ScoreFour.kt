package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.AntGamePlugin
import org.ant.game.gameimpl.gameframe.BoardGame
import org.ant.game.gameimpl.gameframe.GameDeSerializable
import org.ant.game.gameimpl.gameframe.GameSerializable
import org.ant.game.gameimpl.gameframe.GameState
import org.ant.game.gameimpl.gameframe.Method
import org.ant.game.gameimpl.gameframe.Pos
import org.ant.game.gameimpl.gameframe.RecordSerializable
import org.ant.game.gameimpl.gameframe.UUIDPair
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class ScoreFour(private val pluginInstance: AntGamePlugin) :
    BoardGame(SIZE),
    GameSerializable,
    RecordSerializable {
    companion object : GameDeSerializable {
        const val SIZE = 4

        override fun deserialize(pluginInstance: AntGamePlugin, args: Map<String, Any?>): ScoreFour {
            val scoreFour = ScoreFour(pluginInstance)

            @Suppress("UNCHECKED_CAST")
            val boards = args["boards"] as Map<String, Map<String, Any?>>
            for ((key, value) in boards) {
                scoreFour.setBoard(
                    key,
                    value["origin"] as Location,
                    value["xAxis"] as Vector,
                    value["yAxis"] as Vector
                )
            }
            return scoreFour
        }
    }

    var boardState = Array(SIZE) { Array(SIZE) { IntArray(5) } }
    var selected: Pos? = null
    var displaySelectedTask: BukkitTask? = null
    val fallingBlocks = arrayListOf<Entity>()
    var player: Int = 0
    var end: Boolean = false
    val uuidPair = UUIDPair()

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
                Method.deserialize3dBoard(data["board"] as List<List<String>>?),
                data["player"] as Int,
                data["end"] as Boolean
            )
        )
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        data["board"] = Method.serialize3dBoard(boardState)
        data["player"] = player
        data["end"] = end
        return data
    }

    override fun reset(gamePreset: GameState?) {
        if (gamePreset != null) {
            @Suppress("UNCHECKED_CAST")
            boardState = gamePreset.boardState as Array<Array<IntArray>>
            player = gamePreset.player
            end = gamePreset.end
        } else {
            boardState = Array(4) { Array(4) { IntArray(5) } }
            player = 1
            end = false
        }
        selected = null
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        uuidPair.clear()
        display()
    }

    override fun remove(removed: String?) {
        val removed: List<String> = if (removed == null) {
            boards.keys.toList()
        } else {
            listOf(removed)
        }
        for (name in removed) {
            if (boards.containsKey(name)) {
                val board = boards[name]!!
                for (x in 0..<SIZE) {
                    for (y in 0..<SIZE) {
                        val location = board.origin.clone().add(board.xAxis.clone().multiply(2 * x)).add(board.yAxis.clone().multiply(2 * y))
                        for (z in 0..<SIZE) {
                            location.block.type = Material.AIR
                            location.add(0.0, 1.0, 0.0)
                        }
                    }
                }
                boards.remove(name)
            }
        }
    }

    var visible: Boolean = true

    override fun move(x: Int, y: Int, z: Int, movePlayer: Player): Boolean {
        for (fallingBlock in fallingBlocks) {
            if (fallingBlock.isValid) return false
        }
        fallingBlocks.clear()
        if (!end && boardState[x][y][4] < SIZE) {
            val playerUUID = movePlayer.uniqueId
            if (uuidPair.getPlayerUUID(player) == playerUUID || uuidPair.putPlayerUUID(playerUUID)) {
                displaySelectedTask?.cancel()
                if (selected != null) {
                    for (board in boards.values) {
                        board.origin.clone()
                            .add(board.xAxis.clone().multiply(2 * selected!!.x))
                            .add(board.yAxis.clone().multiply(2 * selected!!.y))
                            .block.type = Method.yellowRedGlassMaterial(-1)
                    }
                }

                if (selected == null || selected!!.x != x || selected!!.y != y) {
                    selected = Pos(x, y)
                    visible = true
                    displaySelectedTask = Bukkit.getScheduler().runTaskTimer(
                        pluginInstance,
                        Runnable {
                            for (board in boards.values) {
                                val selectedBlock = board.origin.clone()
                                    .add(board.xAxis.clone().multiply(2 * x))
                                    .add(board.yAxis.clone().multiply(2 * y))
                                    .block
                                if (visible) {
                                    selectedBlock.type = Method.yellowRedGlassMaterial(player)
                                } else {
                                    selectedBlock.type = Method.yellowRedGlassMaterial(-1)
                                }
                            }

                            visible = !visible
                        },
                        0,
                        10
                    )
                } else {
                    display()
                    boardState[x][y][boardState[x][y][4]] = player
                    selected = null
                    for (board in boards.values) {
                        val location = board.origin.clone()
                            .add(board.xAxis.clone().multiply(2 * x))
                            .add(board.yAxis.clone().multiply(2 * y))
                            .add(0.0, 4.0, 0.0)
                            .add(0.5, 0.0, 0.5)
                        fallingBlocks.add(
                            location.world.spawn(location, FallingBlock::class.java) { entity ->
                                entity.blockData = Method.yellowRedGlassMaterial(player).createBlockData()
                                entity.dropItem = false
                                entity.velocity = Vector(0.0, 0.1, 0.0)
                            }
                        )
                    }
                    if (isWin(x, y, boardState[x][y][4])) {
                        val component: Component
                        if (player == 1) {
                            component = Component.text("黃色勝利").color(NamedTextColor.YELLOW)
                            for (board in boards.values) {
                                Method.yellowRedFirework(board.center, true)
                                Method.broadcast(component, board.center, 7)
                            }
                        } else {
                            component = Component.text("紅色勝利").color(NamedTextColor.RED)
                            for (board in boards.values) {
                                Method.yellowRedFirework(board.center, false)
                                Method.broadcast(component, board.center, 7)
                            }
                        }
                        end = true
                    } else if (isTie()) {
                        for (board in boards.values) {
                            Method.broadcast("平手", board.center, 7)
                        }
                        end = true
                    } else {
                        boardState[x][y][4]++
                        player = if (player == 1) {
                            2
                        } else {
                            1
                        }
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
        return false
    }

    fun isInside(x: Int, y: Int, z: Int): Boolean {
        return x in 0..<SIZE && y in 0..<SIZE && z in 0..<SIZE
    }

    private fun isWin(x: Int, y: Int, z: Int): Boolean {
        for (vx in -1..1) {
            for (vy in -1..1) {
                for (vz in 0..1) {
                    if (vx >= 0 && vy >= 0 && vz == 0) continue
                    val vectors = arrayOf(intArrayOf(vx, vy, vz), intArrayOf(-vx, -vy, -vz))
                    var counter = 0
                    for (vector in vectors) {
                        var checkX = x
                        var checkY = y
                        var checkZ = z
                        @Suppress("UNUSED_PARAMETER")
                        for (j in 1..3) {
                            checkX += vector[0]
                            checkY += vector[1]
                            checkZ += vector[2]
                            if (isInside(checkX, checkY, checkZ)) {
                                if (boardState[checkX][checkY][checkZ] == player) {
                                    counter++
                                } else {
                                    break
                                }
                            } else {
                                break
                            }
                        }
                        if (counter >= 3) return true
                    }
                }
            }
        }
        return false
    }

    private fun isTie(): Boolean {
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                for (z in 0..<SIZE) {
                    if (boardState[x][y][z] == 0) return false
                }
            }
        }
        return true
    }

    override fun display() {
        for (board in boards.values) {
            for (x in 0..<SIZE) {
                for (y in 0..<SIZE) {
                    val location = board.origin.clone()
                        .add(board.xAxis.clone().multiply(2 * x))
                        .add(board.yAxis.clone().multiply(2 * y))
                    location.block.type = Method.yellowRedGlassMaterial(-1)
                    location.add(0.0, 1.0, 0.0)
                    for (z in 0..<SIZE) {
                        location.block.type = Method.yellowRedGlassMaterial(boardState[x][y][z])
                        location.add(0.0, 1.0, 0.0)
                    }
                }
            }
        }
    }
}
