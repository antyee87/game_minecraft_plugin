package org.ant.game.gameimpl.gameframe

import org.ant.game.AntGamePlugin

interface GameDeSerializable {

    fun deserialize(pluginInstance: AntGamePlugin, args: Map<String, Any?>): GameSerializable
}
