package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.Game
import org.ant.game.RecordSerializable
import org.ant.game.gameimpl.Method.yellowRedFirework
import org.ant.game.gameimpl.Method.yellowRedMaterial
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

@Suppress("DuplicatedCode")
class ConnectFour(var gameInstance: Game, var location: Location, var align: String) :
    ConfigurationSerializable,
    RecordSerializable {
    companion object {
        fun deserialize(gameInstance: Game, args: MutableMap<String, Any?>): ConnectFour {
            return ConnectFour(
                gameInstance,
                args["location"] as Location,
                args["align"] as String
            )
        }
    }

    var center: Location? = null

    var board: Array<IntArray>? = null
    var top: IntArray? = null
    var selected: Int = -1
    var displaySelectedTask: BukkitTask? = null
    var player: Int = 0
    var end: Boolean = false
    lateinit var minecraftPlayers: Array<Player?>

    var visible: Boolean = true

    init {
        if (align == "x") {
            this.center = location.clone().add(3.5, 3.0, 0.0)
        } else if (align == "z") {
            this.center = location.clone().add(0.0, 3.0, 3.5)
        }
        reset(board, top, false)
    }

    override fun serialize(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        data["location"] = this.location
        data["align"] = this.align
        return data
    }

    override fun deserializeRecord(data: Map<String, Any?>) {
        @Suppress("UNCHECKED_CAST")
        val boardPreset = Method.deserialize2dBoard(data["board"] as List<String>?)
        val topPreset = Method.deserialize1dBoard(data["top"] as String?)
        reset(boardPreset, topPreset)
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        if (!end) {
            data["board"] = Method.serialize2dBoard(this.board)
            data["top"] = Method.serialize1dBoard(this.top)
        }
        return data
    }

    fun reset(boardPreset: Array<IntArray>?, topPreset: IntArray?, resetDisplay: Boolean = true) {
        val noPreset = boardPreset == null
        board = boardPreset ?: Array(6) { IntArray(7) }
        top = topPreset ?: IntArray(7)
        selected = -1
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        player = 1
        minecraftPlayers = arrayOfNulls(2)
        end = false
        if (noPreset && resetDisplay) {
            for (x in 0..6) {
                for (y in 0..6) {
                    var status = 0
                    if (x == 0) {
                        status = -1
                    }
                    if (align == "x") {
                        location.clone().add(y.toDouble(), x.toDouble(), 0.0).block.type = yellowRedMaterial(status)
                    } else if (align == "z") {
                        location.clone().add(0.0, x.toDouble(), y.toDouble()).block.type = yellowRedMaterial(status)
                    }
                }
            }
        }
    }

    fun remove() {
        if (displaySelectedTask != null) displaySelectedTask!!.cancel()
        for (x in 0..6) {
            var status = 0
            if (x == 0) {
                status = -1
            }
            if (align == "x") {
                location.clone().add(x.toDouble(), 0.0, 0.0).block.type = Material.AIR
            } else if (align == "z") {
                location.clone().add(0.0, 0.0, x.toDouble()).block.type = Material.AIR
            }
        }
    }

    fun move(y: Int, minecraftPlayer: Player): Boolean {
        if (!end && top!![y] < 6) {
            if (minecraftPlayers[player - 1] == null || minecraftPlayers[player - 1] == minecraftPlayer) {
                if (minecraftPlayers[player - 1] == null) minecraftPlayers[player - 1] = minecraftPlayer
                if (selected == -1 || selected != y) {
                    if (displaySelectedTask != null) displaySelectedTask!!.cancel()
                    if (selected != -1) {
                        var block: Block? = null
                        if (align == "x") {
                            block = location.clone().add(selected.toDouble(), 0.0, 0.0).block
                        } else if (align == "z") {
                            block = location.clone().add(0.0, 0.0, selected.toDouble()).block
                        }
                        block!!.type = yellowRedMaterial(-1)
                    }
                    selected = y
                    visible = true
                    displaySelectedTask = Bukkit.getScheduler().runTaskTimer(
                        gameInstance,
                        Runnable {
                            var selectedBlock: Block? = null
                            if (align == "x") {
                                selectedBlock = location.clone().add(y.toDouble(), 0.0, 0.0).block
                            } else if (align == "z") {
                                selectedBlock = location.clone().add(0.0, 0.0, y.toDouble()).block
                            }

                            if (visible) {
                                selectedBlock!!.type = yellowRedMaterial(player)
                            } else {
                                selectedBlock!!.type = yellowRedMaterial(-1)
                            }
                            visible = !visible
                        },
                        0,
                        10
                    )
                } else {
                    displaySelectedTask!!.cancel()
                    var block: Block? = null
                    if (align == "x") {
                        block = location.clone().add(y.toDouble(), 0.0, 0.0).block
                    } else if (align == "z") {
                        block = location.clone().add(0.0, 0.0, y.toDouble()).block
                    }
                    block!!.type = yellowRedMaterial(-1)
                    selected = -1

                    board!![top!![y]][y] = player
                    if (align == "x") {
                        location.clone().add(y.toDouble(), 6.0, 0.0).block.type = yellowRedMaterial(player)
                    } else if (align == "z") {
                        location.clone().add(0.0, 6.0, y.toDouble()).block.type = yellowRedMaterial(player)
                    }
                    if (isWin(top!![y], y)) {
                        val component: Component
                        if (player == 1) {
                            component = Component.text("黃色勝利").color(NamedTextColor.YELLOW)
                            yellowRedFirework(center!!.clone().add(0.0, 3.0, 0.0), true)
                        } else {
                            component = Component.text("紅色勝利").color(NamedTextColor.RED)
                            yellowRedFirework(center!!.clone().add(0.0, 3.0, 0.0), false)
                        }
                        Method.broadcast(component, center!!, 7)
                        end = true
                    } else if (isTie()) {
                        Method.broadcast("平手", center!!, 7)
                        end = true
                    } else {
                        top!![y]++
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

    fun isInside(x: Int, y: Int): Boolean {
        return x in 0..<6 && y in 0..<7
    }

    private fun isWin(x: Int, y: Int): Boolean {
        val vectors = arrayOf<IntArray?>(intArrayOf(1, 0), intArrayOf(0, 1), intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 0), intArrayOf(0, -1), intArrayOf(-1, -1), intArrayOf(-1, 1))
        val counter = IntArray(4)
        for (i in 0..7) {
            var checkX = x
            var checkY = y
            @Suppress("UNUSED_PARAMETER")
            for (j in 1..3) {
                checkX += vectors[i]!![0]
                checkY += vectors[i]!![1]
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
            if (counter[i % 4] >= 3) return true
        }
        return false
    }

    private fun isTie(): Boolean {
        for (x in 0..5) {
            for (y in 0..6) {
                if (board!![x][y] == 0) {
                    return false
                }
            }
        }
        return true
    }
}
