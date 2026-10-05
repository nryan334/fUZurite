package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.listeners.playertimers.HQTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamHQArg extends Argument {

    public TeamHQArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "hq",
                        "home"
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
            sender.sendMessage(Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        HQTimer timer = getInstance().getTimerManager().getHqTimer();

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (pt.getHq() == null) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_HQ.NO_HQ"));
            return;
        }

        if (timer.hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_HQ.ALREADY_WARPING"));
            return;
        }

        if (getInstance().getTimerManager().getCombatTimer().hasTimer(player) &&
                !getConfig().getBoolean("COMBAT_TIMER.HQ_TELEPORT")) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_HQ.COMBAT_TAGGED"));
            return;
        }

        if (getInstance().getTimerManager().getInvincibilityTimer().hasTimer(player) &&
                !getConfig().getBoolean("INVINCIBILITY.HQ_TELEPORT")) {
            player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_HQ.PVP_TIMER")
                    .replace("%team%", pt.getName())
            );
            return;
        }

        if (getInstance().getTimerManager().getPvpTimer().hasTimer(player) &&
                !getConfig().getBoolean("PVP_TIMER.HQ_TELEPORT")) {
            player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_HQ.PVP_TIMER")
                    .replace("%team%", pt.getName())
            );
            return;
        }

        Team team = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (!getConfig().getBoolean("HQ_TIMER.ALLOW_TP_OTHER_CLAIM")) {
            if (team instanceof PlayerTeam && !((PlayerTeam) team).getPlayers().contains(player.getUniqueId())) {
                player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_HQ.NOT_ALLOWED"));
                return;
            }
        }

        if (getInstance().getSotwManager().isActive() && !getInstance().getSotwManager().isEnabled(player)
                && getConfig().getBoolean("HQ_TIMER.INSTANT_TP_SOTW")) {
            timer.tpHq(player);
            return;
        }

        if (team instanceof SafezoneTeam && getConfig().getBoolean("HQ_TIMER.INSTANT_TP_SPAWN")) {
            timer.tpHq(player);
            return;
        }

        timer.applyTimer(player);
        player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_HQ.WARPING")
                .replace("%team%", pt.getName())
                .replace("%seconds%", String.valueOf(timer.getSecondsEnemy(player)))
        );
    }
}