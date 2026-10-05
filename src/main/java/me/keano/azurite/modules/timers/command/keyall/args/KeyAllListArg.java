package me.keano.azurite.modules.timers.command.keyall.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.timers.type.CustomTimer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KeyAllListArg extends Argument {

    public KeyAllListArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "names",
                        "list"
                )
        );
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (String string : getLanguageConfig().getStringList("KEY_ALL_COMMAND.KEY_ALL_LIST.TIMERS_LIST")) {
            if (!string.equalsIgnoreCase("%timers%")) {
                sendMessage(sender, string);
                continue;
            }

            for (CustomTimer ct : getInstance().getTimerManager().getCustomTimers().values()) {
                sendMessage(sender, getLanguageConfig().getString("KEY_ALL_COMMAND.KEY_ALL_LIST.TIMERS_FORMAT")
                        .replace("%name%", ct.getName())
                        .replace("%remaining%", ct.getRemainingString())
                        .replace("%displayName%", ct.getDisplayName())
                );
            }
        }
    }
}