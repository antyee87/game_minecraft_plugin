package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class ConnectFour implements ConfigurationSerializable {
    Location location;
    Location center;
    String align;

    int[][] board;
    int[] top;
    int selected = -1;
    BukkitTask display_selected_task;
    int player;
    boolean end;
    Player[] minecraft_players;

    public ConnectFour(Location location, String align) {
        this.location = location;
        this.align = align;
        if(align.equals("x"))this.center = location.clone().add(3.5, 3, 0);
        else if(align.equals("z"))this.center = location.clone().add(0, 3, 3.5);
        reset();
    }
    public void reset(){
        board = new int[6][7];
        top = new int[7];
        selected = -1;
        if(display_selected_task != null)display_selected_task.cancel();
        player = 1;
        minecraft_players = new Player[2];
        end = false;
        for(int x=0; x<7; x++){
            for(int y=0; y<7; y++){
                int status = 0;
                if(x == 0)status = -1;
                if(align.equals("x"))location.clone().add(y,x,0).getBlock().setType(Method.yellow_red_material(status));
                else if(align.equals("z"))location.clone().add(0,x,y).getBlock().setType(Method.yellow_red_material(status));
            }
        }
    }

    

    boolean visible = true;
    public boolean move(int y, Player minecraft_player) {
        if(!end && top[y] < 6){
            if(minecraft_players[player - 1] == null || minecraft_players[player - 1].equals(minecraft_player)) {
                if (minecraft_players[player - 1] == null) minecraft_players[player - 1] = minecraft_player;
                if (selected == -1 || selected != y) {
                    if (display_selected_task != null) display_selected_task.cancel();
                    if (selected != -1) {
                        Block block = null;
                        if (align.equals("x")) block = location.clone().add(selected, 0, 0).getBlock();
                        else if (align.equals("z")) block = location.clone().add(0, 0, selected).getBlock();
                        block.setType(Method.yellow_red_material(-1));
                    }
                    selected = y;
                    visible = true;
                    display_selected_task = Bukkit.getScheduler().runTaskTimer(Game.getInstance(), () -> {
                        Block selected_block = null;
                        if (align.equals("x")) selected_block = location.clone().add(y, 0, 0).getBlock();
                        else if (align.equals("z")) selected_block = location.clone().add(0, 0, y).getBlock();

                        if (visible) selected_block.setType(Method.yellow_red_material(player));
                        else selected_block.setType(Method.yellow_red_material(-1));
                        visible = !visible;
                    }, 0, 10);
                } else {
                    display_selected_task.cancel();
                    Block block = null;
                    if (align.equals("x")) block = location.clone().add(y, 0, 0).getBlock();
                    else if (align.equals("z")) block = location.clone().add(0, 0, y).getBlock();
                    block.setType(Method.yellow_red_material(-1));
                    selected = -1;

                    board[top[y]][y] = player;
                    if (align.equals("x"))
                        location.clone().add(y, 6, 0).getBlock().setType(Method.yellow_red_material(player));
                    else if (align.equals("z"))
                        location.clone().add(0, 6, y).getBlock().setType(Method.yellow_red_material(player));
                    if (is_win(top[y], y)) {
                        Component component;
                        if (player == 1) {
                            component = Component.text("黃色勝利").color(NamedTextColor.YELLOW);
                            Method.yellow_red_firework(center.clone().add(0, 3, 0), true);
                        } else {
                            component = Component.text("紅色勝利").color(NamedTextColor.RED);
                            Method.yellow_red_firework(center.clone().add(0, 3, 0), false);
                        }
                        Method.broadcast(component, center, 7);
                        end = true;
                    } else if (is_tie()) {
                        Method.broadcast("平手", center, 7);
                        end = true;
                    } else {
                        top[y]++;
                        if (player == 1) player = 2;
                        else player = 1;
                    }
                }
                return true;
            }
            else{
                Component component;
                component = Component.text("已被玩家 " + minecraft_players[player - 1].getName() + " 綁定").color(NamedTextColor.RED);
                minecraft_player.sendMessage(component);
                minecraft_player.playSound(minecraft_player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }
        }
        return false;
    }

    public boolean is_inside(int x, int y){
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
