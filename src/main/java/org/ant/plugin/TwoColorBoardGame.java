package org.ant.plugin;

import org.bukkit.*;

import java.util.Optional;

public class TwoColorBoardGame extends  BoardGame {
    Location location;
    Location center;
    Location display_location;
    String display_align;
    int size;
    public TwoColorBoardGame(Location location, Optional<Location> display_location, Optional<String> display_align, int size) {
        super(location, display_location, display_align, size);
        this.location = location;
        display_location.ifPresent(value -> this.display_location = value);
        display_align.ifPresent(s -> this.display_align = s);
        this.size = size;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
    }

    public void set_display(Location location, String display_align) {
        super.set_display(location, display_align);
        this.display_location = location;
        this.display_align = display_align;
    }
    public void remove_display() {
        super.remove_display();
        display_location = null;
        display_align = null;
    }

    private Material material(int status){
        switch (status) {
            case 0:
                return Material.GLASS;
            case 1:
                return Material.BLACK_STAINED_GLASS;
            case 2:
                return Material.WHITE_STAINED_GLASS;
            case 3:
                return Material.YELLOW_STAINED_GLASS;
        }
        return Material.AIR;
    }

    public void display(int[][] board){
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                location.getWorld().setType(
                    location.getBlockX()+x,
                    location.getBlockY(),
                    location.getBlockZ()+y,
                    material(board[x][y]));
                if(display_location != null){
                    if(display_align.equals("x")){
                        display_location.getWorld().setType(
                            display_location.getBlockX()+x,
                            display_location.getBlockY()+y,
                            display_location.getBlockZ(),
                            material(board[x][y]));
                    }
                    else if(display_align.equals("z")){
                        display_location.getWorld().setType(
                            display_location.getBlockX(),
                            display_location.getBlockY()+x,
                            display_location.getBlockZ()+y,
                            material(board[x][y]));
                    }
                }
            }
        }
    }
}

