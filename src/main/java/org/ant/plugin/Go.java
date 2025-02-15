package org.ant.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

/*
Tromp-Taylor 規則
沒有定義死棋，在棋盤上的棋子都視為某方的領地
如果某區域被黑方包圍且沒有白棋，則視為黑棋的領地
如果某區域同時接觸黑棋和白棋，則不視為某方領地
禁止出現和過去相同的盤面（禁全同），這個特性定義了劫
棋子可以自殺
*/
public class Go extends TwoColorBoardGame implements ConfigurationSerializable {
    //棋盤基本資料
    Location location;
    Location center;
    Location display_location;
    String display_align;
    static final int size = 19;
    //計算用資料
    final static int[][] adjacents = {{1,0},{0,1},{-1,0},{0,-1}};

    //遊戲基本資料
    Piece[][] game_board;
    int[][][] history_board;
    boolean[][] moveable = new boolean[size][size];
    int player;
    int[] selected;
    int[] captured;
    boolean end;
    Player[] minecraft_players;

    public Go(Location location, Optional<Location> display_location, Optional<String> display_align){
        super(location, display_location, display_align, Go.size);
        this.location = location;
        this.center = location.clone();
        this.center.add((double) size /2, 0, (double) size /2);
        display_location.ifPresent(value -> this.display_location = value);
        display_align.ifPresent(s -> this.display_align = s);
        reset();
    }

