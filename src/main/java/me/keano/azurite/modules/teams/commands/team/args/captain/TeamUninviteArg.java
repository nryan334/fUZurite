package me.keano.azurite.modules.teams.commands.team.args.captain;

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
public class TeamUninviteArg extends Argument {

    public TeamUninviteArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "uninvite"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNINVITE.USAGE");
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

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (!pt.checkRole(player, Role.CAPTAIN)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.CAPTAIN.getName())
            );
            return;
        }

        if (args[0].equalsIgnoreCase("ALL")) {
            pt.getInvitedPlayers().clear();
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNINVITE.UNINVITED_ALL"));
            return;
        }

        User target = getInstance().getUserManager().getByName(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (!pt.getInvitedPlayers().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNINVITE.NEVER_INVITED"));
            return;
        }

        pt.getInvitedPlayers().remove(target.getUniqueID());
        sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNINVITE.UNINVITED_PLAYER")
                .replace("%player%", target.getName())
        );
    }
}