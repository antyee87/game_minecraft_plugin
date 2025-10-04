package org.ant.game.gameimpl.gameframe

import org.bukkit.Bukkit
import java.util.UUID

class UUIDPair {
    var uuidPair: Pair<UUID?, UUID?> = Pair(null, null)

    fun getPlayerUUID(player: Int): UUID? {
        return if (player == 1) {
            uuidPair.first
        } else {
            uuidPair.second
        }
    }

    fun putPlayerUUID(uuid: UUID): Boolean {
        uuidPair = if (uuidPair.first == null || Bukkit.getPlayer(uuidPair.first!!) == null || !Bukkit.getPlayer(uuidPair.first!!)!!.isOnline) {
            Pair(uuid, uuidPair.second)
        } else if (uuidPair.second == null || Bukkit.getPlayer(uuidPair.second!!) == null || !Bukkit.getPlayer(uuidPair.second!!)!!.isOnline) {
            Pair(uuidPair.first, uuid)
        } else {
            uuidPair
            return false
        }
        return true
    }

    fun clear() {
        uuidPair = Pair(null, null)
    }
}
