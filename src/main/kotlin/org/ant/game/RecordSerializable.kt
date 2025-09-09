package org.ant.game

interface RecordSerializable {

    fun deserializeRecord(data: Map<String, Any?>)

    fun serializeRecord(): MutableMap<String, Any?>
}
