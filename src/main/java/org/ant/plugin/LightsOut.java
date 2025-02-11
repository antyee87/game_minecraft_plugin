package org.ant.plugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.CopperBulb;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LightsOut extends BoardGame implements ConfigurationSerializable{
    static int[][] vectors = new int[][]{{0,0},{1,0},{0,1},{-1,0},{0,-1}};
    Location location;
    Location center;
    Location display_location;
    String display_align;

    final int size;

    boolean[][] board;

    public LightsOut(Location location, int size, Optional<Location> display_location, Optional<String> display_align) {
        super(location, display_location, display_align, size);
        this.location = location;
        this.size = size;
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
        board = new boolean[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int random = (int) (Math.round(Math.random()));
                if(random == 0) move(x, y);
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
                if(display_location != null){
                    if(display_align.equals("x")){
                        block = display_location.clone().add(x,y,0).getBlock();
                        block.setType(Material.WAXED_COPPER_BULB);
                        lightable = (CopperBulb) block.getBlockData();
                        lightable.setLit(board[x][y]);
                        block.setBlockData(lightable);
                    }
                    else if(display_align.equals("z")){
                        block = display_location.clone().add(0,x,y).getBlock();
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
    public boolean move(int x, int y) {
        for(int[] vector : vectors){
            if(is_inside(x + vector[0], y + vector[1])){
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
        data.put("display_location", this.display_location);
        data.put("display_align", this.display_align);
        return data;
    }

    public static LightsOut deserialize(Map<String, Object> args) {
        return new LightsOut(
            (Location) args.get("location"),
            (Integer) args.get("size"),
            Optional.ofNullable((Location) args.get("display_location")),
            Optional.ofNullable((String)args.get("display_align"))
        );
    }
}
