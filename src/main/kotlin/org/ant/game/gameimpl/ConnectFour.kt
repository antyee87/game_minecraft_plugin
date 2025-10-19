package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.AntGamePlugin
import org.ant.game.gameimpl.gameframe.BoardGame
import org.ant.game.gameimpl.gameframe.GameConstants
import org.ant.game.gameimpl.gameframe.GameDeSerializable
import org.ant.game.gameimpl.gameframe.GameSerializable
import org.ant.game.gameimpl.gameframe.GameState
import org.ant.game.gameimpl.gameframe.Method
import org.ant.game.gameimpl.gameframe.RecordSerializable
import org.ant.game.gameimpl.gameframe.UUIDPair
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class ConnectFour(val pluginInstance: AntGamePlugin) :
    BoardGame(SIZE),
    GameSerializable,
    RecordSerializable {
    companion object : GameDeSerializable {
        const val SIZE = 7

        override fun deserialize(pluginInstance: AntGamePlugin, args: Map<String, Any?>): ConnectFour {
            val connectFour = ConnectFour(pluginInstance)

            @Suppress("UNCHECKED_CAST")
            val boards = args["boards"] as Map<String, Map<String, Any?>>
            for ((key, value) in boards) {
                connectFour.setBoard(
                    key,
                    value["origin"] as Location,
                    value["xAxis"] as Vector,
                    value["yAxis"] as Vector
                )
            }
            return connectFour
        }
    }
    var boardState = Array(SIZE) { IntArray(SIZE) }
    var selected: Int = -1
    var displaySelectedTask: BukkitTask? = null
    val fallingBlocks = arrayListOf<Entity>()
    var player: Int = 1
    var end: Boolean = false
    val uuidPair = UUIDPair()

    var visible: Boolean = true

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
        data["board"] = Method.serialize2dBoard(boardState)
        data["player"] = player
        data["end"] = end

        return data
    }

    override fun reset(gamePreset: GameState?) {
        if (gamePreset != null) {
            @Suppress("UNCHECKED_CAST")
            boardState = gamePreset.boardState as Array<IntArray>
            player = gamePreset.player
            end = gamePreset.end
        } else {
            boardState = Array(SIZE) { IntArray(SIZE) }
            selected = -1
            player = 1
            end = false
        }

        displaySelectedTask?.cancel()
        uuidPair.clear()
        display()
    }

    override fun move(x: Int, y: Int, z: Int, movePlayer: Player): Boolean {
        for (fallingBlock in fallingBlocks) {
            if (fallingBlock.isValid) return false
        }
        fallingBlocks.clear()
        if (!end && boardState[x][6] < 6) {
            val playerUUID = movePlayer.uniqueId
            if (selected != -1) {
                for (board in boards.values) {
                    board.origin.clone().add(board.xAxis.clone().multiply(selected)).block.type = Method.yellowRedMaterial(-1)
                }
            }
            if (uuidPair.getPlayerUUID(player) == playerUUID || uuidPair.putPlayerUUID(playerUUID)) {
                displaySelectedTask?.cancel()
                if (selected == -1 || selected != x) {
                    selected = x
                    visible = true
                    displaySelectedTask = Bukkit.getScheduler().runTaskTimer(
                        pluginInstance,
                        Runnable {
                            for (board in boards.values) {
                                val selectedBlock: Block = board.origin.clone().add(board.xAxis.clone().multiply(x)).block
                                if (visible) {
                                    selectedBlock.type = Method.yellowRedMaterial(player)
                                } else {
                                    selectedBlock.type = Method.yellowRedMaterial(-1)
                                }
                                visible = !visible
                            }
                        },
                        0,
                        10
                    )
                } else {
                    display()
                    selected = -1
                    boardState[x][boardState[x][6]] = player
                    for (board in boards.values) {
                        val location = board.origin.clone()
                            .add(board.xAxis.clone().multiply(x))
                            .add(board.yAxis.clone().multiply(6))
                            .add(0.5, 0.0, 0.5)
                        fallingBlocks.add(
                            location.world.spawn(location, FallingBlock::class.java, CreatureSpawnEvent.SpawnReason.DEFAULT) { entity ->
                                entity.blockData = Method.yellowRedMaterial(player).createBlockData()
                                entity.dropItem = false
                                entity.velocity = Vector(0.0, 0.1, 0.0)
                            }
                        )
                    }
                    if (isWin(x, boardState[x][6])) {
                        val component: Component
                        if (player == 1) {
                            component = Component.text("黃色勝利").color(NamedTextColor.YELLOW)
                            for (board in boards.values) {
                                Method.yellowRedFirework(board.center.clone().add(0.0, 4.0, 0.0), true)
                            }
                        } else {
                            component = Component.text("紅色勝利").color(NamedTextColor.RED)
                            for (board in boards.values) {
                                Method.yellowRedFirework(board.center.clone().add(0.0, 4.0, 0.0), false)
                            }
                        }
                        broadcast(component)
                        end = true
                    } else if (isTie()) {
                        broadcast("平手")
                        end = true
                    } else {
                        boardState[x][6]++
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

    override fun display() {
        for (board in boards.values) {
            for (x in 0..<SIZE) {
                val location = board.origin.clone().add(board.xAxis.clone().multiply(x))

                location.block.type = Method.yellowRedMaterial(-1)
                location.add(board.yAxis)
                for (y in 0..<6) {
                    location.block.type = Method.yellowRedMaterial(boardState[x][y])
                    location.add(board.yAxis)
                }
            }
        }
    }

    private fun isWin(x: Int, y: Int): Boolean {
        val counter = IntArray(4)
        for (i in 0..7) {
            var checkX = x
            var checkY = y
            @Suppress("UNUSED_PARAMETER")
            for (j in 1..3) {
                checkX += GameConstants.eightDirection[i][0]
                checkY += GameConstants.eightDirection[i][1]
                if (isInside(checkX, checkY) && y < 6) {
                    if (boardState[checkX][checkY] == player) {
                        counter[i % 4]++
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            if (counter[i % 4] >= 3) return true
        }
        return false
    }

    private fun isTie(): Boolean {
        for (x in 0..<SIZE) {
            for (y in 0..<6) {
                if (boardState[x][y] == 0) {
                    return false
                }
            }
        }
        return true
    }
}
