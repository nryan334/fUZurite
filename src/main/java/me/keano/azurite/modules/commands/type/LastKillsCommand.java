package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LastKillsCommand extends Command {

    public LastKillsCommand(CommandManager manager) {
        super(
                manager,
                "lastkills"
        );
        this.setPermissible("azurite.lastkills");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("LAST_KILLS_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        User target = getInstance().getUserManager().getByName(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        for (String s : getLanguageConfig().getStringList("LAST_KILLS_COMMAND.FORMAT")) {
            if (!s.equalsIgnoreCase("%lastkills%")) {
                sendMessage(sender, s.replace("%player%", target.getName()));
                continue;
            }

            for (String lastKill : target.getLastKills()) {
                sendMessage(sender, lastKill);
            }
        }
    }
}