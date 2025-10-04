package org.ant.game.gameimpl.gameframe

data class GameState(
    val boardState: Any?,
    val player: Int,
    val end: Boolean
)
