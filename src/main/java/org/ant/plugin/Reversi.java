package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Reversi extends TwoColorBoardGame implements ConfigurationSerializable {
    Location location;
    Location center;
    Location display_location;
    String display_align;

    final int size = 8;
    int[][] vector = {{1,0},{0,1},{1,1},{1,-1},{-1,0},{0,-1},{-1,-1},{-1,1}};

    int[][] board;
    boolean[][][] can_flip = new boolean[8][size][size];
    int player;
    boolean end;
    boolean moveable;

    public Reversi(Location location, Optional<Location> display_location, Optional<String> display_align) {
        super(location, display_location, display_align, 8);
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
        board[3][3] = 1;
        board[4][4] = 1;
        board[3][4] = 2;
        board[4][3] = 2;
        player = 1;
        end = false;
        find_can_move();
        display(board);
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
                        check_x += vector[i][0];
                        check_y += vector[i][1];
                        while(is_inside(check_x, check_y)){
                            if(board[check_x][check_y] != player && Method.isInRange(board[check_x][check_y],1,2)) has_opponent = true;
                            else break;
                            check_x += vector[i][0];
                            check_y += vector[i][1];
                        }
                        if(has_opponent && is_inside(check_x, check_y) && !Method.isInRange(board[check_x][check_y],1,2)){
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
    public boolean move(int x, int y) {
        if(!end){
            if(board[x][y] == 3){
                board[x][y] = player;
                for(int i=0; i<8; i++){
                    int check_x = x, check_y = y;
                    check_x += vector[i][0];
                    check_y += vector[i][1];
                    while(can_flip[(i+4)%8][x][y] && is_inside(check_x, check_y)){
                        if(board[check_x][check_y] != player)board[check_x][check_y] = player;
                        else break;
                        check_x += vector[i][0];
                        check_y += vector[i][1];
                    }
                }
                for(int i=0; i<2;i++){
                    if(player == 1)player = 2;
                    else player = 1;
                    find_can_move();
                    if(moveable)break;
                }
                display(board);

                if(!moveable){
                    int black_piece = 0;
                    int white_piece = 0;
                    for(int i=0; i<size; i++){
                        for(int j=0; j<size; j++){
                            if(board[i][j] == 1)black_piece++;
                            else if(board[i][j] == 2)white_piece++;
                        }
                    }
                    Component component;
                    if(black_piece > white_piece){
                        component = Component.text("黑棋勝利").color(NamedTextColor.GRAY);
                        firework(center,true);
                    }
                    else if(white_piece > black_piece){
                        component = Component.text("白棋勝利").color(NamedTextColor.WHITE);
                        firework(center,false);
                    }
                    else{
                        component = Component.text("平手");
                    }
                    broadcast(component);
                    component = Component.text(black_piece).color(NamedTextColor.GRAY)
                        .append(Component.text(" : ").color(NamedTextColor.GREEN))
                        .append(Component.text(white_piece).color(NamedTextColor.WHITE));
                    broadcast(component);
                    end = true;
                }
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        data.put("display_location", this.display_location);
        data.put("display_align", this.display_align);
        return data;
    }

    public static Reversi deserialize(Map<String, Object> args) {
        return new Reversi(
            (Location) args.get("location"),
            Optional.ofNullable((Location) args.get("display_location")),
            Optional.ofNullable((String)args.get("display_align"))
        );
    }
}
