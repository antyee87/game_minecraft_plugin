package org.ant.game.gameimpl.gameframe

interface GameSerializable {
    fun serialize(): MutableMap<String, Any?>
}
