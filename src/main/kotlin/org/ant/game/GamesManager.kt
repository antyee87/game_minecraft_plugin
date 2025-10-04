package org.ant.game

import org.ant.game.gameimpl.gameframe.GameDeSerializable
import org.ant.game.gameimpl.gameframe.GameSerializable
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object GamesManager {
    val games = hashMapOf<String, MutableMap<String, GameSerializable>>()
    val gameClasses = hashMapOf<String, KClass<out GameSerializable>>()
    val gameCompanionObjects = hashMapOf<String, GameDeSerializable>()
    inline fun <reified T : GameSerializable> addGame(gameName: String): GamesManager {
        games[gameName] = hashMapOf()
        gameClasses[gameName] = T::class
        gameCompanionObjects[gameName] = T::class.companionObjectInstance as GameDeSerializable
        return this
    }
}
