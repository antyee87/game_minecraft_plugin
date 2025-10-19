package org.ant.game

import com.mojang.brigadier.Command
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.io.File
import kotlin.math.max
import kotlin.math.min

class GameAreaManager(private val pluginInstance: AntGamePlugin) : Listener {
    private val file = File(pluginInstance.dataFolder, "area.yml")
    private val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
    val gameAreas = hashMapOf<String, Pair<Location, Location>>()
    private var gameAreaTask: BukkitTask? = null

    init {
        load()
        gameAreaTask = Bukkit.getScheduler().runTaskTimer(
            pluginInstance,
            Runnable {
                showGameArea()
            },
            0,
            60
        )
    }

    fun load() {
        val gameAreasSection = config.getConfigurationSection("gameAreas") ?: return
        for (key in gameAreasSection.getKeys(false)) {
            @Suppress("UNCHECKED_CAST")
            gameAreas[key] = Pair(gameAreasSection["$key.first"] as Location, gameAreasSection["$key.second"] as Location)
        }
    }

    fun save(): Int {
        gameAreas.forEach { (name, area) ->
            config["gameAreas.$name.first"] = area.first
            config["gameAreas.$name.second"] = area.second
        }
        config.save(file)

        return Command.SINGLE_SUCCESS
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.hasChangedPosition()) {
            val player = event.player
            if (pluginInstance.settingsManager.settings["flyable"] as Boolean) setFly(player)
        }
    }

    fun setupGameArea(name: String, pos1: Location, pos2: Location) {
        gameAreas[name] = Pair(
            Location(pos1.world, min(pos1.x, pos2.x), min(pos1.y, pos2.y), min(pos1.z, pos2.z)),
            Location(pos1.world, max(pos1.x, pos2.x) + 1, max(pos1.y, pos2.y) + 1, max(pos1.z, pos2.z) + 1)
        )
    }

    private fun showGameArea() {
        if (!(pluginInstance.settingsManager.settings["visible"] as Boolean)) return
        for (gameArea in gameAreas.values) {
            val minX = gameArea.first.x
            val maxX = gameArea.second.x
            val minY = gameArea.first.y
            val maxY = gameArea.second.y
            val minZ = gameArea.first.z
            val maxZ = gameArea.second.z

            val vertices = mutableListOf<Location>()
            for (ix in 0..1) {
                for (iy in 0..1) {
                    for (iz in 0..1) {
                        vertices += Location(
                            gameArea.first.world,
                            if (ix == 0) minX else maxX,
                            if (iy == 0) minY else maxY,
                            if (iz == 0) minZ else maxZ
                        )
                    }
                }
            }

            for (i in vertices.indices) {
                for (j in i + 1 until vertices.size) {
                    val v1 = vertices[i]
                    val v2 = vertices[j]

                    var diffCount = 0
                    if (v1.x != v2.x) diffCount++
                    if (v1.y != v2.y) diffCount++
                    if (v1.z != v2.z) diffCount++

                    if (diffCount == 1) {
                        val vector = v2.clone().subtract(v1).toVector().normalize()
                        val distance = v1.distance(v2)
                        val v1Clone = v1.clone()
                        for (i in 0..distance.toInt()) {
                            v1.world.spawnParticle(Particle.HAPPY_VILLAGER, v1Clone, 1)
                            v1Clone.add(vector)
                        }
                    }
                }
            }
        }
    }

    private fun setFly(player: Player) {
        val location = player.location.toCenterLocation()

        for (area in gameAreas.values) {
            if (area.first.world != location.world) continue

            val pos1 = area.first
            val pos2 = area.second
            if (location.x in pos1.x..pos2.x &&
                location.y in pos1.y..pos2.y &&
                location.z in pos1.z..pos2.z
            ) {
                if (player.allowFlight) return
                player.persistentDataContainer.set(
                    NamespacedKey(pluginInstance, "is_flying"),
                    PersistentDataType.BOOLEAN,
                    true
                )
                player.allowFlight = true
            } else if (player.persistentDataContainer.get(
                    NamespacedKey(pluginInstance, "is_flying"),
                    PersistentDataType.BOOLEAN
                ) == true
            ) {
                player.persistentDataContainer.set(
                    NamespacedKey(pluginInstance, "is_flying"),
                    PersistentDataType.BOOLEAN,
                    false
                )
                player.allowFlight = false
            }
        }
    }
}
