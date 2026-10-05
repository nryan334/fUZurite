package me.keano.azurite.modules.timers.command.keyall.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.timers.listeners.servertimers.KeyAllTimer;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.Formatter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KeyAllCreateArg extends Argument {

    public KeyAllCreateArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "create",
                        "add",
                        "start"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KEY_ALL_COMMAND.KEY_ALL_CREATE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sendUsage(sender);
            return;
        }

        String name = args[0];
        String displayName = CC.t(args[1]);
        Long time = Formatter.parse(args[2]);
        String command = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

        if (time == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[2])
            );
            return;
        }

        if (getInstance().getTimerManager().getCustomTimer(name) != null) {
            sendMessage(sender, getLanguageConfig().getString("KEY_ALL_COMMAND.KEY_ALL_CREATE.ALREADY_EXISTS")
                    .replace("%name%", name)
            );
            return;
        }

        String spaced = displayName.replace("_", " ");

        new KeyAllTimer(getInstance().getTimerManager(), name, spaced, time, command);
        sendMessage(sender, getLanguageConfig().getString("KEY_ALL_COMMAND.KEY_ALL_CREATE.CREATED")
                .replace("%name%", displayName)
                .replace("%command%", command)
        );
    }
}