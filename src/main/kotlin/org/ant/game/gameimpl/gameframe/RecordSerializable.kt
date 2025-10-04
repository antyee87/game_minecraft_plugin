package org.ant.game.gameimpl.gameframe

interface RecordSerializable {
    fun deserializeRecord(data: Map<String, Any?>)
    fun serializeRecord(): MutableMap<String, Any?>
}
