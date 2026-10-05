package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class StatsCommand extends Command {

    public StatsCommand(CommandManager manager) {
        super(
                manager,
                "stats"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "statistics"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("STATS_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                sendUsage(sender);
                return;
            }

            User user = getInstance().getUserManager().getByName(args[0]);

            if (user == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[0])
                );
                return;
            }

            for (String s : getLanguageConfig().getStringList("STATS_COMMAND.STATS_TARGET"))
                sendMessage(sender, s
                        .replace("%kills%", String.valueOf(user.getKills()))
                        .replace("%deaths%", String.valueOf(user.getDeaths()))
                        .replace("%kdr%", String.valueOf(user.getKDRString()))
                        .replace("%killstreak%", String.valueOf(user.getKillstreak()))
                );
            return;
        }

        Player player = (Player) sender;
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (args.length == 0) {
            for (String s : getLanguageConfig().getStringList("STATS_COMMAND.STATS_SELF"))
                sendMessage(sender, s
                        .replace("%kills%", String.valueOf(user.getKills()))
                        .replace("%deaths%", String.valueOf(user.getDeaths()))
                        .replace("%kdr%", String.valueOf(user.getKDRString()))
                        .replace("%killstreak%", String.valueOf(user.getKillstreak()))
                );
            return;
        }

        User targetUser = getInstance().getUserManager().getByName(args[0]);

        if (targetUser == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        for (String s : getLanguageConfig().getStringList("STATS_COMMAND.STATS_TARGET"))
            sendMessage(sender, s
                    .replace("%kills%", String.valueOf(targetUser.getKills()))
                    .replace("%deaths%", String.valueOf(targetUser.getDeaths()))
                    .replace("%kdr%", String.valueOf(targetUser.getKDRString()))
                    .replace("%killstreak%", String.valueOf(targetUser.getKillstreak()))
                    .replace("%player%", targetUser.getName())
            );
    }
}