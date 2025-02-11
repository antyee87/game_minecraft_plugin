package org.ant.plugin;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import java.util.Collection;

public class OperateListener implements Listener {
    Collection<Chess> chess_games = Game.getInstance().chess_games.values();
    Collection<Gomoku> gomoku_games = Game.getInstance().gomoku_games.values();
    Collection<Reversi> reversi_games = Game.getInstance().reversi_games.values();
    Collection<LightsOut> lightsOut_games = Game.getInstance().lightsOut_games.values();
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND){
            if (block == null) return;
            else{
                Location point = block.getLocation();
                chess_games.forEach((game) -> {
                    Location board = game.location;
                    int x= point.getBlockX() - board.getBlockX();
                    int y= point.getBlockY() - board.getBlockY();
                    int z= point.getBlockZ() - board.getBlockZ();
                    if(Method.isInRange(y,0,1) && game.promotable == null){
                        if(game.is_inside(x,z)){
                            if(game.move(x, z, y))block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
                            return;
                        }
                    }
                    else if(Method.isInRange(y,2,5) && game.promotable != null){
                        if(game.is_inside(x,z)){
                            game.promote(y);
                            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                            return;
                        }
                    }
                });
                processGameClick(gomoku_games, block, point);
                processGameClick(reversi_games, block, point);
                processGameClick(lightsOut_games, block, point);
            }
        }
    }
    private <T extends BoardGame> void processGameClick(Collection<T> games, Block block, Location point) {
        games.forEach(game -> {
            Location board = game.location;
            int x = point.getBlockX() - board.getBlockX();
            int z = point.getBlockZ() - board.getBlockZ();

            if (point.getBlockY() == board.getBlockY() && game.is_inside(x, z)) {
                if (game.move(x, z)) {
                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
                }
            }
        });
    }
}
