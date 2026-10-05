package me.keano.azurite.modules.timers.command.keyall.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.timers.listeners.servertimers.KeyAllTimer;
import me.keano.azurite.modules.timers.type.CustomTimer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KeyAllDeleteArg extends Argument {

    public KeyAllDeleteArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "delete",
                        "remove",
                        "stop"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KEY_ALL_COMMAND.KEY_ALL_DELETE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        CustomTimer ct = getInstance().getTimerManager().getCustomTimer(args[0]);

        if (ct == null) {
            sendMessage(sender, getLanguageConfig().getString("KEY_ALL_COMMAND.KEY_ALL_DELETE.NOT_FOUND")
                    .replace("%name%", args[0])
            );
            return;
        }

        if (!(ct instanceof KeyAllTimer)) {
            sendMessage(sender, getLanguageConfig().getString("KEY_ALL_COMMAND.KEY_ALL_DELETE.NOT_FOUND")
                    .replace("%name%", args[0])
            );
            return;
        }

        getInstance().getTimerManager().getCustomTimers().remove(ct.getName());
        sendMessage(sender, getLanguageConfig().getString("KEY_ALL_COMMAND.KEY_ALL_DELETE.DELETED")
                .replace("%name%", args[0])
        );
    }
}