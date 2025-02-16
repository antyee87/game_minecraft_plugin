package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.ant.game.Game;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Gomoku extends TwoColorBoardGame implements ConfigurationSerializable {
    Game gameInstance;
    static int[][] vectors = {{1,0},{0,1},{1,1},{1,-1},{-1,0},{0,-1},{-1,-1},{-1,1}};
    Location location;
    Location center;
    Location display_location;
    String display_align;
    static final int size = 15;

    int[][] board;
    int[] selected;
    int player;
    boolean end;
    Player[] minecraft_players;

    public Gomoku(Game gameInstance, Location location, Optional<Location> display_location, Optional<String> display_align) {
        super(gameInstance, location, display_location, display_align, Gomoku.size);
        this.gameInstance = gameInstance;
        this.location = location;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
        display_location.ifPresent(value -> this.display_location = value);
        display_align.ifPresent(s -> this.display_align = s);
        reset();
    }

    public void set_display(Location location, String display_align) {
        super.set_display(location, display_align);
        this.display_location = location;
        this.display_align = display_align;
    }
    public void remove_display() {
        super.remove_display();
        this.display_location = null;
        this.display_align = null;
    }

    public void reset(){
        board = new int[size][size];
        selected = null;
        if(display_selected_task != null)display_selected_task.cancel();
        player = 1;
        end = false;
        minecraft_players = new Player[2];
        display(board);
    }

    @Override
    public boolean move(int x, int y, Player minecraft_player) {
        if(!end && board[x][y] == 0){
            if(minecraft_players[player - 1] == null || minecraft_players[player - 1].equals(minecraft_player)){
                if(minecraft_players[player - 1] == null)minecraft_players[player - 1] = minecraft_player;

                if (selected == null || selected[0] != x || selected[1] != y) {
                    if (display_selected_task != null) display_selected_task.cancel();
                    if (selected != null) display_single(selected[0], selected[1], 0);
                    selected = new int[]{x, y};
                    select(x, y, player, 0);
                }
                else {
                    display_selected_task.cancel();
                    board[x][y] = player;
                    selected = null;
                    if (is_win(x, y)) {
                        Component component;
                        if (player == 1) {
                            component = Component.text("黑棋勝利").color(NamedTextColor.GRAY);
                            firework(center, true);
                        } else {
                            component = Component.text("白棋勝利").color(NamedTextColor.WHITE);
                            firework(center, false);
                        }
                        broadcast(component);
                        end = true;
                    }
                    if (is_tie()) {
                        broadcast("平手");
                        end = true;
                    }
                    display(board);
                    if (player == 1) player = 2;
                    else player = 1;
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

    private boolean is_win(int x, int y) {
        int[] counter = new int[4];
        for(int i = 0; i <8; i++){
            int check_x = x, check_y = y;
            for(int j = 1; j <=4; j++){
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
            if(counter[i%4] >=4)return true;
        }
        return false;
    }

    private boolean is_tie(){
        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                if(board[i][j] == 0)return false;
            }
        }
        return true;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        data.put("display_location", this.display_location);
        data.put("display_align", this.display_align);
        return data;
    }

    public static Gomoku deserialize(Game gameInstance, Map<String, Object> args) {
        return new Gomoku(
            gameInstance,
            (Location) args.get("location"),
            Optional.ofNullable((Location) args.get("display_location")),
            Optional.ofNullable((String)args.get("display_align"))
        );
    }
}
