package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.ant.game.BasicValue;
import org.ant.game.Game;
import org.ant.game.Method;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScoreFour implements ConfigurationSerializable, BasicValue {
    Game gameInstance;
    Location location;
    Location center;

    int[][][] board;
    int[][] top;
    int[] selected;
    BukkitTask display_selected_task;
    int player;
    boolean end;
    Player[] minecraft_players;

    public ScoreFour(Game gameInstance, Location location, int[][][] board) {
        this.gameInstance = gameInstance;
        this.location = location;
        this.center = location.clone().add(3.5, 2.5, 3.5);
        reset(board);
    }
    public void reset(int[][][] boardPreset){
        board = Objects.requireNonNullElseGet(boardPreset, () -> new int[4][4][4]);
        top = new int[4][4];
        selected = null;
        if(display_selected_task != null)display_selected_task.cancel();
        player = 1;
        minecraft_players = new Player[2];
        end = false;
        for(int x=0; x<4; x++){
            for(int y=0; y<4; y++){
                for(int z=0; z<5; z++){
                    if(z == 0)location.clone().add(2*x, z, 2*y).getBlock().setType(Material.IRON_BLOCK);
                    else location.clone().add(2*x, z, 2*y).getBlock().setType(Material.AIR);
                }
            }
        }
    }
    public void remove(){
        if(display_selected_task != null)display_selected_task.cancel();
        for(int x=0; x<4; x++){
            for(int y=0; y<4; y++){
                for(int z=0; z<5; z++){
                    location.clone().add(2*x, z, 2*y).getBlock().setType(Material.AIR);
                }
            }
        }
    }

    boolean visible = true;
    public boolean move(int x, int y, Player minecraft_player){
        if(!end && top[x][y] < 4){
            if(minecraft_players[player - 1] == null || minecraft_players[player - 1].equals(minecraft_player)) {
                if (minecraft_players[player - 1] == null) minecraft_players[player - 1] = minecraft_player;
                if (selected == null || selected[0] != x || selected[1] != y) {
                    if (display_selected_task != null) display_selected_task.cancel();
                    if (selected != null)
                        location.clone().add(selected[0] * 2, 0, selected[1] * 2).getBlock().setType(Material.IRON_BLOCK);
                    selected = new int[]{x, y};
                    visible = true;
                    display_selected_task = Bukkit.getScheduler().runTaskTimer(gameInstance, () -> {
                        Block selected_block = location.clone().add(x * 2, 0, y * 2).getBlock();
                        if (visible) selected_block.setType(Method.yellow_red_material(player));
                        else selected_block.setType(Material.IRON_BLOCK);
                        visible = !visible;
                    }, 0, 10);
                } else {
                    display_selected_task.cancel();
                    location.clone().add(x * 2, 0, y * 2).getBlock().setType(Material.IRON_BLOCK);
                    board[x][y][top[x][y]] = player;
                    selected = null;

                    location.clone().add(2 * x, 4, 2 * y).getBlock().setType(Method.yellow_red_material(player));
                    if (is_win(x, y, top[x][y])) {
                        Component component;
                        if (player == 1) {
                            component = Component.text("黃色勝利").color(NamedTextColor.YELLOW);
                            Method.yellow_red_firework(center, true);
                        } else {
                            component = Component.text("紅色勝利").color(NamedTextColor.RED);
                            Method.yellow_red_firework(center, false);
                        }
                        Method.broadcast(component, center, 7);
                        end = true;
                    } else if (is_tie()) {
                        Method.broadcast("平手", center, 7);
                        end = true;
                    } else {
                        top[x][y]++;
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
    public boolean is_inside(int x, int y, int z){
        return x >= 0 && x < 4 && y >= 0 && y < 4 && z >= 0 && z < 4;
    }
    private boolean is_win(int x, int y, int z){
        for(int vx=-1; vx<=1; vx++){
            for(int vy=-1; vy<=1; vy++){
                for(int vz=0; vz<=1; vz++){
                    if(vx >= 0 && vy >= 0 && vz == 0) continue;
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

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Location getCenter() {
        return center;
    }

    @Override
    public int getSize() {
        return 7;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        if (!end) {
            data.put("board", this.board);
        }
        return data;
    }

    public static ScoreFour deserialize(Game gameInstance, Map<String, Object> args) {
        return new ScoreFour(
            gameInstance,
            (Location) args.get("location"),
            (int[][][]) args.get("board")
        );
    }
}
