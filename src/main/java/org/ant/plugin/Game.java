package org.ant.plugin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.*;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class Game extends JavaPlugin {
    private static Game instance;

    HashMap<String, Chess> chess_games = new HashMap<String, Chess>();
    HashMap<String, Gomoku> gomoku_games = new HashMap<String, Gomoku>();
    HashMap<String, Reversi> reversi_games = new HashMap<String, Reversi>();
    HashMap<String, LightsOut> lightsOut_games = new HashMap<String, LightsOut>();

    @Override
    public void onEnable() {
        getLogger().info("Ant遊戲插件已啟用");
        instance = this;
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().getDispatcher().register(Commands.literal("game")
                .requires(sender -> sender.getSender().isOp())
                .then(Commands.literal("chess")
                    .then(Commands.literal("board")
                        .then(Commands.literal("set")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> Execute.set_board(ctx,"chess"))
                            )
                        )
                        .then(Commands.literal("reset")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> chess_suggestions(builder))
                                .executes(ctx -> Execute.reset_board(ctx,"chess"))
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> chess_suggestions(builder))
                                .executes(ctx -> Execute.remove_board(ctx,"chess"))
                            )
                        )
                    )
                    .then(Commands.literal("get_player")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((context, builder) -> chess_suggestions(builder))
                            .executes(ctx -> Execute.get_player(ctx, "chess"))
                        )
                    )
                )
                .then(Commands.literal("gomoku")
                    .then(Commands.literal("board")
                        .then(Commands.literal("set")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> Execute.set_board(ctx,"gomoku"))
                            )
                        )
                        .then(Commands.literal("reset")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> gomoku_suggestions(builder))
                                .executes(ctx -> Execute.reset_board(ctx, "gomoku"))
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> gomoku_suggestions(builder))
                                .executes(ctx -> Execute.remove_board(ctx, "gomoku"))
                            )
                        )
                    )
                    .then(Commands.literal("display")
                        .then(Commands.literal("set")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> gomoku_suggestions(builder))
                                .then(Commands.argument("align", StringArgumentType.word())
                                    .suggests((context, builder)->align_suggestions(builder))
                                    .executes(ctx -> Execute.set_display(ctx, "gomoku"))
                                )
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> gomoku_suggestions(builder))
                                .executes(ctx -> Execute.remove_display(ctx, "gomoku"))
                            )
                        )
                    )
                    .then(Commands.literal("get_player")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((context, builder) -> gomoku_suggestions(builder))
                            .executes(ctx -> Execute.get_player(ctx, "gomoku"))
                        )
                    )
                )
                .then(Commands.literal("reversi")
                    .then(Commands.literal("board")
                        .then(Commands.literal("set")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> Execute.set_board(ctx,"reversi"))
                            )
                        )
                        .then(Commands.literal("reset")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> reversi_suggestions(builder))
                                .executes(ctx -> Execute.reset_board(ctx, "reversi"))
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> reversi_suggestions(builder))
                                .executes(ctx -> Execute.remove_board(ctx, "reversi"))
                            )
                        )
                    )
                    .then(Commands.literal("display")
                        .then(Commands.literal("set")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> reversi_suggestions(builder))
                                .then(Commands.argument("align", StringArgumentType.word())
                                    .suggests((context, builder)->align_suggestions(builder))
                                    .executes(ctx -> Execute.set_display(ctx, "reversi"))
                                )
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> reversi_suggestions(builder))
                                .executes(ctx -> Execute.remove_display(ctx,"reversi"))
                            )
                        )
                    )
                    .then(Commands.literal("get_player")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .suggests((context, builder) -> reversi_suggestions(builder))
                            .executes(ctx -> Execute.get_player(ctx, "reversi"))
                        )
                    )
                )
                .then(Commands.literal("lights_out")
                    .then(Commands.literal("board")
                        .then(Commands.literal("set")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("size", IntegerArgumentType.integer())
                                    .executes(ctx -> Execute.set_board(ctx, "lights_out"))
                                )
                            )
                        )
                        .then(Commands.literal("reset")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> lightsOut_suggestions(builder))
                                .executes(ctx -> Execute.reset_board(ctx, "lights_out"))
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> lightsOut_suggestions(builder))
                                .executes(ctx -> Execute.remove_board(ctx, "lights_out"))
                            )
                        )
                    )
                    .then(Commands.literal("display")
                        .then(Commands.literal("set")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> lightsOut_suggestions(builder))
                                .then(Commands.argument("align", StringArgumentType.word())
                                    .suggests((context, builder)->align_suggestions(builder))
                                    .executes(ctx -> Execute.set_display(ctx, "lights_out"))
                                )
                            )
                        )
                        .then(Commands.literal("remove")
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> lightsOut_suggestions(builder))
                                .executes(ctx -> Execute.remove_display(ctx,"lights_out"))
                            )
                        )
                    )
                )
            );
        });
        ConfigurationSerialization.registerClass(Chess.class);
        ConfigurationSerialization.registerClass(Gomoku.class);
        ConfigurationSerialization.registerClass(Reversi.class);
        ConfigurationSerialization.registerClass(LightsOut.class);
        getServer().getPluginManager().registerEvents(new OperateListener(), this);
        saveDefaultConfig();

        //chess games deserialize
        ConfigurationSection section = getConfig().getConfigurationSection("chess_games");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Map<String, Object> data = section.getConfigurationSection(key).getValues(false);
                chess_games.put(key, Chess.deserialize(data));
            }
        }
        //gomoku games deserialize
        section = getConfig().getConfigurationSection("gomoku_games");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Map<String, Object> data = section.getConfigurationSection(key).getValues(false);
                gomoku_games.put(key, Gomoku.deserialize(data));
            }
        }
        //reversi games deserialize
        section = getConfig().getConfigurationSection("reversi_games");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Map<String, Object> data = section.getConfigurationSection(key).getValues(false);
                reversi_games.put(key, Reversi.deserialize(data));
            }
        }
        //lightsOut games deserialize
        section = getConfig().getConfigurationSection("lightsOut_games");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Map<String, Object> data = section.getConfigurationSection(key).getValues(false);
                lightsOut_games.put(key, LightsOut.deserialize(data));
            }
        }
    }

    private CompletableFuture<Suggestions> align_suggestions(SuggestionsBuilder builder) {
        builder.suggest("x");
        builder.suggest("z");
        return  CompletableFuture.completedFuture(builder.build());
    }
    private CompletableFuture<Suggestions> chess_suggestions(SuggestionsBuilder builder) {
        chess_games.keySet().forEach((name) -> {builder.suggest(name);});
        return CompletableFuture.completedFuture(builder.build());
    }
    private CompletableFuture<Suggestions> gomoku_suggestions(SuggestionsBuilder builder) {
        gomoku_games.keySet().forEach((name) -> {builder.suggest(name);});
        return CompletableFuture.completedFuture(builder.build());
    }
    private CompletableFuture<Suggestions> reversi_suggestions(SuggestionsBuilder builder) {
        reversi_games.keySet().forEach((name) -> {builder.suggest(name);});
        return CompletableFuture.completedFuture(builder.build());
    }
    private CompletableFuture<Suggestions> lightsOut_suggestions(SuggestionsBuilder builder) {
        lightsOut_games.keySet().forEach((name) -> {builder.suggest(name);});
        return CompletableFuture.completedFuture(builder.build());
    }

    @Override
    public void onDisable() {
        //chess games serialize
        getConfig().set("chess_games", null);
        for (Map.Entry<String, Chess> entry : chess_games.entrySet()) {
            getConfig().set("chess_games." + entry.getKey(), entry.getValue().serialize());
        }
        //gomoku games serialize
        getConfig().set("gomoku_games", null);
        for (Map.Entry<String, Gomoku> entry : gomoku_games.entrySet()) {
            getConfig().set("gomoku_games." + entry.getKey(), entry.getValue().serialize());
        }
        //reversi games serialize
        getConfig().set("reversi_games", null);
        for (Map.Entry<String, Reversi> entry : reversi_games.entrySet()) {
            getConfig().set("reversi_games." + entry.getKey(), entry.getValue().serialize());
        }
        //lightOut games serialize
        getConfig().set("lightsOut_games", null);
        for (Map.Entry<String, LightsOut> entry : lightsOut_games.entrySet()) {
            getConfig().set("lightsOut_games." + entry.getKey(), entry.getValue().serialize());
        }
        saveConfig();
        getLogger().info("Ant遊戲插件已停用");
    }
    public static Game getInstance() {
        return instance;
    }
}
