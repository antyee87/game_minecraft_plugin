package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.BasicValue
import org.ant.game.Game
import org.ant.game.RecordSerializable
import org.ant.game.gameimpl.Method.broadcast
import org.ant.game.gameimpl.Method.yellowRedFirework
import org.ant.game.gameimpl.Method.yellowRedMaterial
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

class ScoreFour(var gameInstance: Game, override val location: Location) :
    ConfigurationSerializable,
    RecordSerializable,
    BasicValue {
    companion object {
        fun deserialize(gameInstance: Game, args: Map<String?, Any?>): ScoreFour {
            return ScoreFour(
                gameInstance,
                args["location"] as Location
            )
        }
    }
    override var center = location.clone().add(3.5, 2.5, 3.5)
    override val size = 7

    var board: Array<Array<IntArray>>? = null
    var top: Array<IntArray>? = null
    var selected: IntArray? = null
    var displaySelectedTask: BukkitTask? = null
    var player: Int = 0
    var end: Boolean = false
    lateinit var minecraftPlayers: Array<Player?>

    init {
        reset(board, top, false)
    }

    override fun serialize(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        data["location"] = this.location
        return data
    }

    override fun deserializeRecord(data: Map<String, Any?>) {
        @Suppress("UNCHECKED_CAST")
        val boardPreset = Method.deserialize3dBoard(data["board"] as List<List<String>>?)

        @Suppress("UNCHECKED_CAST")
        val topPreset = Method.deserialize2dBoard(data["top"] as List<String>?)
        reset(boardPreset, topPreset)
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        if (!end) {
            data["board"] = Method.serialize3dBoard(this.board)
            data["top"] = Method.serialize2dBoard(this.top)
        }
        return data
    }

    fun reset(boardPreset: Array<Array<IntArray>>?, topPreset: Array<IntArray>?, resetDisplay: Boolean = true) {
        val noPreset = boardPreset == null
        board = boardPreset ?: Array(4) { Array(4) { IntArray(4) } }
        top = topPreset ?: Array(4) { IntArray(4) }
        selected = null
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        player = 1
        minecraftPlayers = arrayOfNulls(2)
        end = false
        if (noPreset && resetDisplay) {
            for (x in 0..3) {
                for (y in 0..3) {
                    for (z in 0..4) {
                        if (z == 0) {
                            @Suppress("KotlinConstantConditions")
                            location.clone().add((2 * x).toDouble(), z.toDouble(), (2 * y).toDouble()).block.type = Material.IRON_BLOCK
                        } else {
                            location.clone().add((2 * x).toDouble(), z.toDouble(), (2 * y).toDouble()).block.type = Material.AIR
                        }
                    }
                }
            }
        }
    }

    fun remove() {
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        for (x in 0..3) {
            for (y in 0..3) {
                for (z in 0..4) {
                    location.clone().add((2 * x).toDouble(), z.toDouble(), (2 * y).toDouble()).block.type = Material.AIR
                }
            }
        }
    }

    var visible: Boolean = true

    fun move(x: Int, y: Int, minecraftPlayer: Player): Boolean {
        if (!end && top!![x][y] < 4) {
            if (minecraftPlayers[player - 1] == null || minecraftPlayers[player - 1] == minecraftPlayer) {
                if (minecraftPlayers[player - 1] == null) minecraftPlayers[player - 1] = minecraftPlayer
                if (selected == null || selected!![0] != x || selected!![1] != y) {
                    if (displaySelectedTask != null) displaySelectedTask!!.cancel()
                    if (selected != null) location.clone().add((selected!![0] * 2).toDouble(), 0.0, (selected!![1] * 2).toDouble()).block.type = Material.IRON_BLOCK
                    selected = intArrayOf(x, y)
                    visible = true
                    displaySelectedTask = Bukkit.getScheduler().runTaskTimer(
                        gameInstance,
                        Runnable {
                            val selectedBlock = location.clone().add((x * 2).toDouble(), 0.0, (y * 2).toDouble()).block
                            if (visible) {
                                selectedBlock.type = yellowRedMaterial(player)
                            } else {
                                selectedBlock.type = Material.IRON_BLOCK
                            }
                            visible = !visible
                        },
                        0,
                        10
                    )
                } else {
                    displaySelectedTask!!.cancel()
                    location.clone().add((x * 2).toDouble(), 0.0, (y * 2).toDouble()).block.type = Material.IRON_BLOCK
                    board!![x][y][top!![x][y]] = player
                    selected = null

                    location.clone().add((2 * x).toDouble(), 4.0, (2 * y).toDouble()).block.type = yellowRedMaterial(player)
                    if (isWin(x, y, top!![x][y])) {
                        val component: Component
                        if (player == 1) {
                            component = Component.text("黃色勝利").color(NamedTextColor.YELLOW)
                            yellowRedFirework(center, true)
                        } else {
                            component = Component.text("紅色勝利").color(NamedTextColor.RED)
                            yellowRedFirework(center, false)
                        }
                        broadcast(component, center, 7)
                        end = true
                    } else if (isTie()) {
                        broadcast("平手", center, 7)
                        end = true
                    } else {
                        top!![x][y] = top!![x][y] + 1
                        player = if (player == 1) {
                            2
                        } else {
                            1
                        }
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

    fun isInside(x: Int, y: Int, z: Int): Boolean {
        return x in 0..<4 && y in 0..<4 && z in 0..<4
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
                                if (board!![checkX][checkY][checkZ] == player) {
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
        for (x in 0..3) {
            for (y in 0..3) {
                for (z in 0..3) {
                    if (board!![x][y][z] == 0) return false
                }
            }
        }
        return true
    }
}
