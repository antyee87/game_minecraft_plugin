package org.ant.game

import com.mojang.brigadier.Command

class GameConfig(private val instance: Game) {
    val config = instance.config
    val configs = instance.configs

    init {
        load()
    }

    fun load() {
        configs.forEach { (configKey, gameInstances) ->
            val section = config.getConfigurationSection(configKey)
            if (section != null) {
                for (key in section.getKeys(false)) {
                    val data = section.getConfigurationSection(key)!!.getValues(false)
                    gameInstances[key] = instance.games[configKey]!!.deserialize(instance, data)
                }
            }
        }
    }

    fun save(): Int {
        configs.forEach { (configKey, gameInstances) ->
            config[configKey] = null
            for ((key, value) in gameInstances) {
                config["$configKey.$key"] = value.serialize()
            }
        }

        instance.saveConfig()

        return Command.SINGLE_SUCCESS
    }
}
