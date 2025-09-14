package org.ant.game

interface GameDeSerializable {

    fun deserialize(gameInstance: Game, args: Map<String, Any?>): GameSerializable
}
