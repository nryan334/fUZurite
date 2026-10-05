package me.keano.azurite.modules.timers.command.customtimer.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.Formatter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CTimerCreateArg extends Argument {

    public CTimerCreateArg(CommandManager manager) {
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
        return getLanguageConfig().getString("CTIMER_COMMAND.CTIMER_CREATE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }

        String name = args[0];
        String displayName = CC.t(args[1]);
        Long time = Formatter.parse(args[2]);

        if (time == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[2])
            );
            return;
        }

        if (getInstance().getTimerManager().getCustomTimer(name) != null) {
            sendMessage(sender, getLanguageConfig().getString("CTIMER_COMMAND.CTIMER_CREATE.ALREADY_EXISTS")
                    .replace("%name%", name)
            );
            return;
        }

        String spaced = displayName.replace("_", " ");

        new CustomTimer(getInstance().getTimerManager(), name, spaced, time);
        sendMessage(sender, getLanguageConfig().getString("CTIMER_COMMAND.CTIMER_CREATE.CREATED")
                .replace("%name%", displayName)
        );
    }
}