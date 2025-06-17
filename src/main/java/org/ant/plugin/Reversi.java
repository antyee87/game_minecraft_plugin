package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.ant.game.Game;
import org.ant.game.Method;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Reversi extends TwoColorBoardGame implements ConfigurationSerializable {
    Game gameInstance;
    static int[][] vectors = {{1,0},{0,1},{1,1},{1,-1},{-1,0},{0,-1},{-1,-1},{-1,1}};
    Location location;
    Location center;
    Location display_location;
    String display_align;
    static final int size = 8;

    int[][] board;
    boolean[][][] can_flip = new boolean[8][size][size];
    int[] selected;
    int player;
    boolean end;
    boolean moveable;
    Player[] minecraft_players;

    public Reversi(Game gameInstance, Location location, int[][] board, Location display_location, String display_align) {
        super(gameInstance, location, display_location, display_align, Reversi.size);
        this.gameInstance = gameInstance;
        this.location = location;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
        this.display_location = display_location;
        this.display_align = display_align;
        reset(board);
    }

    public void setDisplay(Location location, String display_align) {
        super.setDisplay(location, display_align);
        this.display_location = location;
        this.display_align = display_align;
    }
    public void removeDisplay() {
        super.removeDisplay();
        this.display_location = null;
        this.display_align = null;
    }

    public void reset(int[][] boardPreset) {
        board = Objects.requireNonNullElseGet(boardPreset, () -> new int[size][size]);
        board[3][3] = 1;
        board[4][4] = 1;
        board[3][4] = 2;
        board[4][3] = 2;
        selected = null;
        if(display_selected_task != null)display_selected_task.cancel();
        player = 1;
        end = false;
        find_can_move();
        minecraft_players = new Player[2];
        display(board);
    }

    public void remove(){
        if(display_selected_task != null)display_selected_task.cancel();
        super.remove();
    }

    private void find_can_move(){
        moveable = false;
        for(int x=0; x<size; x++){
            for(int y=0; y<size; y++){
                if(board[x][y] == 3)board[x][y] = 0;
                for(int i=0; i<8; i++)can_flip[i][x][y] = false;
            }
        }
        for(int x=0; x<size; x++){
            for(int y=0; y<size; y++){
                if(board[x][y] == player){
                    for(int i=0; i<8; i++){
                        int check_x = x, check_y = y;
                        boolean has_opponent = false;
                        check_x += vectors[i][0];
                        check_y += vectors[i][1];
                        while(isInside(check_x, check_y)){
                            if(board[check_x][check_y] != player && Method.isInRange(board[check_x][check_y],1,2)) has_opponent = true;
                            else break;
                            check_x += vectors[i][0];
                            check_y += vectors[i][1];
                        }
                        if(has_opponent && isInside(check_x, check_y) && !Method.isInRange(board[check_x][check_y],1,2)){
                            board[check_x][check_y] = 3;
                            can_flip[i][check_x][check_y] = true;
                            moveable = true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean move(int x, int y, Player minecraft_player) {
        if(!end){
            if(board[x][y] == 3){
                if(minecraft_players[player - 1] == null || minecraft_players[player - 1].equals(minecraft_player)) {
                    if (minecraft_players[player - 1] == null) minecraft_players[player - 1] = minecraft_player;
                    if (selected == null || selected[0] != x || selected[1] != y) {
                        if (display_selected_task != null) display_selected_task.cancel();
                        if (selected != null) display_single(selected[0], selected[1], 3);
                        selected = new int[]{x, y};
                        select(x, y, player, 3);
                    } else {
                        display_selected_task.cancel();
                        board[x][y] = player;
                        selected = null;
                        for (int i = 0; i < 8; i++) {
                            int check_x = x, check_y = y;
                            check_x += vectors[i][0];
                            check_y += vectors[i][1];
                            while (can_flip[(i + 4) % 8][x][y] && isInside(check_x, check_y)) {
                                if (board[check_x][check_y] != player) board[check_x][check_y] = player;
                                else break;
                                check_x += vectors[i][0];
                                check_y += vectors[i][1];
                            }
                        }
                        for (int i = 0; i < 2; i++) {
                            if (player == 1) player = 2;
                            else player = 1;
                            find_can_move();
                            if (moveable) break;
                        }
                        display(board);

                        if (!moveable) {
                            int black_piece = 0;
                            int white_piece = 0;
                            for (int i = 0; i < size; i++) {
                                for (int j = 0; j < size; j++) {
                                    if (board[i][j] == 1) black_piece++;
                                    else if (board[i][j] == 2) white_piece++;
                                }
                            }
                            Component component;
                            if (black_piece > white_piece) {
                                component = Component.text("黑棋勝利").color(NamedTextColor.GRAY);
                                firework(center, true);
                            } else if (white_piece > black_piece) {
                                component = Component.text("白棋勝利").color(NamedTextColor.WHITE);
                                firework(center, false);
                            } else {
                                component = Component.text("平手");
                            }
                            broadcast(component);
                            component = Component.text(black_piece).color(NamedTextColor.GRAY)
                                .append(Component.text(" : ").color(NamedTextColor.GREEN))
                                .append(Component.text(white_piece).color(NamedTextColor.WHITE));
                            broadcast(component);
                            end = true;
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
        }
        return false;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        if (!end) {
            data.put("board", this.board);
        }
        data.put("display_location", this.display_location);
        data.put("display_align", this.display_align);
        return data;
    }

    public static Reversi deserialize(Game gameInstance, Map<String, Object> args) {
        return new Reversi(
            gameInstance,
            (Location) args.get("location"),
            (int[][]) args.get("board"),
            (Location) args.get("display_location"),
            (String) args.get("display_align")
        );
    }
}
