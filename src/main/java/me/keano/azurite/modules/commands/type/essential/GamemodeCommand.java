package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class GamemodeCommand extends Command {

    public GamemodeCommand(CommandManager manager) {
        super(
                manager,
                "gamemode"
        );
        this.completions.add(new TabCompletion(Arrays.asList("0", "1", "c", "s", "survival", "creative"), 0));
        this.setPermissible("azurite.gamemode");
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "gm"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("GAMEMODE_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "creative":
                case "c":
                case "1":
                    player.setGameMode(GameMode.CREATIVE);
                    sendMessage(sender, getLanguageConfig().getString("GAMEMODE_COMMAND.GM_UPDATED")
                            .replace("%gamemode%", player.getGameMode().name().toLowerCase())
                    );
                    return;

                case "survival":
                case "s":
                case "0":
                    player.setGameMode(GameMode.SURVIVAL);
                    sendMessage(sender, getLanguageConfig().getString("GAMEMODE_COMMAND.GM_UPDATED")
                            .replace("%gamemode%", player.getGameMode().name().toLowerCase())
                    );
                    return;
            }

            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[1])
            );
            return;
        }

        switch (args[0].toLowerCase()) {
            case "creative":
            case "c":
            case "1":
                target.setGameMode(GameMode.CREATIVE);

                sendMessage(target, getLanguageConfig().getString("GAMEMODE_COMMAND.GM_UPDATED")
                        .replace("%gamemode%", target.getGameMode().name().toLowerCase())
                );
                sendMessage(player, getLanguageConfig().getString("GAMEMODE_COMMAND.TARGET_GM_UPDATED")
                        .replace("%target%", target.getName())
                        .replace("%gamemode%", target.getGameMode().name().toLowerCase())
                );
                return;

            case "survival":
            case "s":
            case "0":
                target.setGameMode(GameMode.SURVIVAL);

                sendMessage(target, getLanguageConfig().getString("GAMEMODE_COMMAND.GM_UPDATED")
                        .replace("%gamemode%", target.getGameMode().name().toLowerCase())
                );
                sendMessage(player, getLanguageConfig().getString("GAMEMODE_COMMAND.TARGET_GM_UPDATED")
                        .replace("%target%", target.getName())
                        .replace("%gamemode%", target.getGameMode().name().toLowerCase())
                );
                return;
        }

        sendUsage(sender);
    }
}