package org.ant.game

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Firework

@Suppress("FunctionName")
object Method {
    @JvmStatic
    fun isInRange(a: Int, min: Int, max: Int): Boolean {
        return (a in min..max)
    }

    @JvmStatic
    fun yellow_red_firework(location: Location, isYellow: Boolean) {
        val firework = location.world.spawn(location.clone().add(0.0, 1.0, 0.0), Firework::class.java)
        val meta = firework.fireworkMeta

        val mainColors = if (isYellow) arrayOf(Color.YELLOW, Color.NAVY) else arrayOf(Color.RED, Color.NAVY)
        val fadeColor = Color.fromRGB(14602026)

        val effect = FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(*mainColors)
            .withFade(fadeColor)
            .trail(true)
            .flicker(true)
            .build()

        meta.addEffect(effect)
        meta.power = 1

        firework.fireworkMeta = meta
    }

    @JvmStatic
    fun yellow_red_material(status: Int): Material {
        when (status) {
            -1 -> return Material.IRON_BLOCK
            0 -> return Material.AIR
            1 -> return Material.YELLOW_CONCRETE_POWDER
            2 -> return Material.RED_CONCRETE_POWDER
        }
        return Material.AIR
    }

    @JvmStatic
    fun broadcast(message: Component, location: Location, size: Int) {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (location.distance(player!!.location) <= size.toDouble() + 5) {
                player.sendMessage(message)
            }
        }
    }

    @JvmStatic
    fun broadcast(message: String, location: Location, size: Int) {
        broadcast(Component.text(message), location, size)
    }
}
