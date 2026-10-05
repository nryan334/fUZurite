package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class GMCCommand extends Command {

    public GMCCommand(CommandManager manager) {
        super(
                manager,
                "gmc"
        );
        this.setPermissible("azurite.gmc");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[0])
                );
                return;
            }

            target.setGameMode(GameMode.CREATIVE);

            sendMessage(target, getLanguageConfig().getString("GAMEMODE_COMMAND.GM_UPDATED")
                    .replace("%gamemode%", target.getGameMode().name().toLowerCase())
            );
            sendMessage(player, getLanguageConfig().getString("GAMEMODE_COMMAND.TARGET_GM_UPDATED")
                    .replace("%target%", target.getName())
                    .replace("%gamemode%", target.getGameMode().name().toLowerCase())
            );
            return;
        }

        player.setGameMode(GameMode.CREATIVE);
        sendMessage(player, getLanguageConfig().getString("GAMEMODE_COMMAND.GM_UPDATED")
                .replace("%gamemode%", player.getGameMode().name().toLowerCase())
        );
    }
}