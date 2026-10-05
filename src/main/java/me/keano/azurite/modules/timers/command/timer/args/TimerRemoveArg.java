package me.keano.azurite.modules.timers.command.timer.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TimerRemoveArg extends Argument {

    public TimerRemoveArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "remove"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TIMER_COMMAND.TIMER_REMOVE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        if (args.length == 2) {
            Player player = Bukkit.getPlayer(args[0]);
            PlayerTimer pt = getInstance().getTimerManager().getPlayerTimer(args[1]);

            if (pt == null) {
                sendMessage(sender, getLanguageConfig().getString("TIMER_COMMAND.NOT_FOUND")
                        .replace("%timer%", args[1])
                );
                return;
            }

            if (args[0].equalsIgnoreCase("all")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!pt.hasTimer(online)) continue;
                    pt.removeTimer(online);
                }

                sender.sendMessage(getLanguageConfig().getString("TIMER_COMMAND.TIMER_REMOVE.REMOVED_TIMER_ALL")
                        .replace("%timer%", pt.getName())
                );
                return;
            }

            if (player == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[0])
                );
                return;
            }

            if (!pt.hasTimer(player)) {
                sendMessage(sender, getLanguageConfig().getString("TIMER_COMMAND.TIMER_REMOVE.NO_TIMER")
                        .replace("%player%", args[0])
                        .replace("%timer%", args[1])
                );
                return;
            }

            pt.removeTimer(player);

            sendMessage(sender, getLanguageConfig().getString("TIMER_COMMAND.TIMER_REMOVE.REMOVED_TIMER")
                    .replace("%player%", args[0])
                    .replace("%timer%", args[1])
            );
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 2) {
            String string = args[args.length - 1];
            return getInstance().getTimerManager().getPlayerTimers().keySet()
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}