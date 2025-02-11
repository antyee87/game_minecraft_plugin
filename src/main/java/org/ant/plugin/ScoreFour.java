package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.HashMap;
import java.util.Map;

public class ScoreFour implements ConfigurationSerializable {
    Location location;
    Location center;
    int[][][] board;
    int[][] top;
    int player;
    boolean end;
    public ScoreFour(Location location) {
        this.location = location;
        this.center = location.clone().add(3.5, 2.5, 3.5);
        reset();
    }
    public void reset(){
        for(int x=0; x<4; x++){
            for(int y=0; y<4; y++){
                for(int z=0; z<5; z++){
                    if(z == 0)location.clone().add(2*x, z, 2*y).getBlock().setType(Material.IRON_BLOCK);
                    else location.clone().add(2*x, z, 2*y).getBlock().setType(Material.AIR);
                }
            }
        }
        board = new int[4][4][4];
        top = new int[4][4];
        player = 1;
        end = false;
    }
    public void remove(){
        for(int x=0; x<4; x++){
            for(int y=0; y<4; y++){
                for(int z=0; z<5; z++){
                    location.clone().add(2*x, z, 2*y).getBlock().setType(Material.AIR);
                }
            }
        }
    }

    private Material material(int player) {
        if(player == 1)return Material.YELLOW_CONCRETE_POWDER;
        else return Material.RED_CONCRETE_POWDER;
    }
    public boolean move(int x, int y){
        if(!end && top[x][y] < 4){
            board[x][y][top[x][y]] = player;
            location.clone().add(2*x, 4, 2*y).getBlock().setType(material(player));
            if(is_win(x,y,top[x][y])){
                Component component;
                if(player == 1){
                    component = Component.text("黃色勝利").color(NamedTextColor.YELLOW);
                    firework(center, true);
                }
                else{
                    component = Component.text("紅色勝利").color(NamedTextColor.RED);
                    firework(center, false);
                }
                broadcast(component);
                end = true;
            }
            else if(is_tie()){
                broadcast("平手");
                end = true;
            }
            else {
                top[x][y]++;
                if (player == 1) player = 2;
                else player = 1;
            }
        }
        return false;
    }
    public boolean is_inside(int x, int y, int z){
        return x >= 0 && x < 4 && y >= 0 && y < 4 && z >= 0 && z < 4;
    }
    private boolean is_win(int x, int y, int z){
        for(int vx=-1; vx<=1; vx++){
            for(int vy=-1; vy<=1; vy++){
                for(int vz=0; vz<=1; vz++){
                    if(x >= 0 && y >= 0 && z == 0) continue;
                    int[][] vectors = {{vx,vy,vz},{-vx,-vy,-vz}};
                    int counter = 0;
                    for(int[] vector : vectors){
                        int check_x = x, check_y = y, check_z = z;
                        for(int j = 1; j <=3; j++){
                            check_x += vector[0];
                            check_y += vector[1];
                            check_z += vector[2];
                            if(is_inside(check_x, check_y, check_z)){
                                if(board[check_x][check_y][check_z] == player){
                                    counter++;
                                }
                                else break;
                            }
                            else break;
                        }
                        if(counter >=3)return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean is_tie(){
        for(int x=0; x<4; x++){
            for(int y=0; y<4; y++){
                for(int z=0; z<4; z++){
                    if(board[x][y][z] == 0)return false;
                }
            }
        }
        return true;
    }
    private void broadcast(Object message){
        if(message instanceof String){
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(center.distance(player.getLocation()) <= 6.5){
                    player.sendMessage((String)message);
                }
            });
        }
        else if(message instanceof Component){
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(center.distance(player.getLocation()) <= 6.5){
                    player.sendMessage((Component)message);
                }
            });
        }
    }
    public void firework(Location location, boolean isYellow){
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
        meta.setPower(2);

        firework.setFireworkMeta(meta);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        return data;
    }

    public static ScoreFour deserialize(Map<String, Object> args) {
        return new ScoreFour(
            (Location) args.get("location")
        );
    }
}
