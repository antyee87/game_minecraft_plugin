package org.ant.game.gameimpl

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
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
import org.bukkit.block.Skull
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.collections.arrayListOf
import kotlin.math.abs

class Chess(private val pluginInstance: AntGamePlugin) :
    BoardGame(SIZE),
    GameSerializable,
    RecordSerializable {
    companion object : GameDeSerializable {
        const val SIZE = 8

        val init = arrayOf(
            intArrayOf(2, 3, 4, 6, 5, 4, 3, 2),
            intArrayOf(1, 1, 1, 1, 1, 1, 1, 1)
        )

        val rookVector = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(0, 1),
            intArrayOf(-1, 0),
            intArrayOf(0, -1)
        )
        val bishopVector = arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, -1),
            intArrayOf(-1, -1),
            intArrayOf(-1, 1)
        )
        val knightVector = arrayOf(
            intArrayOf(1, 2),
            intArrayOf(2, 1),
            intArrayOf(1, -2),
            intArrayOf(2, -1),
            intArrayOf(-1, -2),
            intArrayOf(-2, -1),
            intArrayOf(-1, 2),
            intArrayOf(-2, 1)
        )

        override fun deserialize(pluginInstance: AntGamePlugin, args: Map<String, Any?>): Chess {
            val chess = Chess(pluginInstance)

            @Suppress("UNCHECKED_CAST")
            val boards = args["boards"] as Map<String, Map<String, Any?>>
            for ((key, value) in boards) {
                chess.setBoard(
                    key,
                    value["origin"] as Location,
                    value["xAxis"] as Vector,
                    value["yAxis"] as Vector
                )
            }
            return chess
        }
    }

    enum class PieceType(val value: Int) {
        PAWN(1),
        ROOK(2),
        KNIGHT(3),
        BISHOP(4),
        QUEEN(5),
        KING(6);

        companion object {
            private val byNumber = entries.associateBy(PieceType::value)

            fun fromNumber(number: Int): PieceType? = byNumber[number]
        }
    }

    enum class Color(val value: Int) {
        WHITE(0),
        BLACK(1);

        companion object {
            private val byNumber = entries.associateBy(Color::value)

            fun fromNumber(number: Int): Color? = byNumber[number]
        }
    }

    enum class MoveType(val value: Int) {
        NORMAL(1),
        CAPTURE(2),
        PROMOTION(3),
        EN_PASSANT(4),
        CASTLING(5),
    }

    class Piece(var type: PieceType, val color: Color, var hadMoved: Boolean = false) {
        fun texture(): String {
            return when (color) {
                Color.WHITE -> {
                    when (type) {
                        PieceType.PAWN -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg0OWRlNzBjYTg4NWIzZTFjNTE4NTE2MGI3NDA2MzlhZDFjNzgyMGJjMmI3N2QwYTVhYmMyZTU0NWY1ZTkwNSJ9fX0="
                        PieceType.ROOK -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY4MjVmYWQ4MzA4OTg5ZjlkMmE1MTA4ZTEyMjM4ZmIxMDI3MGM0ZDFmZDE3YzFiNjQzNmZlOGQyYjI0MmQxMCJ9fX0="
                        PieceType.KNIGHT -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTA0YWRiZmEzZjg1Nzg5ZGQ0Yzc3NDk5YmNhNWMyY2VjMTU3MWEwODJiM2NjMDgwMDU4NmY5YTRkMzNiNTA3OSJ9fX0="
                        PieceType.BISHOP -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjIyNGU4MDliOWE1ODc2OTExNzZhMWIzNGQyZDUxM2ZmODE0MGY2MDZkMjViZTU3ZjA3NWU5NmM3M2EyNWIzYiJ9fX0="
                        PieceType.QUEEN -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjE0M2Y2ODRlNjc5MjU5MGNjNGFkYThlODY3MDBhNTMxODc1ZjQ4Y2Y4OTE3M2M3ZWEwMjU0MjMxNDRmMGExNSJ9fX0="
                        PieceType.KING -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzBlNzE0MDk2YTBkZDQwNDI4OTBhOTUxMGQ4NTdhNzZhNTBjMzRlODJhNDM4YzZiZjEyOTMxNWEyOWVjODViMyJ9fX0="
                    }
                }
                Color.BLACK -> {
                    when (type) {
                        PieceType.PAWN -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTg4NTUzMjk5ZmQ0ZTgzMTMyYTI2YTBlMGQyNzZiNjA0ZmVkYTVmYmEzNDg1MDFkMDc2MDI4Y2U5ODgzNDEzZSJ9fX0="
                        PieceType.ROOK -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTVjMjRlYTM4MjE0NzUyOWNlN2I1ZmRhZGJhZDVhMDFkMDAwMzg0NTY1NGNhOTA0NjQwM2U1NjkxNzhlZWZiMCJ9fX0="
                        PieceType.KNIGHT -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDExZjMzNmRlNTJiMjgzMzc4Zjk5MmM2NDk3NGRkNTYzZTQ1YjEzMDdjNTNmYzUzZjQxMTEyZWUyZGYyMTAzMCJ9fX0="
                        PieceType.BISHOP -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBjOWEzNDBlMDQyM2E0MTgyOTQ1NzNkMmRkYmU1OTY4MmRkMjg5ZDhjZTQ4ZWE0Y2Y1ZTVmOWY0YzY1ZjU1YiJ9fX0="
                        PieceType.QUEEN -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTExNzk3YjlhODIxNWRkMzRiMmZlNmQwNWJlYzFkNjM4ZDZlODdkOWVhMjZkMDMxODYwOGI5NzZkZjJmZDI1OSJ9fX0="
                        PieceType.KING -> "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQxOWVlOThhOWJkNzRjZWQ4OGY5M2FkMWI1ZTQ1NzdhOTI5NGEwZDgwZGZlMDcyOTIxYTNkMGVjZDBkZGMwNSJ9fX0="
                    }
                }
            }
        }

        fun profile(): PlayerProfile {
            val profile = Bukkit.createProfile(UUID.randomUUID())
            profile.setProperty(ProfileProperty("textures", texture()))
            return profile
        }
    }

    data class Move(
        val to: Pos,
        val captured: Piece? = null,
        val moveType: MoveType = MoveType.NORMAL
    )

    var boardState: Array<Array<Piece?>> = Array(SIZE) { arrayOfNulls(SIZE) }

    /**
     * White: 0, Black: 1
     */
    var player: Color = Color.WHITE
    val uuidPair = UUIDPair()
    var moveable = Array(SIZE) { BooleanArray(SIZE) }
    var movesList = listOf<Move>()
    var selected: Pos? = null
    var end: Boolean = false

    var enPassantTarget: Pos? = null
    var promotionTarget: Pos? = null

    override fun serialize(): MutableMap<String, Any?> {
        val data: MutableMap<String, Any?> = HashMap()
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
        val board = data["board"] as? List<Int> ?: return
        val tmpBoardState: Array<Array<Piece?>> = Array(SIZE) { arrayOfNulls(SIZE) }
        for (pieceData in board) {
            var value = pieceData
            val data = arrayListOf<Int>()
            while (value > 0) {
                data.add(value % 10)
                value /= 10
            }
            tmpBoardState[data[0]][data[1]] = Piece(PieceType.fromNumber(data[4])!!, Color.fromNumber(data[3])!!, data[2] == 1)
        }
        @Suppress("UNCHECKED_CAST")
        if (data.containsKey("enPassantTarget")) enPassantTarget = Pos((data["enPassantTarget"] as List<Int>)[0], (data["enPassantTarget"] as List<Int>)[1])
        @Suppress("UNCHECKED_CAST")
        if (data.containsKey("promotionTarget")) promotionTarget = Pos((data["promotionTarget"] as List<Int>)[0], (data["promotionTarget"] as List<Int>)[1])
        reset(
            GameState(
                tmpBoardState,
                data["player"] as Int,
                data["end"] as Boolean
            )
        )
    }

    override fun serializeRecord(): MutableMap<String, Any?> {
        val data = HashMap<String, Any?>()
        val board = arrayListOf<Int>()
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                if (boardState[x][y] != null) {
                    var hadMoved = 0
                    if (boardState[x][y]!!.hadMoved) hadMoved = 1
                    board.add(x + 10 * y + 100 * hadMoved + 1000 * boardState[x][y]!!.color.value + 10000 * boardState[x][y]!!.type.value)
                }
            }
        }
        data["board"] = board
        data["player"] = player.value
        data["end"] = end
        if (enPassantTarget != null) data["enPassantTarget"] = listOf(enPassantTarget!!.x, enPassantTarget!!.y)
        if (promotionTarget != null) data["promotionTarget"] = listOf(promotionTarget!!.x, promotionTarget!!.y)
        return data
    }

    init {
        reset(null)
    }

    override fun remove(removed: String?) {
        val removed: List<String> = if (removed == null) {
            boards.keys.toList()
        } else {
            listOf(removed)
        }
        for (name in removed) {
            val board = boards[name] ?: continue
            for (x in 0..<SIZE) {
                val location = board.origin.clone().add(board.xAxis.clone().multiply(x))
                for (y in 0..<SIZE) {
                    board.origin.world.setType(
                        location,
                        Material.AIR
                    )
                    location.add(board.yAxis)
                }
            }
            for (x in 0..<SIZE) {
                val location = board.origin.clone()
                    .add(board.xAxis.clone().multiply(x))
                    .add(0.0, 1.0, 0.0)
                for (y in 0..<SIZE) {
                    board.origin.world.setType(
                        location,
                        Material.AIR
                    )
                    location.add(board.yAxis)
                }
            }
            if (promotionTarget != null) {
                for (z in 2..5) {
                    board.origin.clone()
                        .add(board.xAxis.clone().multiply(promotionTarget!!.x))
                        .add(board.yAxis.clone().multiply(promotionTarget!!.y))
                        .add(0.0, z.toDouble(), 0.0)
                        .block.type = Material.AIR
                }
            }
            boards.remove(name)
        }
    }

    override fun reset(gamePreset: GameState?) {
        if (gamePreset != null) {
            @Suppress("UNCHECKED_CAST")
            boardState = gamePreset.boardState as Array<Array<Piece?>>
            player = Color.fromNumber(gamePreset.player)!!
            end = gamePreset.end
        } else {
            boardState = Array(SIZE) { arrayOfNulls(SIZE) }
            for (x in 0..<SIZE) {
                for (y in 0..1) {
                    boardState[x][y] = Piece(PieceType.fromNumber(init[y][x])!!, Color.WHITE)
                    boardState[x][7 - y] = Piece(PieceType.fromNumber(init[y][x])!!, Color.BLACK)
                }
            }
            end = false
            player = Color.WHITE

            if (promotionTarget != null) {
                for (board in boards.values) {
                    for (z in 2..5) {
                        board.origin.clone()
                            .add(board.xAxis.clone().multiply(promotionTarget!!.x))
                            .add(board.yAxis.clone().multiply(promotionTarget!!.y))
                            .add(0.0, z.toDouble(), 0.0)
                            .block.type = Material.AIR
                    }
                }
                enPassantTarget = null
                promotionTarget = null
            }
        }
        uuidPair.clear()
        selected = null
        moveable = Array(SIZE) { BooleanArray(SIZE) }

        display()
    }

    override fun display() {
        for (board in boards.values) {
            for (x in 0..<SIZE) {
                val location = board.origin.clone().add(board.xAxis.clone().multiply(x))
                for (y in 0..<SIZE) {
                    if (moveable[x][y]) {
                        if ((x + y) % 2 == 1) {
                            location.block.type = Material.STRIPPED_SPRUCE_LOG
                        } else {
                            location.block.type = Material.STRIPPED_BIRCH_LOG
                        }
                    } else {
                        if ((x + y) % 2 == 1) {
                            location.block.type = Material.STRIPPED_SPRUCE_WOOD
                        } else {
                            location.block.type = Material.STRIPPED_BIRCH_WOOD
                        }
                    }

                    val block = location.clone().add(0.0, 1.0, 0.0).block
                    if (boardState[x][y] != null) {
                        if (block !is Skull) block.type = Material.PLAYER_HEAD
                        val skull = block.state as Skull
                        if (skull.playerProfile?.textures != boardState[x][y]!!.profile().textures) {
                            skull.setPlayerProfile(boardState[x][y]!!.profile())
                            skull.update()
                        }
                    } else {
                        block.type = Material.AIR
                    }
                    location.add(board.yAxis)
                }
            }
        }
    }

    private fun generatePieceMoves(pos: Pos): List<Move> {
        val moves = arrayListOf<Move>()
        val x = pos.x
        val y = pos.y
        val piece = boardState[x][y] ?: return listOf()
        when (piece.type) {
            PieceType.PAWN -> {
                val forward = if (piece.color.value == 0) {
                    y + 1
                } else {
                    y - 1
                }

                for (i in arrayOf(-1, 1)) {
                    if (isInside(x + i, forward) &&
                        boardState[x + i][forward] != null &&
                        boardState[x + i][forward]!!.color != piece.color
                    ) {
                        moves.add(
                            Move(
                                Pos(x + i, forward),
                                boardState[x + i][forward],
                                MoveType.CAPTURE
                            )
                        )
                    }

                    if (isInside(x + i, y) && Pos(x + i, y) == enPassantTarget) {
                        moves.add(
                            Move(
                                Pos(x + i, forward),
                                boardState[x + i][y],
                                MoveType.EN_PASSANT
                            )
                        )
                    }
                }

                if (isInside(x, forward) && boardState[x][forward] == null) {
                    moves.add(
                        Move(
                            Pos(x, forward)
                        )
                    )
                }

                if (!piece.hadMoved &&
                    isInside(x, forward * 2 - y) &&
                    boardState[x][forward * 2 - y] == null
                ) {
                    moves.add(
                        Move(
                            Pos(x, forward * 2 - y)
                        )
                    )
                }
            }
            PieceType.ROOK -> {
                for (vector in rookVector) {
                    iterFind(pos, piece, vector, moves)
                }
            }
            PieceType.KNIGHT -> {
                for (vector in knightVector) {
                    val dx = vector[0]
                    val dy = vector[1]
                    if (isInside(x + dx, y + dy)) {
                        if (boardState[x + dx][y + dy] == null) {
                            moves.add(Move(Pos(x + dx, y + dy)))
                        } else if (boardState[x + dx][y + dy]!!.color != piece.color) {
                            moves.add(
                                Move(
                                    Pos(x + dx, y + dy),
                                    boardState[x + dx][y + dy],
                                    MoveType.CAPTURE
                                )
                            )
                        }
                    }
                }
            }
            PieceType.BISHOP -> {
                for (vector in bishopVector) {
                    iterFind(pos, piece, vector, moves)
                }
            }
            PieceType.QUEEN -> {
                for (vector in rookVector) {
                    iterFind(pos, piece, vector, moves)
                }
                for (vector in bishopVector) {
                    iterFind(pos, piece, vector, moves)
                }
            }
            PieceType.KING -> {
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        if (isInside(x + dx, y + dy)) {
                            if (boardState[x + dx][y + dy] == null) {
                                moves.add(Move(Pos(x + dx, y + dy)))
                            } else if (boardState[x + dx][y + dy]!!.color != piece.color) {
                                moves.add(
                                    Move(
                                        Pos(x + dx, y + dy),
                                        boardState[x + dx][y + dy],
                                        MoveType.CAPTURE
                                    )
                                )
                            }
                        }
                    }
                }
                if (!boardState[x][y]!!.hadMoved) {
                    var canShortCastling = true
                    var canLongCastling = true
                    for (dx in -2..3) {
                        if (dx == 0) continue
                        if (boardState[x + dx][y] != null) {
                            if (dx <= 0) canShortCastling = false
                            if (dx >= 0) canLongCastling = false
                        }
                    }

                    if (canShortCastling && boardState[0][y]?.hadMoved == false)moves.add(Move(Pos(x - 2, y), null, MoveType.CASTLING))
                    if (canLongCastling && boardState[7][y]?.hadMoved == false)moves.add(Move(Pos(x + 2, y), null, MoveType.CASTLING))
                }
            }
        }

        val result = mutableListOf<Move>()
        var king: Pos? = null
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                if (boardState[x][y]?.type == PieceType.KING && boardState[x][y]?.color == piece.color) {
                    king = Pos(x, y)
                }
            }
        }
        if (king == null) return listOf()

        for (move in moves) {
            if (boardState[x][y]?.type == PieceType.KING && boardState[x][y]?.color == piece.color) king = move.to
            when (move.moveType) {
                MoveType.NORMAL, MoveType.CAPTURE, MoveType.PROMOTION -> {
                    boardState[move.to.x][move.to.y] = boardState[x][y]
                    boardState[x][y] = null
                    if (!isAttacked(king!!, piece.color)) result.add(move)
                    boardState[x][y] = boardState[move.to.x][move.to.y]
                    boardState[move.to.x][move.to.y] = move.captured
                }

                MoveType.EN_PASSANT -> {
                    val backward = if (piece.color == Color.WHITE) {
                        move.to.y - 1
                    } else {
                        move.to.y + 1
                    }
                    boardState[move.to.x][move.to.y] = boardState[x][y]
                    boardState[x][y] = null
                    boardState[x][backward] = null
                    if (!isAttacked(king!!, piece.color)) result.add(move)
                    boardState[x][y] = boardState[move.to.x][move.to.y]
                    boardState[move.to.x][move.to.y] = null
                    boardState[move.to.x][backward] = move.captured
                }

                MoveType.CASTLING -> {
                    val dx = (move.to.x - x) / abs(move.to.x - x)
                    var canCastling = true
                    for (i in 0..2) {
                        if (isAttacked(Pos(x + i * dx, y), piece.color)) {
                            canCastling = false
                            break
                        }
                    }
                    if (canCastling) result.add(move)
                }
            }
        }
        return result
    }

    private fun iterFind(pos: Pos, piece: Piece, vector: IntArray, result: MutableList<Move>) {
        val x = pos.x
        val y = pos.y
        var dx = vector[0]
        var dy = vector[1]
        while (isInside(x + dx, y + dy)) {
            if (boardState[x + dx][y + dy] == null) {
                result.add(Move(Pos(x + dx, y + dy)))
            } else {
                if (boardState[x + dx][y + dy]!!.color != piece.color) {
                    result.add(
                        Move(
                            Pos(x + dx, y + dy),
                            boardState[x + dx][y + dy],
                            MoveType.CAPTURE
                        )
                    )
                }
                break
            }
            dx += vector[0]
            dy += vector[1]
        }
    }

    private fun isAttacked(pos: Pos, color: Color): Boolean {
        val x = pos.x
        val y = pos.y

        val forward = if (color == Color.WHITE) {
            y + 1
        } else {
            y - 1
        }

        for (i in arrayOf(-1, 1)) {
            if (isInside(x + i, forward) &&
                boardState[x + i][forward] != null &&
                boardState[x + i][forward]!!.color != color &&
                boardState[x + i][forward]!!.type == PieceType.PAWN
            ) {
                return true
            }
        }

        for (vector in rookVector) {
            var dx = vector[0]
            var dy = vector[1]
            while (isInside(x + dx, y + dy)) {
                if (boardState[x + dx][y + dy] != null) {
                    if (boardState[x + dx][y + dy]!!.color != color &&
                        (boardState[x + dx][y + dy]!!.type == PieceType.ROOK || boardState[x + dx][y + dy]!!.type == PieceType.QUEEN)
                    ) {
                        return true
                    }
                    break
                }
                dx += vector[0]
                dy += vector[1]
            }
        }

        for (vector in bishopVector) {
            var dx = vector[0]
            var dy = vector[1]
            while (isInside(x + dx, y + dy)) {
                if (boardState[x + dx][y + dy] != null) {
                    if (boardState[x + dx][y + dy]!!.color != color &&
                        (boardState[x + dx][y + dy]!!.type == PieceType.BISHOP || boardState[x + dx][y + dy]!!.type == PieceType.QUEEN)
                    ) {
                        return true
                    }
                    break
                }
                dx += vector[0]
                dy += vector[1]
            }
        }

        for (vector in knightVector) {
            val dx = vector[0]
            val dy = vector[1]
            if (isInside(x + dx, y + dy)) {
                if (boardState[x + dx][y + dy] != null &&
                    boardState[x + dx][y + dy]!!.color != color &&
                    boardState[x + dx][y + dy]!!.type == PieceType.KNIGHT
                ) {
                    return true
                }
            }
        }

        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                if (isInside(x + dx, y + dy)) {
                    if (boardState[x + dx][y + dy] != null &&
                        boardState[x + dx][y + dy]!!.color != color &&
                        boardState[x + dx][y + dy]!!.type == PieceType.KING
                    ) {
                        return true
                    }
                }
            }
        }

        return false
    }

    override fun move(x: Int, y: Int, z: Int, movePlayer: Player): Boolean {
        if (!end) {
            val uuid = movePlayer.uniqueId
            if (uuidPair.getPlayerUUID(player.value + 1) == uuid || uuidPair.putPlayerUUID(uuid)) {
                if (promotionTarget != null) {
                    if (z !in 2..5) return false
                    val x = promotionTarget!!.x
                    val y = promotionTarget!!.y
                    for (board in boards.values) {
                        val location = board.origin.clone()
                            .add(board.xAxis.clone().multiply(x))
                            .add(board.yAxis.clone().multiply(y))
                            .add(0.0, 1.0, 0.0)
                        location.world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                        for (z in 1..4) {
                            location.add(0.0, 1.0, 0.0).block.type = Material.AIR
                        }
                    }
                    boardState[x][y]!!.type = PieceType.fromNumber(z)!!
                    promotionTarget = null
                    switchPlayer()
                    display()
                    return true
                } else if (boardState[x][y] != null && boardState[x][y]!!.color == player &&
                    (selected == null || (selected!!.x != x || selected!!.y != y))
                ) {
                    moveable = Array(SIZE) { BooleanArray(SIZE) }
                    selected = Pos(x, y)
                    movesList = generatePieceMoves(Pos(x, y))
                    for (move in movesList) moveable[move.to.x][move.to.y] = true
                    display()
                    return true
                } else if (selected != null && moveable[x][y]) {
                    val type = boardState[selected!!.x][selected!!.y]?.type
                    val color = boardState[selected!!.x][selected!!.y]?.color

                    var move: Move? = null
                    for (tmpMove in movesList) {
                        if (tmpMove.to == Pos(x, y)) {
                            move = tmpMove
                            break
                        }
                    }
                    if (move == null) return false

                    enPassantTarget = null

                    boardState[x][y] = boardState[selected!!.x][selected!!.y]
                    boardState[selected!!.x][selected!!.y] = null
                    boardState[x][y]!!.hadMoved = true
                    if (type == PieceType.PAWN) {
                        if (abs(selected!!.y - y) == 2) {
                            enPassantTarget = move.to
                        } else if (move.moveType == MoveType.EN_PASSANT) {
                            val backward = if (color == Color.WHITE) {
                                y - 1
                            } else {
                                y + 1
                            }
                            boardState[x][backward] = null
                        } else if (y == 0 || y == 7) {
                            broadcast("Promotion")
                            promotionTarget = Pos(x, y)
                        }
                    }
                    if (move.moveType == MoveType.CASTLING) {
                        if (x == 1) {
                            boardState[x + 1][y] = boardState[0][y]
                            boardState[0][y] = null
                        } else if (x == 5) {
                            boardState[x - 1][y] = boardState[7][y]
                            boardState[7][y] = null
                        }
                    }
                    selected = null
                    moveable = Array(SIZE) { BooleanArray(SIZE) }

                    display()
                    if (promotionTarget == null) {
                        switchPlayer()
                    } else {
                        for (board in boards.values) {
                            val location = board.origin.clone()
                                .add(board.xAxis.clone().multiply(x))
                                .add(board.yAxis.clone().multiply(y))
                                .add(0.0, 1.0, 0.0)
                            for (i in 2..5) {
                                location.add(0.0, 1.0, 0.0)
                                location.block.type = Material.PLAYER_HEAD
                                val skull = location.block.state as Skull
                                skull.setPlayerProfile(Piece(PieceType.fromNumber(i)!!, color!!).profile())
                                skull.update()
                            }
                        }
                    }
                    return true
                }
                return false
            } else {
                val component = Component.text("已被玩家 " + Bukkit.getPlayer(uuidPair.getPlayerUUID(player.value + 1)!!)?.name + " 綁定").color(NamedTextColor.RED)
                movePlayer.sendMessage(component)
                movePlayer.playSound(movePlayer, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
                return false
            }
        }
        return false
    }

    private fun switchPlayer() {
        player = if (player == Color.WHITE) {
            Color.BLACK
        } else {
            Color.WHITE
        }

        lateinit var king: Pos
        var hasMove = false
        for (x in 0..<SIZE) {
            for (y in 0..<SIZE) {
                if (boardState[x][y]?.color == player) {
                    if (boardState[x][y]?.type == PieceType.KING) king = Pos(x, y)
                    if (!hasMove) {
                        if (generatePieceMoves(Pos(x, y)).isNotEmpty()) hasMove = true
                    }
                }
            }
        }
        if (isAttacked(king, player)) {
            broadcast("Check!")
            if (!hasMove) {
                broadcast("Checkmate.")
                val component: Component?
                if (player == Color.WHITE) {
                    component = Component.text("黑棋勝利").color(NamedTextColor.GRAY)
                    for (board in boards.values) {
                        Method.blackWhiteFirework(board.origin, true)
                    }
                } else {
                    component = Component.text("白棋勝利").color(NamedTextColor.GRAY)
                    for (board in boards.values) {
                        Method.blackWhiteFirework(board.origin, false)
                    }
                }
                broadcast(component)
                end = true
            }
        } else if (!hasMove) {
            broadcast("Draw")
            end = true
        }
    }
}
