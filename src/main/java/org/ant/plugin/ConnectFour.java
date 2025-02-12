package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class ConnectFour implements ConfigurationSerializable {
    Location location;
    Location center;
    String align;
    int[][] board;
    int[] top;
    public int player;
    boolean end;
    public ConnectFour(Location location, String align) {
        this.location = location;
        this.align = align;
        if(align.equals("x"))this.center = location.clone().add(3.5, 0, 0);
        else if(align.equals("z"))this.center = location.clone().add(0, 0, 3.5);
        reset();
    }
    public void reset(){
        for(int x=0; x<6; x++){
            for(int y=0; y<7; y++){
                if(align.equals("x")){
                    location.clone().add(y,x,0).getBlock().setType(Material.AIR);
                }
                else if(align.equals("z")){
                    location.clone().add(0,x,y).getBlock().setType(Material.AIR);
                }
            }
        }
        board = new int[6][7];
        top = new int[7];
        player = 1;
        end = false;
    }

    private Material material(int player) {
        if(player == 1)return Material.YELLOW_CONCRETE_POWDER;
        else return Material.RED_CONCRETE_POWDER;
    }
    public void move(int y){
        if(!end && top[y] < 6){
            board[top[y]][y] = player;
            if(align.equals("x"))location.clone().add(y,5,0).getBlock().setType(material(player));
            else if(align.equals("z"))location.clone().add(0,5,y).getBlock().setType(material(player));
            if(is_win(top[y],y)){
                Component component;
                if(player == 1)component = Component.text("黃色勝利").color(NamedTextColor.YELLOW);
                else component = Component.text("紅色勝利").color(NamedTextColor.RED);
                broadcast(component);
                end = true;
            }
            else if(is_tie()){
                broadcast("平手");
                end = true;
            }
            else {
                top[y]++;
                if (player == 1) player = 2;
                else player = 1;
            }
        }
    }
    private boolean is_inside(int x, int y){
        return x >= 0 && x < 6 && y >= 0 && y < 7;
    }
    private boolean is_win(int x, int y){
        int[][] vectors = {{1,0},{0,1},{1,1},{1,-1},{-1,0},{0,-1},{-1,-1},{-1,1}};
        int[] counter = new int[4];
        for(int i = 0; i <8; i++){
            int check_x = x, check_y = y;
            for(int j = 1; j <=3; j++){
                check_x += vectors[i][0];
                check_y += vectors[i][1];
                if(is_inside(check_x, check_y)){
                    if(board[check_x][check_y] == player){
                        counter[i%4]++;
                    }
                    else break;
                }
                else break;
            }
            if(counter[i%4] >=3)return true;
        }
        return false;
    }
    private boolean is_tie(){
        for(int x=0; x<6; x++){
            for(int y=0; y<7; y++){
                if(board[x][y] == 0)return false;
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

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        data.put("align", this.align);
        return data;
    }

    public static ConnectFour deserialize(Map<String, Object> args) {
        return new ConnectFour(
            (Location) args.get("location"),
            (String) args.get("align")
        );
    }
}
