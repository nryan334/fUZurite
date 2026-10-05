package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.timers.listeners.playertimers.InvincibilityTimer;
import me.keano.azurite.modules.timers.listeners.playertimers.PvPTimer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PvPCommand extends Command {

    public PvPCommand(CommandManager manager) {
        super(
                manager,
                "pvp"
        );
        this.completions.add(new TabCompletion(Arrays.asList("enable", "clear", "time"), 0));
        this.completions.add(new TabCompletion(Arrays.asList("enablefor", "clearfor"), 0, "azurite.pvptimer.admin"));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("PVPTIMER_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        InvincibilityTimer invincibilityTimer = getInstance().getTimerManager().getInvincibilityTimer();
        PvPTimer pvpTimer = getInstance().getTimerManager().getPvpTimer();

        switch (args[0].toLowerCase()) {
            case "clear":
            case "enable":
                if (!invincibilityTimer.hasTimer(player) && !pvpTimer.hasTimer(player)) {
                    sendMessage(sender, getLanguageConfig().getString("PVPTIMER_COMMAND.NO_TIMERS"));
                    return;
                }

                if (invincibilityTimer.hasTimer(player)) {
                    invincibilityTimer.removeTimer(player);
                }

                if (pvpTimer.hasTimer(player)) {
                    pvpTimer.removeTimer(player);
                }

                sendMessage(sender, getLanguageConfig().getString("PVPTIMER_COMMAND.ENABLED"));
                return;

            case "time":
                if (!invincibilityTimer.hasTimer(player) && !pvpTimer.hasTimer(player)) {
                    sendMessage(sender, getLanguageConfig().getString("PVPTIMER_COMMAND.NO_TIMERS"));
                    return;
                }

                if (invincibilityTimer.hasTimer(player)) {
                    sendMessage(sender, getLanguageConfig().getString("PVPTIMER_COMMAND.TIME_FORMAT")
                            .replace("%remaining%", invincibilityTimer.getRemainingString(player))
                    );
                    return;
                }

                if (pvpTimer.hasTimer(player)) {
                    sendMessage(sender, getLanguageConfig().getString("PVPTIMER_COMMAND.TIME_FORMAT")
                            .replace("%remaining%", pvpTimer.getRemainingString(player))
                    );
                    return;
                }
                return;

            case "enablefor":
            case "clearfor":
                if (!sender.hasPermission("azurite.pvptimer.admin")) {
                    sendMessage(sender, Config.INSUFFICIENT_PERM);
                    return;
                }

                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                if (!invincibilityTimer.hasTimer(target) && !pvpTimer.hasTimer(target)) {
                    sendMessage(sender, getLanguageConfig().getString("PVPTIMER_COMMAND.ENABLE_FOR_NO_TIMER")
                            .replace("%target%", target.getName())
                    );
                    return;
                }

                if (invincibilityTimer.hasTimer(target)) {
                    invincibilityTimer.removeTimer(target);
                }

                if (pvpTimer.hasTimer(target)) {
                    pvpTimer.removeTimer(target);
                }

                sendMessage(sender, getLanguageConfig().getString("PVPTIMER_COMMAND.ENABLED_FOR")
                        .replace("%target%", target.getName())
                );
                return;
        }

        sendUsage(sender);
    }
}