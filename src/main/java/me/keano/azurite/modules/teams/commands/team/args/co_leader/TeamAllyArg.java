package me.keano.azurite.modules.teams.commands.team.args.co_leader;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
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
public class TeamAllyArg extends Argument {

    public TeamAllyArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "ally"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.USAGE");
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
        PlayerTeam target = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (getTeamConfig().getInt("TEAMS.ALLIES") == 0) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.ALLIES_DISABLED"));
            return;
        }

        if (target == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (pt == target) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.CANNOT_ALLY_SELF"));
            return;
        }

        if (target.getAllies().size() == getTeamConfig().getInt("TEAMS.ALLIES")) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.TARGET_MAX_ALLIES")
                    .replace("%team%", target.getName())
            );
            target.getAllyRequests().remove(pt.getUniqueID());
            return;
        }

        if (!pt.checkRole(player, Role.CO_LEADER)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.CO_LEADER.getName())
            );
            return;
        }

        if (pt.getAllies().size() == getTeamConfig().getInt("TEAMS.ALLIES")) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.SELF_MAX_ALLIES"));
            return;
        }

        if (pt.getAllies().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.ALREADY_REQUESTED")
                    .replace("%team%", target.getName())
            );
            return;
        }

        if (pt.getAllyRequests().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.ALREADY_REQUESTED")
                    .replace("%team%", target.getName())
            );
            return;
        }

        if (target.getAllyRequests().contains(pt.getUniqueID())) {
            target.getAllies().add(pt.getUniqueID());
            target.getAllyRequests().remove(pt.getUniqueID());
            target.save();

            pt.getAllies().add(target.getUniqueID());
            pt.getAllyRequests().remove(target.getUniqueID());
            pt.save();

            if (target.getFocus() == pt.getUniqueID())
                target.setFocus(null); // make sure allies don't stay focused - keqno

            pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.ALLY_ACCEPTED")
                    .replace("%team%", target.getName())
            );
            target.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.ALLY_ACCEPTED")
                    .replace("%team%", pt.getName())
            );
            return;
        }

        pt.getAllyRequests().add(target.getUniqueID());

        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.REQUEST_SENT")
                .replace("%team%", target.getName())
        );
        target.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALLY.REQUEST_RECEIVED")
                .replace("%team%", pt.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getStringTeams().keySet()
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}