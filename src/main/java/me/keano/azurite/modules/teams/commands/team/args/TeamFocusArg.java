package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
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
public class TeamFocusArg extends Argument {

    public TeamFocusArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "focus"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_FOCUS.USAGE");
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
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        Team target = getInstance().getTeamManager().getFocus(args[0]);

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (target == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (pt.getAllies().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FOCUS.FOCUS_ALLY"));
            return;
        }

        if (pt == target) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FOCUS.FOCUS_SELF"));
            return;
        }

        // Can clear using this or /f unfocus - whichever is easier for them
        if (pt.getFocus() != null && pt.getFocus() == target.getUniqueID()) {
            // Azurite - Lunar Integration
            for (Player member : pt.getOnlinePlayers(true)) {
                getInstance().getWaypointManager().getFocusWaypoint().remove(member, target.getHq(), s -> s
                        .replace("%team%", target.getName())
                );
            }

            pt.setFocus(null);
            pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNFOCUS.FOCUS_CLEARED"));
            return;
        }

        // Azurite - Lunar Integration
        for (Player member : pt.getOnlinePlayers(true)) {
            if (pt.getFocus() != null) {
                Team currentFocus = pt.getFocusedTeam();

                if (currentFocus != null) {
                    getInstance().getWaypointManager().getFocusWaypoint().remove(member, currentFocus.getHq(), s ->
                            s.replace("%team%", currentFocus.getName())
                    );
                }
            }

            getInstance().getWaypointManager().getFocusWaypoint().send(member, target.getHq(), s -> s
                    .replace("%team%", target.getName())
            );
        }

        pt.setFocus(target.getUniqueID());
        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_FOCUS.FOCUS_UPDATED")
                .replace("%team%", target.getName())
                .replace("%player%", player.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getTeams().values()
                    .stream()
                    .map(Team::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}