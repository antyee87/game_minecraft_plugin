package org.ant.plugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class Execute {
    public static int set_board(CommandContext<CommandSourceStack> ctx, String mode) {
        Entity executor = ctx.getSource().getExecutor();
        if(executor != null){
            Location location = executor.getLocation().getBlock().getLocation();
            String name = ctx.getArgument("name", String.class);
            switch (mode) {
                case "chess":
                    Game.getInstance().chess_games.put(name, new Chess(location));
                    break;
                case "gomoku":
                    Game.getInstance().gomoku_games.put(name, new Gomoku(location, Optional.empty(), Optional.empty()));
                    break;
                case "reversi":
                    Game.getInstance().reversi_games.put(name, new Reversi(location, Optional.empty(), Optional.empty()));
                    break;
                case "lights_out":
                    int size = ctx.getArgument("size", int.class);
                    Game.getInstance().lightsOut_games.put(name, new LightsOut(location, size, Optional.empty(), Optional.empty()));
                    break;
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public  static int set_display(CommandContext<CommandSourceStack> ctx, String mode) {
        String name = ctx.getArgument("name", String.class);
        String align = ctx.getArgument("align", String.class);
        Entity executor = ctx.getSource().getExecutor();
        if(executor != null){
            Location location = executor.getLocation().getBlock().getLocation();
            switch (mode) {
                case "gomoku":
                    if(Game.getInstance().gomoku_games.containsKey(name)) {
                        Gomoku gomoku = Game.getInstance().gomoku_games.get(name);
                        gomoku.remove_display();
                        gomoku.set_display(location, align);
                    }
                    break;
                case "reversi":
                    if(Game.getInstance().reversi_games.containsKey(name)) {
                        Reversi reversi = Game.getInstance().reversi_games.get(name);
                        reversi.remove_display();
                        reversi.set_display(location, align);
                    }
                    break;
                case "lights_out":
                    if(Game.getInstance().lightsOut_games.containsKey(name)) {
                        LightsOut lightsOut = Game.getInstance().lightsOut_games.get(name);
                        lightsOut.remove_display();
                        lightsOut.set_display(location, align);
                    }
                    break;
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int reset_board(CommandContext<CommandSourceStack> ctx, String mode) {
        String name = ctx.getArgument("name", String.class);
        switch (mode) {
            case "chess":
                if(Game.getInstance().chess_games.containsKey(name))Game.getInstance().chess_games.get(name).reset();
                break;
            case "gomoku":
                if(Game.getInstance().gomoku_games.containsKey(name))Game.getInstance().gomoku_games.get(name).reset();
                break;
            case "reversi":
                if(Game.getInstance().reversi_games.containsKey(name))Game.getInstance().reversi_games.get(name).reset();
                break;
            case "lights_out":
                if(Game.getInstance().lightsOut_games.containsKey(name))Game.getInstance().lightsOut_games.get(name).reset();
                break;
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int remove_board(CommandContext<CommandSourceStack> ctx, String mode) {
        String name = ctx.getArgument("name", String.class);
        switch (mode) {
            case "chess":
                if(Game.getInstance().chess_games.containsKey(name)) {
                    Game.getInstance().chess_games.get(name).remove();
                    Game.getInstance().chess_games.remove(name);
                }
                break;
            case "gomoku":
                if(Game.getInstance().gomoku_games.containsKey(name)) {
                    Game.getInstance().gomoku_games.get(name).remove();
                    Game.getInstance().gomoku_games.get(name).remove_display();
                    Game.getInstance().gomoku_games.remove(name);
                }
                break;
            case "reversi":
                if(Game.getInstance().reversi_games.containsKey(name)) {
                    Game.getInstance().reversi_games.get(name).remove();
                    Game.getInstance().reversi_games.get(name).remove_display();
                    Game.getInstance().reversi_games.remove(name);
                }
                break;
            case "lights_out":
                if(Game.getInstance().lightsOut_games.containsKey(name)) {
                    Game.getInstance().lightsOut_games.get(name).remove();
                    Game.getInstance().lightsOut_games.get(name).remove_display();
                    Game.getInstance().lightsOut_games.remove(name);
                }
                break;
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int remove_display(CommandContext<CommandSourceStack> ctx, String mode) {
        String name = ctx.getArgument("name", String.class);
        switch (mode) {
            case "gomoku":
                if(Game.getInstance().gomoku_games.containsKey(name))Game.getInstance().gomoku_games.get(name).remove_display();
                break;
            case "reversi":
                if(Game.getInstance().reversi_games.containsKey(name))Game.getInstance().reversi_games.get(name).remove_display();
                break;
            case "lights_out":
                if(Game.getInstance().lightsOut_games.containsKey(name))Game.getInstance().lightsOut_games.get(name).remove_display();
                break;
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int get_player(CommandContext<CommandSourceStack> ctx, String mode) {
        String name = ctx.getArgument("name", String.class);
        switch (mode) {
            case "chess":
                if(Game.getInstance().chess_games.containsKey(name))return Game.getInstance().chess_games.get(name).player + 1;
            case "gomoku":
                if(Game.getInstance().gomoku_games.containsKey(name))return Game.getInstance().gomoku_games.get(name).player;
            case "reversi":
                if(Game.getInstance().reversi_games.containsKey(name))return Game.getInstance().reversi_games.get(name).player;
        }
        return -1;
    }
}
