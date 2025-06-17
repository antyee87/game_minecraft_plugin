package org.ant.plugin;

import org.ant.game.BoardGame;
import org.ant.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

public class TwoColorBoardGame extends BoardGame {
    Game gameInstance;
    Location location;
    Location center;
    Location display_location;
    String display_align;
    int size;

    int[] selected_point;
    BukkitTask display_selected_task;

    public TwoColorBoardGame(Game gameInstance, Location location, Location display_location, String display_align, int size) {
        super(location, display_location, display_align, size);
        this.gameInstance = gameInstance;
        this.location = location;
        this.display_location = display_location;
        this.display_align = display_align;
        this.size = size;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
    }

    public void setDisplay(Location location, String display_align) {
        super.setDisplay(location, display_align);
        this.display_location = location;
        this.display_align = display_align;
    }
    public void removeDisplay() {
        super.removeDisplay();
        display_location = null;
        display_align = null;
    }

    private Material material(int status){
        return switch (status) {
            case 0 -> Material.GLASS;
            case 1 -> Material.BLACK_STAINED_GLASS;
            case 2 -> Material.WHITE_STAINED_GLASS;
            case 3 -> Material.YELLOW_STAINED_GLASS;
            default -> Material.AIR;
        };
    }

    boolean visible ;
    public void select(int x, int y, int player, int init){
        selected_point = new int[]{x, y};
        visible = true;
        display_selected_task = Bukkit.getScheduler().runTaskTimer(gameInstance, () -> {
            if(visible)display_single(x, y, player);
            else display_single(x, y, init);
            visible = !visible;
        }, 0, 10);
    }

    public void display(int[][] board){
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                display_single(x, y, board[x][y]);
            }
        }
    }
    public void display_single(int x, int y, int player){
        location.getWorld().setType(
            location.getBlockX()+x,
            location.getBlockY(),
            location.getBlockZ()+y,
            material(player));
        if(display_location != null){
            if(display_align.equals("x")){
                display_location.getWorld().setType(
                    display_location.getBlockX()+x,
                    display_location.getBlockY()+y,
                    display_location.getBlockZ(),
                    material(player));
            }
            else if(display_align.equals("z")){
                display_location.getWorld().setType(
                    display_location.getBlockX(),
                    display_location.getBlockY()+x,
                    display_location.getBlockZ()+y,
                    material(player));
            }
        }
    }
}

