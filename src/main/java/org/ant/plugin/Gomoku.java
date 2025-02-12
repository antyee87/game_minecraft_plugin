package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Gomoku extends TwoColorBoardGame implements ConfigurationSerializable {
    static int[][] vectors = {{1,0},{0,1},{1,1},{1,-1},{-1,0},{0,-1},{-1,-1},{-1,1}};
    Location location;
    Location center;
    Location display_location;
    String display_align;

    static final int size = 15;

    int[][] board;
    public int player;
    boolean end;

    public Gomoku(Location location, Optional<Location> display_location, Optional<String> display_align) {
        super(location, display_location, display_align, 15);
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
        player = 1;
        display(board);
        end = false;
    }

    @Override
    public boolean move(int x, int y) {
        if(!end && board[x][y] == 0){
            board[x][y] = player;
            if(is_win(x,y)){
                Component component;
                if (player == 1) {
                    component = Component.text("黑棋勝利").color(NamedTextColor.GRAY);
                    firework(center,true);
                }
                else{
                    component = Component.text("白棋勝利").color(NamedTextColor.WHITE);
                    firework(center,false);
                }
                broadcast(component);
                end = true;
            }
            if(is_tie()){
                broadcast("平手");
                end = true;
            }
            display(board);
            if(player == 1)player = 2;
            else player = 1;
            return true;
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

    public static Gomoku deserialize(Map<String, Object> args) {
        return new Gomoku(
            (Location) args.get("location"),
            Optional.ofNullable((Location) args.get("display_location")),
            Optional.ofNullable((String)args.get("display_align"))
        );
    }
}
