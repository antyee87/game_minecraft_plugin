package org.ant.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Optional;

public class BoardGame {
    public Location location;
    Location center;
    Location display_location;
    String display_align;
    int size;

    public BoardGame(Location location, Optional<Location> display_location, Optional<String> display_align, int size) {
        this.location = location;
        display_location.ifPresent(value -> this.display_location = value);
        display_align.ifPresent(s -> this.display_align = s);
        this.size = size;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
    }
    public boolean move(int x, int z){return false;}
    public void set_display(Location location, String display_align) {
        this.display_location = location;
        this.display_align = display_align;
    }

    public boolean is_inside(int x, int y){
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public void remove(){
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                location.getWorld().setType(
                    location.getBlockX()+x,
                    location.getBlockY(),
                    location.getBlockZ()+y,
                    Material.AIR);
            }
        }
    }

    public void remove_display() {
        if(display_location != null) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (display_align.equals("x")) {
                        display_location.getWorld().setType(
                            display_location.getBlockX()+x,
                            display_location.getBlockY()+y,
                            display_location.getBlockZ(),
                            Material.AIR);
                    }
                    else if (display_align.equals("z")) {
                        display_location.getWorld().setType(
                            display_location.getBlockX(),
                            display_location.getBlockY()+x,
                            display_location.getBlockZ()+y,
                            Material.AIR);
                    }
                }
            }
        }
        display_location = null;
        display_align = null;
    }
    public void broadcast(Object message){
        if(message instanceof String){
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(center.distance(player.getLocation()) <= (double)size/2+3){
                    player.sendMessage((String)message);
                }
            });
        }
        else if(message instanceof Component){
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(center.distance(player.getLocation()) <= (double)size/2+3){
                    player.sendMessage((Component)message);
                }
            });
        }
    }
    public void firework(Location location, boolean isBlack){
        Firework firework = location.getWorld().spawn(location.clone().add(0,1,0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        Color[] mainColors = isBlack ? new Color[]{Color.BLACK, Color.NAVY} : new Color[]{Color.WHITE, Color.NAVY};
        Color fadeColor = Color.fromRGB(14602026);

        FireworkEffect effect = FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(mainColors)
            .withFade(fadeColor)
            .trail(true)
            .flicker(true)
            .build();

        meta.addEffect(effect);
        meta.setPower(2);

        firework.setFireworkMeta(meta);
        //黑 summon firework_rocket ~ ~1 ~ {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"large_ball",has_twinkle:true,has_trail:true,colors:[I;11250603,4408131],fade_colors:[I;14602026]}]}}}}
        //白 summon firework_rocket ~ ~1 ~ {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"large_ball",has_twinkle:true,has_trail:true,colors:[I;11250603,15790320],fade_colors:[I;14602026]}]}}}}
    }
}
