package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamUnfocusArg extends Argument {

    public TeamUnfocusArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "unfocus"
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
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (pt.getFocus() == null) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNFOCUS.NO_FOCUS"));
            return;
        }

        WaypointAzurite focusWaypoint = getInstance().getWaypointManager().getFocusWaypoint();
        Team focusTeam = pt.getFocusedTeam();

        // Azurite - Lunar Integration
        for (Player member : pt.getOnlinePlayers(true)) {
            if (focusTeam != null) {
                focusWaypoint.remove(member, focusTeam.getHq(), s -> s
                        .replace("%team%", focusTeam.getName())
                );
            }
        }

        pt.setFocus(null);
        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNFOCUS.FOCUS_CLEARED"));
    }
}