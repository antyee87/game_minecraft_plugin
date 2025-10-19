package org.ant.game

import org.ant.game.gameimpl.gameframe.GameDeSerializable
import org.ant.game.gameimpl.gameframe.GameSerializable
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object GamesManager {
    val games = hashMapOf<KClass<out GameSerializable>, MutableMap<String, GameSerializable>>()
    val gameNames = hashMapOf<KClass<out GameSerializable>, String>()
    val gameCompanionObjects = hashMapOf<KClass<out GameSerializable>, GameDeSerializable>()
    fun <T : GameSerializable> addGame(gameName: String, clazz: KClass<T>): GamesManager {
        games[clazz] = hashMapOf()
        gameNames[clazz] = gameName
        gameCompanionObjects[clazz] = clazz.companionObjectInstance as GameDeSerializable
        return this
    }
}
