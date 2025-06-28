package org.ant.plugin;

import org.ant.game.BoardGame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.CopperBulb;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LightsOut extends BoardGame implements ConfigurationSerializable{
    static int[][] vectors = new int[][]{{0,0},{1,0},{0,1},{-1,0},{0,-1}};

    final int size;

    boolean[][] board;

    public LightsOut(Location location, int size, boolean[][] board, Location displayLocation, String displayAlign) {
        super(location, displayLocation, displayAlign, size);
        this.location = location;
        this.size = size;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
        this.displayLocation = displayLocation;
        this.displayAlign = displayAlign;
        reset(board);
    }

    public void setDisplay(Location location, String display_align) {
        super.setDisplay(location, display_align);
        this.displayLocation = location;
        this.displayAlign = display_align;
    }
    public void removeDisplay() {
        super.removeDisplay();
        this.displayLocation = null;
        this.displayAlign = null;
    }

    public void reset(boolean[][] boardPreset){
        board = Objects.requireNonNullElseGet(boardPreset, () -> new boolean[size][size]);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int random = (int) (Math.round(Math.random()));
                if(random == 0) move(x, y, null);
            }
        }
        display();
    }

    private void display(){
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                Block block = location.clone().add(x,0,y).getBlock();
                block.setType(Material.WAXED_COPPER_BULB);
                Lightable lightable = (CopperBulb) block.getBlockData();
                lightable.setLit(board[x][y]);
                block.setBlockData(lightable);
                if(displayLocation != null){
                    if(displayAlign.equals("x")){
                        block = displayLocation.clone().add(x,y,0).getBlock();
                        block.setType(Material.WAXED_COPPER_BULB);
                        lightable = (CopperBulb) block.getBlockData();
                        lightable.setLit(board[x][y]);
                        block.setBlockData(lightable);
                    }
                    else if(displayAlign.equals("z")){
                        block = displayLocation.clone().add(0,x,y).getBlock();
                        block.setType(Material.WAXED_COPPER_BULB);
                        lightable = (CopperBulb) block.getBlockData();
                        lightable.setLit(board[x][y]);
                        block.setBlockData(lightable);
                    }
                }
            }
        }
    }

    @Override
    public boolean move(int x, int y, Player minecraft_player) {
        for(int[] vector : vectors){
            if(isInside(x + vector[0], y + vector[1])){
                board[x+vector[0]][y+vector[1]] = !board[x+vector[0]][y+vector[1]];
            }
        }
        display();
        return true;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        data.put("size", this.size);
        data.put("board", this.board);
        data.put("display_location", this.displayLocation);
        data.put("display_align", this.displayAlign);
        return data;
    }

    public static LightsOut deserialize(Map<String, Object> args) {
        return new LightsOut(
            (Location) args.get("location"),
            (Integer) args.get("size"),
            (boolean[][]) args.get("board"),
            (Location) args.get("display_location"),
            (String) args.get("display_align")
        );
    }
}
