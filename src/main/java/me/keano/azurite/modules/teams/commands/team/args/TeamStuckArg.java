package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.timers.listeners.playertimers.StuckTimer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamStuckArg extends Argument {

    public TeamStuckArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "stuck"
                )
        );
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        StuckTimer timer = getInstance().getTimerManager().getStuckTimer();

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_STUCK.CANNOT_STUCK"));
            return;
        }

        if (timer.hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_STUCK.ALREADY_STUCKING"));
            return;
        }

        if (!getConfig().getBoolean("COMBAT_TIMER.STUCK_TELEPORT")
                && getInstance().getTimerManager().getCombatTimer().hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_STUCK.COMBAT_TAGGED"));
            return;
        }

        timer.applyTimer(player);
        sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_STUCK.STARTED_STUCK")
                .replace("%seconds%", String.valueOf(timer.getSeconds()))
                .replace("%blocks%", String.valueOf(timer.getMaxMoveBlocks()))
        );
    }
}