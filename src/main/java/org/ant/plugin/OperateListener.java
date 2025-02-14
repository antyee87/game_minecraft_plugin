package org.ant.plugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class OperateListener implements Listener {
    private final Collection<Chess> chess_games = Game.getInstance().chess_games.values();
    private final Collection<Gomoku> gomoku_games = Game.getInstance().gomoku_games.values();
    private final Collection<Reversi> reversi_games = Game.getInstance().reversi_games.values();
    private final Collection<LightsOut> lightsOut_games = Game.getInstance().lightsOut_games.values();
    private final Collection<ConnectFour> connectFours_games = Game.getInstance().connectFour_games.values();
    private final Collection<ScoreFour> scoreFour_games = Game.getInstance().scoreFour_games.values();

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if(!cooldowns.containsKey(playerId) || currentTime - cooldowns.get(playerId) > 100) {
            cooldowns.put(playerId, currentTime);
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
                if (block == null) return;
                else {
                    Location point = block.getLocation();
                    chess_games.forEach((game) -> {
                        Location board = game.location;
                        int x = point.getBlockX() - board.getBlockX();
                        int y = point.getBlockY() - board.getBlockY();
                        int z = point.getBlockZ() - board.getBlockZ();
                        if (Method.isInRange(y, 0, 1) && game.promotable == null) {
                            if (game.is_inside(x, z)) {
                                if (game.move(x, z, y))
                                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
                                return;
                            }
                        } else if (Method.isInRange(y, 2, 5) && game.promotable != null) {
                            if (game.is_inside(x, z)) {
                                game.promote(y);
                                block.getWorld().playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                return;
                            }
                        }
                    });
                    simpleGameClick(gomoku_games, block, point);
                    simpleGameClick(reversi_games, block, point);
                    simpleGameClick(lightsOut_games, block, point);
                    connectFours_games.forEach((game) -> {
                        Location board = game.location;
                        int x = point.getBlockX() - board.getBlockX();
                        int y = point.getBlockY() - board.getBlockY();
                        int z = point.getBlockZ() - board.getBlockZ();
                        if (y <= 6) {
                            if (game.align.equals("x") && point.getBlockZ() == board.getBlockZ() && game.is_inside(0, x)) {
                                if (game.move(x))
                                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
                            } else if (game.align.equals("z") && point.getBlockX() == board.getBlockX() && game.is_inside(0, z)) {
                                if (game.move(z))
                                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
                            }

                        }
                    });
                    scoreFour_games.forEach((game) -> {
                        Location board = game.location;
                        int x = (point.getBlockX() - board.getBlockX()) / 2;
                        int y = point.getBlockY() - board.getBlockY();
                        int z = (point.getBlockZ() - board.getBlockZ()) / 2;
                        if (Method.isInRange(y, 0, 4)) {
                            if (game.is_inside(x, z, 0)) {
                                if (game.move(x, z))
                                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
                            }
                        }
                    });
                }
            }
        }
    }
    private <T extends BoardGame> void simpleGameClick(Collection<T> games, Block block, Location point) {
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(event.hasChangedPosition()) {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL) {
                boolean[] flyable = {false};
                set_fly(scoreFour_games, player, flyable);
                player.setAllowFlight(flyable[0]);
            }
        }
    }
    private <T extends BasicValue> void set_fly(Collection<T> games, Player player, boolean[] flyable) {
        if(flyable[0])return;
        Location location = player.getLocation();
        games.forEach(game -> {
            Location center = game.getCenter();
            double distance = location.distance(center);
            if(distance < (double)game.getSize() / 2 + 5)flyable[0] = true;
        });
    }
}
