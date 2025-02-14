package org.ant.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Collection;

public class Method {
    public static boolean isInRange(int a, int min, int max){
        return (a >= min && a <= max);
    }

    public static void yellow_red_firework(Location location, boolean isYellow){
        Firework firework = location.getWorld().spawn(location.clone().add(0,1,0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        Color[] mainColors = isYellow ? new Color[]{Color.YELLOW, Color.NAVY} : new Color[]{Color.RED, Color.NAVY};
        Color fadeColor = Color.fromRGB(14602026);

        FireworkEffect effect = FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(mainColors)
            .withFade(fadeColor)
            .trail(true)
            .flicker(true)
            .build();

        meta.addEffect(effect);
        meta.setPower(1);

        firework.setFireworkMeta(meta);
    }

    public static Material yellow_red_material(int status) {
        switch(status){
            case -1:
                return Material.IRON_BLOCK;
            case 0:
                return Material.AIR;
            case 1:
                return Material.YELLOW_CONCRETE_POWDER;
            case 2:
                return Material.RED_CONCRETE_POWDER;
        }
        return Material.AIR;
    }

    public static void broadcast(Object message, Location location, int size){
        if(message instanceof String){
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(location.distance(player.getLocation()) <= (double) size / 2 + 5){
                    player.sendMessage((String)message);
                }
            });
        }
        else if(message instanceof Component){
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(location.distance(player.getLocation()) <= (double)size + 5){
                    player.sendMessage((Component)message);
                }
            });
        }
    }
}
