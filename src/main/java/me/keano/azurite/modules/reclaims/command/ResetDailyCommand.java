package me.keano.azurite.modules.reclaims.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ResetDailyCommand extends Command {

    public ResetDailyCommand(CommandManager manager) {
        super(
                manager,
                "resetdaily"
        );
        this.setPermissible("azurite.resetdaily");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("RESET_DAILY_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("ONLINE")) {
            int i = 0;

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                User user = getInstance().getUserManager().getByUUID(onlinePlayer.getUniqueId());
                user.setDailyCooldown(0L);
                user.save();
                i++;
            }

            sendMessage(sender, getLanguageConfig().getString("RESET_DAILY_COMMAND.RESET_DAILY_ONLINE")
                    .replace("%amount%", String.valueOf(i))
            );
            return;
        }

        if (args[0].equalsIgnoreCase("ALL")) {
            List<User> users = new ArrayList<>(getInstance().getUserManager().getUsers().values());

            int i = 0;

            for (User user : users) {
                user.setDailyCooldown(0L);
                user.save();
                i++;
            }

            sendMessage(sender, getLanguageConfig().getString("RESET_DAILY_COMMAND.RESET_DAILY_ALL")
                    .replace("%amount%", String.valueOf(i))
            );
            return;
        }

        User user = getInstance().getUserManager().getByName(args[0]);

        if (user == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (user.getDailyCooldown() < System.currentTimeMillis()) {
            sendMessage(sender, getLanguageConfig().getString("RESET_DAILY_COMMAND.NOT_DAILY")
                    .replace("%player%", user.getName())
            );
            return;
        }

        user.setDailyCooldown(0L);
        user.save();

        sendMessage(sender, getLanguageConfig().getString("RESET_DAILY_COMMAND.RESET_DAILY")
                .replace("%player%", user.getName())
        );
    }
}