package org.ant.game.gameimpl

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.ant.game.BoardGame
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Skull
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.math.abs

class Chess(location: Location) :
    BoardGame(location, null, null, 8),
    ConfigurationSerializable {
    companion object {
        const val SIZE = 8

        fun profile(piece: Int): PlayerProfile {
            val textureValue = when (piece) {
                1 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg0OWRlNzBjYTg4NWIzZTFjNTE4NTE2MGI3NDA2MzlhZDFjNzgyMGJjMmI3N2QwYTVhYmMyZTU0NWY1ZTkwNSJ9fX0="
                2 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY4MjVmYWQ4MzA4OTg5ZjlkMmE1MTA4ZTEyMjM4ZmIxMDI3MGM0ZDFmZDE3YzFiNjQzNmZlOGQyYjI0MmQxMCJ9fX0="
                3 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTA0YWRiZmEzZjg1Nzg5ZGQ0Yzc3NDk5YmNhNWMyY2VjMTU3MWEwODJiM2NjMDgwMDU4NmY5YTRkMzNiNTA3OSJ9fX0="
                4 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjIyNGU4MDliOWE1ODc2OTExNzZhMWIzNGQyZDUxM2ZmODE0MGY2MDZkMjViZTU3ZjA3NWU5NmM3M2EyNWIzYiJ9fX0="
                5 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjE0M2Y2ODRlNjc5MjU5MGNjNGFkYThlODY3MDBhNTMxODc1ZjQ4Y2Y4OTE3M2M3ZWEwMjU0MjMxNDRmMGExNSJ9fX0="
                6 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzBlNzE0MDk2YTBkZDQwNDI4OTBhOTUxMGQ4NTdhNzZhNTBjMzRlODJhNDM4YzZiZjEyOTMxNWEyOWVjODViMyJ9fX0="
                11 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTg4NTUzMjk5ZmQ0ZTgzMTMyYTI2YTBlMGQyNzZiNjA0ZmVkYTVmYmEzNDg1MDFkMDc2MDI4Y2U5ODgzNDEzZSJ9fX0="
                12 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTVjMjRlYTM4MjE0NzUyOWNlN2I1ZmRhZGJhZDVhMDFkMDAwMzg0NTY1NGNhOTA0NjQwM2U1NjkxNzhlZWZiMCJ9fX0="
                13 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDExZjMzNmRlNTJiMjgzMzc4Zjk5MmM2NDk3NGRkNTYzZTQ1YjEzMDdjNTNmYzUzZjQxMTEyZWUyZGYyMTAzMCJ9fX0="
                14 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBjOWEzNDBlMDQyM2E0MTgyOTQ1NzNkMmRkYmU1OTY4MmRkMjg5ZDhjZTQ4ZWE0Y2Y1ZTVmOWY0YzY1ZjU1YiJ9fX0="
                15 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTExNzk3YjlhODIxNWRkMzRiMmZlNmQwNWJlYzFkNjM4ZDZlODdkOWVhMjZkMDMxODYwOGI5NzZkZjJmZDI1OSJ9fX0="
                16 -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQxOWVlOThhOWJkNzRjZWQ4OGY5M2FkMWI1ZTQ1NzdhOTI5NGEwZDgwZGZlMDcyOTIxYTNkMGVjZDBkZGMwNSJ9fX0="
                else -> ""
            }
            val profile = Bukkit.createProfile(UUID.randomUUID())
            profile.setProperty(ProfileProperty("textures", textureValue))
            return profile
        }

        fun deserialize(args: MutableMap<String, Any?>): Chess {
            return Chess(
                args["location"] as Location
            )
        }
    }

    var board: Array<Array<Piece?>>? = null
    var canMove: Array<BooleanArray> = Array(SIZE) { BooleanArray(SIZE) }
    var attacked: Array<BooleanArray> = Array(SIZE) { BooleanArray(SIZE) }
    var player: Int = 0
    lateinit var minecraftPlayers: Array<Player?>
    var selected: Piece? = null
    var passable: Piece? = null
    var promotable: Piece? = null
    var king: Array<Piece?> = arrayOfNulls<Piece>(2)
    var end: Boolean = false

    override fun serialize(): MutableMap<String, Any?> {
        val data: MutableMap<String, Any?> = HashMap()
        data["location"] = this.location
        return data
    }

    init {
        this.center = location.clone()
        this.center.add(SIZE.toDouble() / 2, 0.0, SIZE.toDouble() / 2)
        reset()
    }

    override fun remove() {
        super.remove()
        if (promotable != null) {
            val x = promotable!!.x
            val y = promotable!!.y
            for (i in 2..5) {
                val block = this.location.clone().add(x.toDouble(), i.toDouble(), y.toDouble()).block
                block.type = Material.AIR
            }
        }
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                location.getWorld().setType(
                    location.blockX + x,
                    location.blockY + 1,
                    location.blockZ + y,
                    Material.AIR
                )
            }
        }
    }

    fun reset() {
        val init = arrayOf(
            intArrayOf(2, 3, 4, 5, 6, 4, 3, 2),
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1)
        )
        board = Array(SIZE) { arrayOfNulls(SIZE) }
        for (i in 0..1) {
            for (j in 0..<SIZE) {
                board!![i][j] = Piece(init[i][j], i, j)
                board!![7 - i][j] = Piece(init[i][j] + 10, 7 - i, j)
            }
        }
        king[0] = board!![0][4]
        king[1] = board!![7][4]
        canMoveReset()
        attackedReset()
        if (promotable != null) {
            val x = promotable!!.x
            val y = promotable!!.y
            for (i in 2..5) {
                val block = this.location.clone().add(x.toDouble(), i.toDouble(), y.toDouble()).block
                block.type = Material.AIR
            }
        }
        minecraftPlayers = arrayOfNulls(2)
        player = 0
        selected = null
        passable = null
        promotable = null
        end = false
        display()
    }

    private fun canMoveReset() {
        for (i in 0..<SIZE) {
            for (j in 0..<SIZE) {
                canMove[i][j] = false
            }
        }
    }

    private fun attackedReset() {
        for (i in 0..<SIZE) {
            for (j in 0..<SIZE) {
                attacked[i][j] = false
            }
        }
    }

    fun display() {
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                if ((x + y) % 2 == 0) {
                    this.location.clone().add(x.toDouble(), 0.0, y.toDouble()).block.type = Material.STRIPPED_SPRUCE_WOOD
                    if (canMove[x][y]) this.location.clone().add(x.toDouble(), 0.0, y.toDouble()).block.type = Material.STRIPPED_SPRUCE_LOG
                } else {
                    this.location.clone().add(x.toDouble(), 0.0, y.toDouble()).block.type = Material.STRIPPED_BIRCH_WOOD
                    if (canMove[x][y]) this.location.clone().add(x.toDouble(), 0.0, y.toDouble()).block.type = Material.STRIPPED_BIRCH_LOG
                }
                if (board!![x][y] != null) {
                    val block = this.location.clone().add(x.toDouble(), 1.0, y.toDouble()).block
                    if (block !is Skull) block.type = Material.PLAYER_HEAD
                    val skull = block.state as Skull
                    if (skull.playerProfile == null || skull.playerProfile!!.textures != board!![x][y]!!.profile().textures) {
                        skull.setPlayerProfile(board!![x][y]!!.profile())
                        skull.update()
                    }
                } else {
                    val block = this.location.clone().add(x.toDouble(), 1.0, y.toDouble()).block
                    block.type = Material.AIR
                }
            }
        }
    }

    private fun findMove(selected: Piece, result: Array<BooleanArray>) {
        val owner = selected.owner
        val type = selected.type
        val x = selected.x
        val y = selected.y
        val rookVector = arrayOf(intArrayOf(1, 0), intArrayOf(0, 1), intArrayOf(-1, 0), intArrayOf(0, -1))
        val bishopVector = arrayOf(intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, -1), intArrayOf(-1, 1))
        val knightVector = arrayOf(intArrayOf(1, 2), intArrayOf(2, 1), intArrayOf(1, -2), intArrayOf(2, -1), intArrayOf(-1, -2), intArrayOf(-2, -1), intArrayOf(-1, 2), intArrayOf(-2, 1))
        if (type == 1) {
            val forward = if (owner == 0) {
                x + 1
            } else {
                x - 1
            }
            if (!result.contentEquals(attacked)) {
                if (board!![forward][y] == null) {
                    result[forward][y] = true
                    if (!selected.hadMoved && board!![forward * 2 - x][y] == null) result[forward * 2 - x][y] = true
                }
                if (isExist(forward, y + 1) && board!![forward][y + 1]!!.owner != owner) result[forward][y + 1] = true
                if (isExist(forward, y - 1) && board!![forward][y - 1]!!.owner != owner) result[forward][y - 1] = true
                if (isExist(x, y + 1) && board!![x][y + 1]!!.owner != owner && board!![x][y + 1] === passable) result[forward][y + 1] = true
                if (isExist(x, y - 1) && board!![x][y - 1]!!.owner != owner && board!![x][y - 1] === passable) result[forward][y - 1] = true
            } else {
                if (isInside(forward, y + 1)) result[forward][y + 1] = true
                if (isInside(forward, y - 1)) result[forward][y - 1] = true
            }
        } else if (type == 2) {
            for (vectors in rookVector) {
                iterFind(x, y, vectors, result)
            }
        } else if (type == 3) {
            for (vectors in knightVector) {
                var checkX = x
                var checkY = y
                checkX += vectors[0]
                checkY += vectors[1]
                if (isInside(checkX, checkY)) {
                    if (board!![checkX][checkY] == null) {
                        result[checkX][checkY] = true
                    } else if (board!![checkX][checkY]!!.owner != owner) {
                        result[checkX][checkY] = true
                    }
                }
            }
        } else if (type == 4) {
            for (vectors in bishopVector) {
                iterFind(x, y, vectors, result)
            }
        } else if (type == 5) {
            for (vectors in rookVector) {
                iterFind(x, y, vectors, result)
            }
            for (vectors in bishopVector) {
                iterFind(x, y, vectors, result)
            }
        } else if (type == 6) {
            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue
                    if (isInside(x + dx, y + dy) && (board!![x + dx][y + dy] == null || board!![x + dx][y + dy]!!.owner != owner)) result[x + dx][y + dy] = true
                }
            }
            if (!board!![x][y]!!.hadMoved && !attacked[x][y]) {
                var shortCastable = true
                var longCastable = true
                for (dy in -3..2) {
                    if (dy != 0 && board!![x][y + dy] != null) {
                        if (dy <= 0) longCastable = false
                        if (dy >= 0) shortCastable = false
                    }
                    if (dy != -3 && attacked[x][y + dy]) {
                        if (dy <= 0) longCastable = false
                        if (dy >= 0) shortCastable = false
                    }
                }

                if (board!![x][y + 3] != null && !board!![x][y + 3]!!.hadMoved && shortCastable) result[x][y + 2] = true
                if (board!![x][y - 4] != null && !board!![x][y - 4]!!.hadMoved && longCastable) result[x][y - 2] = true
            }
        }
    }

    private fun findAttacked() {
        attackedReset()
        for (i in 0..<SIZE) {
            for (j in 0..<SIZE) {
                if (board!![i][j] != null && board!![i][j]!!.owner != player) {
                    findMove(board!![i][j]!!, attacked)
                }
            }
        }
    }

    private fun isExist(x: Int, y: Int): Boolean {
        return (isInside(x, y) && board!![x][y] != null)
    }

    private fun iterFind(x: Int, y: Int, vectors: IntArray, result: Array<BooleanArray>) {
        val player = board!![x][y]!!.owner
        var checkX = x
        var checkY = y
        checkX += vectors[0]
        checkY += vectors[1]
        while (isInside(checkX, checkY)) {
            if (board!![checkX][checkY] == null) {
                result[checkX][checkY] = true
                checkX += vectors[0]
                checkY += vectors[1]
            } else if (board!![checkX][checkY]!!.owner != player) {
                result[checkX][checkY] = true
                break
            } else {
                break
            }
        }
    }

    private fun switchPlayer() {
        player = if (player == 0) {
            1
        } else {
            0
        }
        findAttacked()
        if (attacked[king[player]!!.x][king[player]!!.y]) broadcast("check")
    }

    fun move(x: Int, y: Int, layer: Int, minecraftPlayer: Player): Boolean {
        if (!end) {
            if (minecraftPlayers[player] == null || minecraftPlayers[player] == minecraftPlayer) {
                if (minecraftPlayers[player] == null) minecraftPlayers[player] = minecraftPlayer
                if (layer == 1 && board!![x][y] != null) {
                    if (board!![x][y]!!.owner == player) {
                        selected = board!![x][y]
                        canMoveReset()
                        findMove(selected!!, canMove)
                        display()
                        return true
                    }
                    return false
                } else {
                    if (selected != null && canMove[x][y]) {
                        val type = selected!!.type
                        val owner = selected!!.owner
                        if (type == 1) {
                            if (!selected!!.hadMoved && abs(x - selected!!.x) == 2) passable = selected
                            if (owner == 0) {
                                if (board!![x - 1][y] === passable && board!![x - 1][y] != null && board!![x - 1][y]!!.owner != player) board!![x - 1][y] = null
                                if (x == 7) promotable = selected
                            } else {
                                if (board!![x + 1][y] === passable && board!![x + 1][y] != null && board!![x + 1][y]!!.owner != player) board!![x + 1][y] = null
                                if (x == 0) promotable = selected
                            }
                        } else if (type == 6) {
                            if (!board!![selected!!.x][selected!!.y]!!.hadMoved) {
                                if (board!![x][y + 1] != null && board!![x][y + 1]!!.type == 2 && !board!![x][y + 1]!!.hadMoved) {
                                    board!![x][y - 1] = board!![x][y + 1]
                                    board!![x][y + 1] = null
                                }
                                if (board!![x][y - 2] != null && board!![x][y - 2]!!.type == 2 && !board!![x][y - 2]!!.hadMoved) {
                                    board!![x][y + 1] = board!![x][y - 2]
                                    board!![x][y - 2] = null
                                }
                            }
                        }
                        if (passable != null && passable!!.owner != player) passable = null

                        if (isExist(x, y) && board!![x][y]!!.type == 6) {
                            val component: Component?
                            if (board!![x][y]!!.owner == 0) {
                                component = Component.text("黑棋勝利").color(NamedTextColor.GRAY)
                                firework(center, true)
                            } else {
                                component = Component.text("白棋勝利").color(NamedTextColor.GRAY)
                                firework(center, false)
                            }
                            broadcast(component)
                            end = true
                        }

                        board!![x][y] = board!![selected!!.x][selected!!.y]
                        board!![selected!!.x][selected!!.y] = null
                        selected!!.setPos(x, y)
                        selected!!.hadMoved = true
                        selected = null
                        canMoveReset()
                        display()
                        if (promotable == null) {
                            switchPlayer()
                            return true
                        } else {
                            for (i in 2..5) {
                                val block = this.location.clone().add(x.toDouble(), i.toDouble(), y.toDouble()).block
                                block.type = Material.PLAYER_HEAD
                                val skull = block.state as Skull
                                skull.setPlayerProfile(profile(player * 10 + i))
                                skull.update()
                            }
                        }
                    }
                    return false
                }
            } else {
                val component = Component.text("已被玩家 " + minecraftPlayers[player]!!.name + " 綁定").color(NamedTextColor.RED)
                minecraftPlayer.sendMessage(component)
                minecraftPlayer.playSound(minecraftPlayer, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                return false
            }
        }
        return false
    }

    fun promote(choice: Int, minecraftPlayer: Player) {
        if (minecraftPlayers[player] == minecraftPlayer) {
            val x = promotable!!.x
            val y = promotable!!.y
            for (i in 2..5) {
                val block = this.location.clone().add(x.toDouble(), i.toDouble(), y.toDouble()).block
                block.type = Material.AIR
            }
            promotable!!.setPiece(player * 10 + choice)
            promotable = null
            switchPlayer()
            display()
        } else {
            val component = Component.text("已被玩家 " + minecraftPlayers[player]!!.name + " 綁定").color(NamedTextColor.RED)
            minecraftPlayer.sendMessage(component)
            minecraftPlayer.playSound(minecraftPlayer, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
        }
    }

    class Piece(piece: Int, x: Int, y: Int) {
        var piece: Int = 0
            private set
        var owner: Int = 0
        var type: Int = 0
        var x: Int = 0
        var y: Int = 0
        var hadMoved: Boolean = false

        init {
            setPiece(piece)
            setPos(x, y)
        }

        fun profile(): PlayerProfile {
            return profile(this.piece)
        }

        fun setPos(x: Int, y: Int) {
            this.x = x
            this.y = y
        }

        fun setPiece(piece: Int) {
            this.piece = piece
            this.owner = piece / 10
            this.type = piece % 10
        }
    }
}
