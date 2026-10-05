package me.keano.azurite.modules.timers.command.customtimer.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.timers.type.CustomTimer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CTimerDeleteArg extends Argument {

    public CTimerDeleteArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "delete",
                        "remove",
                        "stop",
                        "end"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("CTIMER_COMMAND.CTIMER_DELETE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        CustomTimer ct = getInstance().getTimerManager().getCustomTimer(args[0]);

        if (ct == null) {
            sendMessage(sender, getLanguageConfig().getString("CTIMER_COMMAND.CTIMER_DELETE.NOT_FOUND")
                    .replace("%name%", args[0])
            );
            return;
        }

        getInstance().getTimerManager().getCustomTimers().remove(ct.getName());
        sendMessage(sender, getLanguageConfig().getString("CTIMER_COMMAND.CTIMER_DELETE.DELETED")
                .replace("%name%", args[0])
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTimerManager().getCustomTimers().keySet()
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}