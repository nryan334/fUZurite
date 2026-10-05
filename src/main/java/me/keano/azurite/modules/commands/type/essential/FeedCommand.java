package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class FeedCommand extends Command {

    public FeedCommand(CommandManager manager) {
        super(
                manager,
                "feed"
        );
        this.setPermissible("azurite.feed");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("FEED_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.setFoodLevel(20);
                player.sendMessage(getLanguageConfig().getString("FEED_COMMAND.FED_SELF"));
                return;
            }

            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        target.setFoodLevel(20);
        target.sendMessage(getLanguageConfig().getString("FEED_COMMAND.TARGET_MESSAGE")
                .replace("%player%", sender.getName())
        );
        sendMessage(sender, getLanguageConfig().getString("FEED_COMMAND.FED_TARGET")
                .replace("%player%", target.getName())
        );
    }
}