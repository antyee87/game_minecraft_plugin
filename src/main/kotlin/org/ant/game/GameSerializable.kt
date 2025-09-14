package org.ant.game

import org.bukkit.configuration.serialization.ConfigurationSerializable

interface GameSerializable :
    ConfigurationSerializable,
    RecordSerializable {

    override fun serialize(): MutableMap<String, Any?>
}