    public void reset(){
        if(display_selected_task != null)display_selected_task.cancel();
        game_board = new Piece[size][size];
        history_board = new int[2][size][size];
        reset_moveable();
        player = 1;
        selected = null;
        captured = new int[2];
        end = false;
        minecraft_players = new Player[2];
        display(history_board[player]);
    }
    private void reset_moveable(){
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                moveable[x][y] = true;
            }
        }
    }

    public boolean move(int x, int y, Player minecraft_player){
        if(!end && game_board[x][y] == null){
            if(minecraft_players[player - 1] == null || minecraft_players[player - 1].equals(minecraft_player)) {
                if (minecraft_players[player - 1] == null) minecraft_players[player - 1] = minecraft_player;
                if (selected == null || selected[0] != x || selected[1] != y) {
                    if (!moveable[x][y] || !is_legal(x, y)) {
                        moveable[x][y] = false;
                        return false;
                    } else {
                        if (display_selected_task != null) display_selected_task.cancel();
                        if (selected != null) display_single(selected[0], selected[1], 0);
                        selected = new int[]{x, y};
                        select(x, y, player, 0);
                    }
                } else {
                    reset_moveable();
                    display_selected_task.cancel();
                    game_board[x][y] = new Piece(x, y, player);
                    selected = null;
                    set_piece_string(x, y, game_board);
                    move_calculation(x, y, game_board);
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            if (game_board[i][j] != null) history_board[player - 1][i][j] = game_board[i][j].owner;
                            else history_board[player - 1][i][j] = 0;
                        }
                    }
                    display(history_board[player - 1]);
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
    public void skip(){
        if (player == 1)player = 2;
        else player = 1;
    }

    private void calculate_territory(){
        Map<Integer, Piece> piece_string_group_map = new HashMap<>();
        int group = 1;
        for(int x=0; x<size; x++){
            for(int y=0; y<size; y++){
                if(game_board[x][y] != null && game_board[x][y].group == 0){
                    Piece cur = game_board[x][y];
                    piece_string_group_map.put(group, cur);
                    boolean flag = true;
                    while(cur != game_board[x][y] || flag){
                        cur.group = group;
                        flag = false;
                        cur = cur.next;
                    }
                }
                group++;
            }
        }
        HashMap<Integer, Set<Integer>> area_borders = new HashMap<>();
        int area = 0;
        for(int x=0; x<size; x++){
            for(int y=0; y<size; y++){
                if(game_board[x][y] == null){
                    Set<Integer> border_group_set = new HashSet<>();
                    boolean[][] has_visited = new boolean[size][size];
                    flood_fill(x, y, border_group_set, has_visited);
                    boolean[] has_border = new boolean[3];
                    boolean absolute = true;
                    for(int border_group:border_group_set){
                        has_border[piece_string_group_map.get(border_group).group] = true;
                        if(has_border[1] && has_border[2]){
                            absolute = false;
                            break;
                        }
                    }
                    if(absolute){
                        for(int border_group:border_group_set) {
                            piece_string_group_map.get(border_group).vital_area ++;
                        }
                    }
                    else{
                        area_borders.put(area, border_group_set);
                        area++;
                    }
                }
            }
        }
        for(Set<Integer> area_border:area_borders.values()){
            for(int piece_string_group:area_border){
                Piece piece = piece_string_group_map.get(piece_string_group);
                piece.liberty = count_liberties(piece.x, piece.y, game_board);
            }

            Piece min_liberty_piece = null;
            int min = Integer.MAX_VALUE;
            for(int piece_string_group:area_border) {
                Piece piece = piece_string_group_map.get(piece_string_group);
                if(piece.liberty < min)min_liberty_piece = piece;
                else if(piece.liberty == min)min_liberty_piece = null;
            }
            if(min_liberty_piece != null)capture_piece(min_liberty_piece.x, min_liberty_piece.y, game_board);
        }

        /*
        沒眼 : 跟其他邊界比氣 從氣少的開始提吃 直到邊界只剩下一色
        有一隻眼
        有兩隻以上眼
         */
        Map<Integer, Boolean> alive = new HashMap<>();
        for(Piece piece_string:piece_string_group_map.values()) {
            if(piece_string.vital_area >= 2)alive.put(piece_string.group, true);
        }
    }
    private int flood_fill(int x, int y, Set<Integer> border_group_set, boolean[][] has_visited){
        int count = 0;
        if(!has_visited[x][y]){
            has_visited[x][y] = true;
            count++;
            for(int[] adjacent:adjacents){
                if(is_valid_liberty(x + adjacent[0], y + adjacent[1], game_board)) {
                    count+=flood_fill(x + adjacent[0], y + adjacent[1], border_group_set, has_visited);
                }
                else if(is_valid_piece(x + adjacent[0], y + adjacent[1], game_board)){
                    border_group_set.add(game_board[x + adjacent[0]][y + adjacent[1]].group);
                }
            }
        }
        return count;
    }

    private boolean is_legal(int x, int y){
        Piece[][] calculation_board = copy_board();
        calculation_board[x][y] = new Piece(x, y, player);
        set_piece_string(x, y, calculation_board);
        move_calculation(x, y, calculation_board);
        int liberties = count_liberties(x, y, calculation_board);
        if(liberties == 0) return false;
        boolean repeat = true;
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(calculation_board[i][j] != null){
                    if(calculation_board[i][j].owner != history_board[player - 1][i][j]){
                        repeat = false;
                        break;
                    }
                }
                else if(history_board[player - 1][i][j] != 0){
                    repeat = false;
                    break;
                }
            }
            if(!repeat) break;
        }
        return !repeat;
    }

    private void move_calculation(int x, int y, Piece[][] board){
        for(int[] adjacent : adjacents){
            if(is_valid_piece(x + adjacent[0], y + adjacent[1], board)){
                if(board[x + adjacent[0]][y + adjacent[1]].owner != player){
                    if(count_liberties(x + adjacent[0], y + adjacent[1], board) == 0){
                        capture_piece(x + adjacent[0], y + adjacent[1], board);
                    }
                }
            }
        }
    }

    private int count_liberties(int x, int y, Piece[][] board){
        int liberties = 0;
        Piece cur = board[x][y];
        boolean[][] has_visited = new boolean[size][size];
        boolean flag = true;
        while(cur != board[x][y] || flag){
            for(int[] adjacent : adjacents){
                if(!has_visited[cur.x + adjacent[0]][cur.y + adjacent[1]]
                    && is_valid_liberty(cur.x + adjacent[0], cur.y + adjacent[1], board)){
                    liberties++;
                    has_visited[cur.x + adjacent[0]][cur.y + adjacent[1]] = true;
                }
            }
            flag = false;
            cur = cur.next;
        }
        return liberties;
    }

    private int capture_piece(int x,int y, Piece[][] board){
        int captured = 0;
        Piece cur = board[x][y];
        while(board[cur.x][cur.y] != null){
            board[cur.x][cur.y] = null;
            cur = cur.next;
            captured++;
        }
        return captured;
    }
    private boolean is_valid_piece(int x, int y, Piece[][] board){
        return (is_inside(x, y) && board[x][y] != null);
    }
    private boolean is_valid_liberty(int x, int y, Piece[][] board){
        return (is_inside(x, y) && board[x][y] == null);
    }

    private void set_piece_string(int x, int y, Piece[][] board){
        for(int[] adjacent:adjacents){
            if(is_valid_piece(x + adjacent[0], y + adjacent[1], board)){
                if(board[x + adjacent[0]][y + adjacent[1]].owner == board[x][y].owner){
                    if(!is_in_group(x, y, x + adjacent[0], y + adjacent[1], board)) {
                        Piece next = board[x][y].next;
                        board[x][y].next = board[x + adjacent[0]][y + adjacent[1]].next;
                        board[x + adjacent[0]][y + adjacent[1]].next = next;
                    }
                }
            }
        }
    }
    private boolean is_in_group(int x1, int y1, int x2, int y2, Piece[][] board){
        Piece cur = board[x1][y1];
        boolean flag = true;
        while(cur != board[x1][y1] || flag){
            if(cur == board[x2][y2])return true;
            flag = false;
            cur = cur.next;
        }
        return false;
    }

    private Piece[][] copy_board(){
        Piece[][] coppied_board = new Piece[size][size];
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                if(game_board[x][y] != null)coppied_board[x][y] = new Piece(x, y, game_board[x][y].owner);
            }
        }
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++){
                if(game_board[x][y] != null)coppied_board[x][y].next = coppied_board[game_board[x][y].next.x][game_board[x][y].next.y];
            }
        }
        return coppied_board;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("location", this.location);
        data.put("display_location", this.display_location);
        data.put("display_align", this.display_align);
        return data;
    }

    public static Go deserialize(Map<String, Object> args) {
        return new Go(
            (Location) args.get("location"),
            Optional.ofNullable((Location) args.get("display_location")),
            Optional.ofNullable((String)args.get("display_align"))
        );
    }
    public static class Piece {
        int x, y;
        int owner;
        Piece next = this;
        int group;
        int vital_area = 0;
        int liberty;
        public Piece(int x, int y, int owner){
            this.x = x;
            this.y = y;
            this.owner = owner;
        }
    }
}
