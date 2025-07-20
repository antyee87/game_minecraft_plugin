package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.ant.game.Game;
import org.ant.game.TwoColorBoardGame;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Gomoku extends TwoColorBoardGame implements ConfigurationSerializable {
    static int[][] vectors = {{1,0},{0,1},{1,1},{1,-1},{-1,0},{0,-1},{-1,-1},{-1,1}};
    static final int size = 15;

    int[][] board;
    int[] selected;
    int player;
    boolean end;
    Player[] minecraft_players;

    public Gomoku(Game gameInstance, Location location, int[][] board, Location displayLocation, String displayAlign) {
        super(gameInstance, location, displayLocation, displayAlign, Gomoku.size);
        this.gameInstance = gameInstance;
        this.location = location;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
        this.displayLocation = displayLocation;
        this.displayAlign = displayAlign;
        reset(board);
    }

    public void setDisplay(Location location, String displayAlign) {
        super.setDisplay(location, displayAlign);
        this.displayLocation = location;
        this.displayAlign = displayAlign;
    }
    public void removeDisplay() {
        super.removeDisplay();
        this.displayLocation = null;
        this.displayAlign = null;
    }
    public void remove(){
        if(displaySelectedTask != null) displaySelectedTask.cancel();
        super.remove();
    }

    public void reset(int[][] boardPreset) {
        this.board = Objects.requireNonNullElseGet(boardPreset, () -> new int[size][size]);
        selected = null;
        if(displaySelectedTask != null) displaySelectedTask.cancel();
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
                    if (displaySelectedTask != null) displaySelectedTask.cancel();
                    if (selected != null) displaySingle(selected[0], selected[1], 0);
                    selected = new int[]{x, y};
                    select(x, y, player, 0);
                }
                else {
                    displaySelectedTask.cancel();
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
                if(isInside(check_x, check_y)){
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
        if (!end) {
            data.put("board", this.board);
        }
        data.put("display_location", this.displayLocation);
        data.put("display_align", this.displayAlign);
        return data;
    }

    public static Gomoku deserialize(Game gameInstance, Map<String, Object> args) {
        return new Gomoku(
            gameInstance,
            (Location) args.get("location"),
            ((List<List<Integer>>) args.get("board")).stream()
                .map(y -> y.stream().mapToInt(Integer::intValue).toArray())
                .toArray(int[][]::new),
            (Location) args.get("display_location"),
            (String)args.get("display_align")
        );
    }
}
