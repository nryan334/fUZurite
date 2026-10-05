package me.keano.azurite.modules.teams.commands.systeam.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.enums.MountainType;
import me.keano.azurite.modules.teams.type.MountainTeam;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import me.keano.azurite.modules.waypoints.WaypointManager;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SysTeamSetHqArg extends Argument {

    public SysTeamSetHqArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "sethq",
                        "sethome"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_SETHQ.USAGE");
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
        Team team = getInstance().getTeamManager().getTeam(args[0]);

        if (team == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (team instanceof MountainTeam) {
            MountainTeam mt = (MountainTeam) team;
            WaypointManager manager = getInstance().getWaypointManager();
            WaypointAzurite waypoint = (mt.getMountainType() == MountainType.GLOWSTONE ?
                    manager.getGlowstoneWaypoint() :
                    manager.getOreMountainWaypoint());

            for (Player online : Bukkit.getOnlinePlayers()) {
                waypoint.remove(online, team.getHq(), UnaryOperator.identity());
                waypoint.send(online, player.getLocation(), UnaryOperator.identity());
            }
        }

        team.setHq(player.getLocation());
        team.save();

        sendMessage(sender, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_SETHQ.SET_HQ")
                .replace("%team%", team.getDisplayName(player))
                .replace("%location%", Utils.formatLocation(player.getLocation()))
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getSystemTeams().values()
                    .stream()
                    .map(Team::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}