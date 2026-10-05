package me.keano.azurite.modules.timers.command.customtimer.args;

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
public class CTimerListArg extends Argument {

    public CTimerListArg(CommandManager manager) {
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
        for (String string : getLanguageConfig().getStringList("CTIMER_COMMAND.CTIMER_LIST.TIMERS_LIST")) {
            if (!string.equalsIgnoreCase("%timers%")) {
                sendMessage(sender, string);
                continue;
            }

            for (CustomTimer ct : getInstance().getTimerManager().getCustomTimers().values()) {
                sendMessage(sender, getLanguageConfig().getString("CTIMER_COMMAND.CTIMER_LIST.TIMERS_FORMAT")
                        .replace("%name%", ct.getName())
                        .replace("%remaining%", ct.getRemainingString())
                        .replace("%displayName%", ct.getDisplayName())
                );
            }
        }
    }
}