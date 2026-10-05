package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.timers.listeners.servertimers.RebootTimer;
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
public class RebootCommand extends Command {

    public RebootCommand(CommandManager manager) {
        super(
                manager,
                "reboot"
        );
        this.setPermissible("azurite.reboot");
        this.completions.add(new TabCompletion(Arrays.asList("start", "end", "cancel", "extend"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("REBOOT_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        RebootTimer rebootTimer = getInstance().getTimerManager().getRebootTimer();

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

                if (rebootTimer.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("REBOOT_COMMAND.ALREADY_STARTED"));
                    return;
                }

                rebootTimer.start(time);

                for (String s : getLanguageConfig().getStringList("REBOOT_COMMAND.STARTED")) {
                    Bukkit.broadcastMessage(s
                            .replace("%time%", Formatter.formatDetailed(time))
                    );
                }
                return;

            case "extend":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Long extend = Formatter.parse(args[1]);

                if (extend == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[1])
                    );
                    return;
                }

                if (!rebootTimer.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("REBOOT_COMMAND.NOT_ACTIVE"));
                    return;
                }

                rebootTimer.extend(extend);

                for (String s : getLanguageConfig().getStringList("REBOOT_COMMAND.EXTENDED")) {
                    Bukkit.broadcastMessage(s
                            .replace("%time%", Formatter.formatDetailed(extend))
                    );
                }
                return;

            case "end":
            case "cancel":
                if (!rebootTimer.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("REBOOT_COMMAND.NOT_ACTIVE"));
                    return;
                }

                rebootTimer.cancel();

                for (String s : getLanguageConfig().getStringList("REBOOT_COMMAND.CANCELLED")) {
                    Bukkit.broadcastMessage(s);
                }
                return;
        }

        sendUsage(sender);
    }
}