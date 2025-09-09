package org.ant.game.gameimpl

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Firework
import kotlin.text.map

object Method {
    fun isInRange(a: Int, min: Int, max: Int): Boolean {
        return (a in min..max)
    }

    fun firework(location: Location, isFirst: Boolean, firstColor: Color, secondColor: Color) {
        val firework = location.world.spawn(location.clone().add(0.0, 1.0, 0.0), Firework::class.java)
        val meta = firework.fireworkMeta

        val mainColors = if (isFirst) arrayOf(firstColor, Color.NAVY) else arrayOf(secondColor, Color.NAVY)
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
        // 黑 summon firework_rocket ~ ~1 ~ {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"large_ball",has_twinkle:true,has_trail:true,colors:[I;11250603,4408131],fade_colors:[I;14602026]}]}}}}
        // 白 summon firework_rocket ~ ~1 ~ {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"large_ball",has_twinkle:true,has_trail:true,colors:[I;11250603,15790320],fade_colors:[I;14602026]}]}}}}
    }

    fun yellowRedFirework(location: Location, isYellow: Boolean) {
        firework(location, isYellow, Color.YELLOW, Color.RED)
    }

    fun yellowRedMaterial(status: Int): Material {
        when (status) {
            -1 -> return Material.IRON_BLOCK
            0 -> return Material.AIR
            1 -> return Material.YELLOW_CONCRETE_POWDER
            2 -> return Material.RED_CONCRETE_POWDER
        }
        return Material.AIR
    }

    fun broadcast(message: Component, location: Location, size: Int) {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (location.distance(player!!.location) <= size.toDouble() + 5) {
                player.sendMessage(message)
            }
        }
    }

    fun broadcast(message: String, location: Location, size: Int) {
        broadcast(Component.text(message), location, size)
    }

    fun serialize1dBoard(board: IntArray?): String? {
        return board?.joinToString("")
    }

    fun deserialize1dBoard(board: String?): IntArray? {
        return board?.map(Char::digitToInt)?.toIntArray()
    }

    fun serialize2dBoard(board: Array<IntArray>?): List<String>? {
        return board?.map {
            it.joinToString("")
        }
    }

    fun deserialize2dBoard(board: List<String>?): Array<IntArray>? {
        return board
            ?.map { it.map(Char::digitToInt).toIntArray() }
            ?.toTypedArray()
    }

    fun serialize2dBooleanBoard(board: Array<BooleanArray>?): List<String>? {
        return board?.map { array ->
            array.joinToString("") {
                if (it) {
                    "1"
                } else {
                    "0"
                }
            }
        }
    }

    fun deserialize2dBooleanBoard(board: List<String>?): Array<BooleanArray>? {
        return board
            ?.map { arrayString -> arrayString.map { it == '1' }.toBooleanArray() }
            ?.toTypedArray()
    }

    fun serialize3dBoard(board: Array<Array<IntArray>>?): List<List<String>>? {
        return board?.map { array ->
            array.map {
                it.joinToString("")
            }
        }
    }

    fun deserialize3dBoard(board: List<List<String>>?): Array<Array<IntArray>>? {
        return board
            ?.map { y -> y.map { it.map(Char::digitToInt).toIntArray() }.toTypedArray() }
            ?.toTypedArray()
    }
}
