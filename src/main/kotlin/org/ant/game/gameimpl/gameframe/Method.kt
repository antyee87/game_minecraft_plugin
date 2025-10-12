package org.ant.game.gameimpl.gameframe

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.kyori.adventure.text.Component
import org.ant.game.gameimpl.gameframe.GameConstants.CardinalDirection
import org.ant.game.gameimpl.gameframe.GameConstants.Orientation
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Firework
import org.bukkit.util.Vector

object Method {
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

    fun blackWhiteFirework(location: Location, isBlack: Boolean) {
        firework(location, isBlack, Color.BLACK, Color.WHITE)
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

    fun yellowRedGlassMaterial(status: Int): Material {
        when (status) {
            -1 -> return Material.IRON_BLOCK
            0 -> return Material.AIR
            1 -> return Material.YELLOW_STAINED_GLASS
            2 -> return Material.RED_STAINED_GLASS
        }
        return Material.AIR
    }

    /**
     * getAxis for board by cardinal directions and orientation
     * xAxis equal to the direction
     * if the orientation is HORIZONTAL, yAxis will be the xAxis rotated 90 degrees
     */
    fun getAxis(direction: CardinalDirection, orientation: Orientation): Pair<Vector, Vector> {
        val xAxis = when (direction) {
            CardinalDirection.EAST -> Vector(1.0, 0.0, 0.0)
            CardinalDirection.SOUTH -> Vector(0.0, 0.0, 1.0)
            CardinalDirection.WEST -> Vector(-1.0, 0.0, 0.0)
            CardinalDirection.NORTH -> Vector(0.0, 0.0, -1.0)
        }

        val yAxis = when (orientation) {
            Orientation.VERTICAL_POSITIVE -> Vector(0.0, 1.0, 0.0)
            Orientation.VERTICAL_NEGATIVE -> Vector(0.0, -1.0, 0.0)
            Orientation.HORIZONTAL -> Vector(-xAxis.z, 0.0, xAxis.x)
        }
        return Pair(xAxis, yAxis)
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

    fun ConfigurationSection.toMapRecursively(): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        for (key in getKeys(false)) {
            val value = get(key)
            result[key] = if (value is ConfigurationSection) {
                value.toMapRecursively()
            } else {
                value
            }
        }
        return result
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

    fun getArgumentType(value: Any): ArgumentType<*> = when (value) {
        is Int -> IntegerArgumentType.integer()
        is Double -> DoubleArgumentType.doubleArg()
        is Boolean -> BoolArgumentType.bool()
        is String -> StringArgumentType.string()
        else -> StringArgumentType.string()
    }
}
