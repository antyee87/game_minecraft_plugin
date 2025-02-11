package org.ant.plugin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class Chess extends BoardGame implements ConfigurationSerializable {
    Location location;
    Location center;
    final int size = 8;

    Piece[][] board;
    boolean[][] can_move = new boolean[size][size];
    boolean[][] attacked = new boolean[size][size];
    int player;
    Piece selected;
    Piece passable;
    Piece promotable;
    Piece[] king = new Piece[2];
    boolean end;

    public Chess(Location location) {
        super(location, Optional.empty(), Optional.empty(), 8);
        this.location = location;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
        reset();
    }

    public void remove() {
        super.remove();
        if(promotable != null){
            int x = promotable.x, y = promotable.y;
            for(int i=2; i<=5; i++){
                Block block = this.location.clone().add(x,i,y).getBlock();
                block.setType(Material.AIR);
            }
        }
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                location.getWorld().setType(
                    location.getBlockX()+x,
                    location.getBlockY()+1,
                    location.getBlockZ()+y,
                    Material.AIR);
            }
        }
    }

    public void reset(){
        int[][] init = new int[][]{
            {2, 3, 4, 5, 6, 4, 3, 2},
            {1, 1, 1, 1, 1, 1, 1, 1}};
        board = new Piece[size][size];
        for(int i=0;i<2;i++){
            for(int j=0;j<size;j++){
                board[i][j] = new Piece(init[i][j],i,j);
                board[7-i][j] = new Piece(init[i][j] + 10,7-i,j);
            }
        }
        king[0] = board[0][4];
        king[1] = board[7][4];
        can_move_reset();
        attacked_reset();
        if(promotable != null){
            int x = promotable.x, y = promotable.y;
            for(int i=2; i<=5; i++){
                Block block = this.location.clone().add(x,i,y).getBlock();
                block.setType(Material.AIR);
            }
        }
        display();
        player = 0;
        selected = null;
        passable = null;
        promotable = null;
        end = false;
    }

    private void can_move_reset(){
        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                can_move[i][j] = false;}
        }
    }
    private void attacked_reset(){
        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                attacked[i][j] = false;
            }
        }
    }

    public void display() {
        for(int x=0; x<size; x++){
            for(int y=0; y<size; y++){
                if((x+y) % 2 ==0){
                    this.location.clone().add(x,0,y).getBlock().setType(Material.STRIPPED_OAK_WOOD);
                    if(can_move[x][y])this.location.clone().add(x,0,y).getBlock().setType(Material.OAK_WOOD);
                }
                else{
                    this.location.clone().add(x,0,y).getBlock().setType(Material.STRIPPED_BIRCH_WOOD);
                    if(can_move[x][y])this.location.clone().add(x,0,y).getBlock().setType(Material.BIRCH_WOOD);
                }
                if(board[x][y] != null){
                    Block block = this.location.clone().add(x,1,y).getBlock();
                    if(!(block instanceof Skull))block.setType(Material.PLAYER_HEAD);
                    Skull skull = (Skull)block.getState();
                    if(skull.getPlayerProfile() == null || !skull.getPlayerProfile().getTextures().equals(board[x][y].profile().getTextures())) {
                        skull.setPlayerProfile(board[x][y].profile());
                        skull.update();
                    }
                }
                else{
                    Block block = this.location.clone().add(x,1,y).getBlock();
                    block.setType(Material.AIR);
                }
            }
        }
    }

    private void find_move(Piece selected, boolean[][] result) {
        int owner = selected.owner;
        int type = selected.type;
        int[][] rook_vector = new int[][]{{1,0},{0,1},{-1,0},{0,-1}};
        int[][] bishop_vector = new int[][]{{1,1},{1,-1},{-1,-1},{-1,1}};
        int[][] knight_vector = new int[][]{{1,2},{2,1},{1,-2},{2,-1},{-1,-2},{-2,-1},{-1,2},{-2,1}};
        int x = selected.x, y = selected.y;
        if(type == 1) {
            if(owner == 0) {
                if(result != attacked) {
                    if (board[x + 1][y] == null) {
                        result[x + 1][y] = true;
                        if (!selected.had_moved && board[x + 2][y] == null) result[x + 2][y] = true;
                    }
                    if (is_exist(x + 1, y + 1) && board[x + 1][y + 1].owner != owner) result[x + 1][y + 1] = true;
                    if (is_exist(x + 1, y - 1) && board[x + 1][y - 1].owner != owner) result[x + 1][y - 1] = true;
                    if (is_exist(x, y + 1) && board[x][y + 1].owner != owner && board[x][y + 1] == passable) result[x + 1][y + 1] = true;
                    if (is_exist(x, y - 1) && board[x][y - 1].owner != owner && board[x][y - 1] == passable) result[x + 1][y - 1] = true;
                }
                else{
                    if(is_inside(x+1,y+1))result[x + 1][y + 1] = true;
                    if(is_inside(x+1,y-1))result[x + 1][y - 1] = true;
                }
            }
            else{
                if(result != attacked){
                    if (board[x - 1][y] == null) {
                        result[x - 1][y] = true;
                        if (!selected.had_moved && board[x - 2][y] == null) result[x - 2][y] = true;
                    }
                    if (is_exist(x - 1, y + 1) && board[x - 1][y + 1].owner != owner) result[x - 1][y + 1] = true;
                    if (is_exist(x - 1, y - 1) && board[x - 1][y - 1].owner != owner) result[x - 1][y - 1] = true;
                    if (is_exist(x, y + 1) && board[x][y + 1].owner != owner && board[x][y + 1] == passable) result[x - 1][y + 1] = true;
                    if (is_exist(x, y - 1) && board[x][y - 1].owner != owner && board[x][y - 1] == passable) result[x - 1][y - 1] = true;
                }
                else{
                    if(is_inside(x-1,y+1))result[x - 1][y + 1] = true;
                    if(is_inside(x-1,y-1))result[x - 1][y - 1] = true;
                }
            }
        }
        else if(type == 2){
            for (int[] vectors : rook_vector) {
                iter_find(x, y,vectors, result);
            }
        }
        else if(type == 3){
            for (int[] vectors : knight_vector) {
                int check_x = x, check_y = y;
                check_x += vectors[0];
                check_y += vectors[1];
                if(is_inside(check_x, check_y)) {
                    if (board[check_x][check_y] == null) result[check_x][check_y] = true;
                    else if (board[check_x][check_y].owner != owner) result[check_x][check_y] = true;
                }
            }
        }
        else if(type == 4){
            for (int[] vectors : bishop_vector) {
                iter_find(x, y, vectors, result);
            }
        }
        else if(type == 5){
            for (int[] vectors : rook_vector) {
                iter_find(x, y,vectors, result);
            }
            for (int[] vectors : bishop_vector) {
                iter_find(x, y, vectors, result);
            }
        }
        else if(type == 6){
            for(int dx=-1; dx<=1; dx++){
                for(int dy=-1; dy<=1; dy++){
                    if(dx == 0 && dy == 0) continue;
                    if(is_inside(x+dx,y+dy) && (board[x+dx][y+dy] == null || board[x+dx][y+dy].owner != owner))result[x+dx][y+dy] = true;
                }
            }
            if(!board[x][y].had_moved && !attacked[x][y]){
                boolean short_castlable = true, long_castlable = true;
                for(int dy=-3; dy<=2; dy++){
                    if(dy != 0 && board[x][y + dy] != null){
                        if(dy <= 0)long_castlable = false;
                        if(dy >= 0)short_castlable = false;
                    }
                    if (dy != -3 && attacked[x][y + dy]){
                        if(dy <= 0)long_castlable = false;
                        if(dy >= 0)short_castlable = false;
                    }
                }

                if(board[x][y+3] != null && !board[x][y+3].had_moved && short_castlable)result[x][y+2] = true;
                if(board[x][y-4] != null && !board[x][y-4].had_moved && long_castlable)result[x][y-2] = true;
            }
        }
    }

    private void find_attacked(){
        attacked_reset();
        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                if(board[i][j] != null && board[i][j].owner != player ){
                    find_move(board[i][j], attacked);
                }
            }
        }
    }

    private boolean is_exist(int x, int y) {
        return (is_inside(x,y) && board[x][y] != null);
    }

    private void iter_find(int x, int y, int[] vectors, boolean[][] result){
        int player = board[x][y].owner;
        int check_x = x, check_y = y;
        check_x += vectors[0];
        check_y += vectors[1];
        while (is_inside(check_x, check_y)) {
            if (board[check_x][check_y] == null) {
                result[check_x][check_y] = true;
                check_x += vectors[0];
                check_y += vectors[1];
            }
            else if (board[check_x][check_y].owner != player) {
                result[check_x][check_y] = true;
                break;
            }
            else break;
        }
    }
    private void switch_player(){
        if (player == 0) player = 1;
        else player = 0;
        find_attacked();
        if(attacked[king[player].x][king[player].y])broadcast("check");
    }

    public boolean move(int x, int y, int layer) {
        if(!end){
            if(layer == 1 && board[x][y] != null){
                if(board[x][y].owner == player){
                    selected = board[x][y];
                    can_move_reset();
                    find_move(selected, can_move);
                    display();
                    return true;
                }
                return false;
            }
            else{
                if (selected != null && can_move[x][y]) {
                    int type = selected.type;
                    int owner = selected.owner;
                    if(type == 1){
                        if(!selected.had_moved && Math.abs(x - selected.x) == 2)passable = selected;
                        if(owner == 0){
                            if(board[x-1][y] == passable && board[x-1][y] != null && board[x-1][y].owner != player)board[x-1][y] = null;
                            if(x == 7)promotable = selected;
                        }
                        else{
                            if(board[x+1][y] == passable && board[x-1][y] != null && board[x-1][y].owner != player)board[x+1][y] = null;
                            if(x == 0)promotable = selected;
                        }
                    }
                    else if(type == 6){
                        if(!board[selected.x][selected.y].had_moved){
                            if(board[x][y+1] != null && !board[x][y+1].had_moved){
                                board[x][y-1] = board[x][y+1];
                                board[x][y+1] = null;
                            }
                            if(board[x][y-2] != null && !board[x][y-2].had_moved){
                                board[x][y+1] = board[x][y-2];
                                board[x][y-2] = null;
                            }
                        }
                    }
                    if(passable != null && passable.owner != player)passable = null;

                    if(is_exist(x,y) && board[x][y].type == 6) {
                        Component component;
                        if(board[x][y].owner == 0){
                            component = Component.text("黑棋勝利").color(NamedTextColor.GRAY);
                            firework(center, true);
                        }
                        else{
                            component = Component.text("白棋勝利").color(NamedTextColor.GRAY);
                            firework(center,false);
                        }
                        broadcast(component);
                        end = true;
                    }

                    board[x][y] = board[selected.x][selected.y];
                    board[selected.x][selected.y] = null;
                    selected.setPos(x, y);
                    selected.had_moved = true;
                    selected = null;
                    can_move_reset();
                    display();
                    if(promotable == null){
                        switch_player();
                        return true;
                    }
                    else{
                        for(int i=2; i<=5; i++){
                            Block block = this.location.clone().add(x,i,y).getBlock();
                            block.setType(Material.PLAYER_HEAD);
                            Skull skull = (Skull)block.getState();
                            skull.setPlayerProfile(Chess.profile(player * 10 + i));
                            skull.update();
                        }
                    }
                }
                return false;
            }
        }
        return false;
    }

    public void promote(int choice){
        int x = promotable.x, y = promotable.y;
        for(int i=2; i<=5; i++){
            Block block = this.location.clone().add(x,i,y).getBlock();
            block.setType(Material.AIR);
        }
        promotable.piece = player * 10 + choice;
        promotable = null;
        switch_player();
        display();
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        return data;
    }

    public static Chess deserialize(Map<String, Object> args) {
        return new Chess(
            (Location) args.get("location")
        );
    }

    public static PlayerProfile profile(int piece) {
        String textureValue="";
        switch (piece) {
            case 1:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg0OWRlNzBjYTg4NWIzZTFjNTE4NTE2MGI3NDA2MzlhZDFjNzgyMGJjMmI3N2QwYTVhYmMyZTU0NWY1ZTkwNSJ9fX0=";
                break;
            case 2:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY4MjVmYWQ4MzA4OTg5ZjlkMmE1MTA4ZTEyMjM4ZmIxMDI3MGM0ZDFmZDE3YzFiNjQzNmZlOGQyYjI0MmQxMCJ9fX0=";
                break;
            case 3:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTA0YWRiZmEzZjg1Nzg5ZGQ0Yzc3NDk5YmNhNWMyY2VjMTU3MWEwODJiM2NjMDgwMDU4NmY5YTRkMzNiNTA3OSJ9fX0=";
                break;
            case 4:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjIyNGU4MDliOWE1ODc2OTExNzZhMWIzNGQyZDUxM2ZmODE0MGY2MDZkMjViZTU3ZjA3NWU5NmM3M2EyNWIzYiJ9fX0=";
                break;
            case 5:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjE0M2Y2ODRlNjc5MjU5MGNjNGFkYThlODY3MDBhNTMxODc1ZjQ4Y2Y4OTE3M2M3ZWEwMjU0MjMxNDRmMGExNSJ9fX0=";
                break;
            case 6:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzBlNzE0MDk2YTBkZDQwNDI4OTBhOTUxMGQ4NTdhNzZhNTBjMzRlODJhNDM4YzZiZjEyOTMxNWEyOWVjODViMyJ9fX0=";
                break;
            case 11:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTg4NTUzMjk5ZmQ0ZTgzMTMyYTI2YTBlMGQyNzZiNjA0ZmVkYTVmYmEzNDg1MDFkMDc2MDI4Y2U5ODgzNDEzZSJ9fX0=";
                break;
            case 12:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTVjMjRlYTM4MjE0NzUyOWNlN2I1ZmRhZGJhZDVhMDFkMDAwMzg0NTY1NGNhOTA0NjQwM2U1NjkxNzhlZWZiMCJ9fX0=";
                break;
            case 13:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDExZjMzNmRlNTJiMjgzMzc4Zjk5MmM2NDk3NGRkNTYzZTQ1YjEzMDdjNTNmYzUzZjQxMTEyZWUyZGYyMTAzMCJ9fX0=";
                break;
            case 14:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBjOWEzNDBlMDQyM2E0MTgyOTQ1NzNkMmRkYmU1OTY4MmRkMjg5ZDhjZTQ4ZWE0Y2Y1ZTVmOWY0YzY1ZjU1YiJ9fX0=";
                break;
            case 15:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTExNzk3YjlhODIxNWRkMzRiMmZlNmQwNWJlYzFkNjM4ZDZlODdkOWVhMjZkMDMxODYwOGI5NzZkZjJmZDI1OSJ9fX0=";
                break;
            case 16:
                textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQxOWVlOThhOWJkNzRjZWQ4OGY5M2FkMWI1ZTQ1NzdhOTI5NGEwZDgwZGZlMDcyOTIxYTNkMGVjZDBkZGMwNSJ9fX0=";
                break;
        }
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", textureValue));
        return profile;
    }

    public static class Piece {
        int piece;
        int owner;
        int type;
        int x, y;
        boolean had_moved = false;
        public Piece(int piece, int x, int y) {
            this.piece = piece;
            this.owner = piece / 10;
            this.type = piece % 10;
            this.x = x;
            this.y = y;
        }
        public PlayerProfile profile(){
            return Chess.profile(this.piece);
        }
        public void setPos(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}
