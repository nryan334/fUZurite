package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.timers.listeners.playertimers.LogoutTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LogoutCommand extends Command {

    public LogoutCommand(CommandManager manager) {
        super(
                manager,
                "logout"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        LogoutTimer logoutTimer = getInstance().getTimerManager().getLogoutTimer();

        if (logoutTimer.hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("LOGOUT_COMMAND.ALREADY_ACTIVE"));
            return;
        }

        if (getConfig().getBoolean("COMBAT_TIMER.LOGOUT_COMMAND")
                && getInstance().getTimerManager().getCombatTimer().hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("LOGOUT_COMMAND.COMBAT_TAGGED"));
            return;
        }

        logoutTimer.applyTimer(player);
        sendMessage(player, getLanguageConfig().getString("LOGOUT_COMMAND.STARTED_LOGOUT")
                .replace("%seconds%", String.valueOf(logoutTimer.getSeconds()))
        );
    }
}