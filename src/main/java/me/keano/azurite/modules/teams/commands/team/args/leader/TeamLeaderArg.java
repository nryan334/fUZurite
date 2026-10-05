package me.keano.azurite.modules.teams.commands.team.args.leader;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamLeaderArg extends Argument {

    public TeamLeaderArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "leader"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEADER.USAGE");
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
        User target = getInstance().getUserManager().getByName(args[0]);
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (!pt.checkRole(player, Role.LEADER)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.LEADER.getName())
            );
            return;
        }

        if (!pt.getPlayers().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEADER.NOT_IN_TEAM"));
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEADER.ALREADY_LEADER"));
            return;
        }

        pt.getMember(player.getUniqueId()).setRole(Role.CO_LEADER); // make the old leader step down
        pt.getMember(target.getUniqueID()).setRole(Role.LEADER);
        pt.setLeader(target.getUniqueID());
        pt.save();

        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEADER.LEADER_CHANGED")
                .replace("%player%", target.getName())
        );
    }
}