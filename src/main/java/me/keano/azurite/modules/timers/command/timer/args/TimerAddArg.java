package me.keano.azurite.modules.timers.command.timer.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Formatter;
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
public class TimerAddArg extends Argument {

    public TimerAddArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "add"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TIMER_COMMAND.TIMER_ADD.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }

        if (args.length == 3) {
            Player player = Bukkit.getPlayer(args[0]);
            PlayerTimer pt = getInstance().getTimerManager().getPlayerTimer(args[1]);
            Long time = Formatter.parse(args[2]);

            if (pt == null) {
                sendMessage(sender, getLanguageConfig().getString("TIMER_COMMAND.NOT_FOUND")
                        .replace("%timer%", args[1])
                );
                return;
            }

            if (time == null) {
                sendMessage(sender, Config.NOT_VALID_NUMBER
                        .replace("%number%", args[2])
                );
                return;
            }

            if (args[0].equalsIgnoreCase("all")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    pt.applyTimer(online, time);
                }

                sender.sendMessage(getLanguageConfig().getString("TIMER_COMMAND.TIMER_ADD.ADDED_TIMER_ALL")
                        .replace("%timer%", pt.getName())
                        .replace("%time%", Formatter.getRemaining(time, true))
                );
                return;
            }

            if (player == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[0])
                );
                return;
            }

            pt.applyTimer(player, time);

            sendMessage(sender, getLanguageConfig().getString("TIMER_COMMAND.TIMER_ADD.ADDED_TIMER")
                    .replace("%timer%", pt.getName())
                    .replace("%player%", player.getName())
                    .replace("%time%", Formatter.getRemaining(time, true))
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