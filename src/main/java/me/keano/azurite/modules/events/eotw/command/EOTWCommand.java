package me.keano.azurite.modules.events.eotw.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.eotw.EOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class EOTWCommand extends Command {

    public EOTWCommand(CommandManager manager) {
        super(
                manager,
                "EOTW"
        );
        this.setPermissible("azurite.eotw");
        this.completions.add(new TabCompletion(Arrays.asList("start", "cancel", "extend", "whitelist"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("EOTW_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        EOTWManager eotwManager = getInstance().getEotwManager();

        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Long time = Formatter.parse(args[1]);

                if (time == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[1])
                    );
                    return;
                }

                eotwManager.startPreEOTW(time);
                sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.STARTED")
                        .replace("%time%", args[1])
                );
                return;

            case "cancel":
                if (eotwManager.isNotPreEOTW()) {
                    sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.NOT_PRE_EOTW"));
                    return;
                }

                for (String s : getLanguageConfig().getStringList("EOTW_TIMER.CANCELLED_EOTW")) {
                    Bukkit.broadcastMessage(s);
                }

                getInstance().getTimerManager().getCustomTimers().remove("EOTW");
                sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.CANCELLED"));
                return;

            case "extend":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                if (eotwManager.isNotPreEOTW()) {
                    sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.NOT_PRE_EOTW"));
                    return;
                }

                Long extend = Formatter.parse(args[1]);

                if (extend == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[1])
                    );
                    return;
                }

                CustomTimer timer = getInstance().getTimerManager().getCustomTimer("EOTW");
                timer.setRemaining(timer.getRemaining() + extend);
                sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.EXTENDED")
                        .replace("%time%", timer.getRemainingString())
                );
                return;

            case "whitelist":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                User target = getInstance().getUserManager().getByName(args[1]);

                if (target == null) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                if (eotwManager.getWhitelisted().remove(target.getUniqueID())) {
                    sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.REMOVED_WHITELIST")
                            .replace("%player%", target.getName())
                    );
                    return;
                }

                eotwManager.getWhitelisted().add(target.getUniqueID());
                sendMessage(sender, getLanguageConfig().getString("EOTW_COMMAND.WHITELISTED")
                        .replace("%player%", target.getName())
                );
                return;
        }

        sendUsage(sender);
    }
}