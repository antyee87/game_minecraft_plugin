package org.ant.game

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import java.util.Optional

@Suppress("LocalVariableName", "PropertyName", "FunctionName")
open class BoardGame(var location: Location, display_location: Optional<Location>, display_align: Optional<String>, size: Int) {
    var center: Location
    var display_location: Location? = null
    var display_align: String? = null
    var size: Int

    init {
        display_location.ifPresent { value: Location? -> this.display_location = value }
        display_align.ifPresent { s: String? -> this.display_align = s }
        this.size = size
        this.center = location.clone()
        center.add(size.toDouble() / 2, 0.0, size.toDouble() / 2)
    }

    open fun move(x: Int, z: Int, minecraft_player: Player): Boolean {
        return false
    }

    open fun set_display(location: Location?, display_align: String?) {
        this.display_location = location
        this.display_align = display_align
    }

    open fun is_inside(x: Int, y: Int): Boolean {
        return x in 0..<size && y in 0..<size
    }

    open fun remove() {
        for (x in 0..<size) {
            for (y in 0..<size) {
                location.world.setType(
                    location.blockX + x,
                    location.blockY,
                    location.blockZ + y,
                    Material.AIR
                )
            }
        }
    }

    open fun remove_display() {
        if (display_location != null) {
            for (x in 0..<size) {
                for (y in 0..<size) {
                    if (display_align == "x") {
                        display_location!!.world.setType(
                            display_location!!.blockX + x,
                            display_location!!.blockY + y,
                            display_location!!.blockZ,
                            Material.AIR
                        )
                    } else if (display_align == "z") {
                        display_location!!.world.setType(
                            display_location!!.blockX,
                            display_location!!.blockY + x,
                            display_location!!.blockZ + y,
                            Material.AIR
                        )
                    }
                }
            }
        }
        display_location = null
        display_align = null
    }

    fun broadcast(message: Component) {
        Method.broadcast(message, center, size)
    }

    fun broadcast(message: String) {
        Method.broadcast(message, center, size)
    }

    fun firework(location: Location, isBlack: Boolean) {
        val firework = location.world.spawn(location.clone().add(0.0, 1.0, 0.0), Firework::class.java)
        val meta = firework.fireworkMeta

        val mainColors = if (isBlack) arrayOf(Color.BLACK, Color.NAVY) else arrayOf(Color.WHITE, Color.NAVY)
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
}
