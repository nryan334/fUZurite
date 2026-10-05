package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamInvitesArg extends Argument {

    public TeamInvitesArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "invites"
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
        List<String> invited = calcInvited(player);
        String invitedJoined = String.join("§7, ", invited);
        String nonPending = getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITES.NON_PENDING_PLACEHOLDER");

        for (String s : getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_INVITES.SELF_INVITES")) {
            sendMessage(sender, s
                    .replace("%invites%", (invited.isEmpty() ? nonPending : invitedJoined))
            );
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt != null) {
            List<String> teamInvited = calcTeamInvited(pt);
            String teamInvitedJoined = String.join("§7, ", teamInvited);

            for (String s : getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_INVITES.TEAM_INVITES")) {
                sendMessage(sender, s
                        .replace("%users%", (teamInvited.isEmpty() ? nonPending : teamInvitedJoined))
                );
            }
        }
    }

    private List<String> calcInvited(Player player) {
        List<String> list = new ArrayList<>();

        for (Team team : getInstance().getTeamManager().getTeams().values()) {
            if (team.getType() != TeamType.PLAYER) continue;

            PlayerTeam checking = (PlayerTeam) team;

            if (checking.getInvitedPlayers().contains(player.getUniqueId())) {
                list.add(getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITES.INVITES_FORMAT")
                        .replace("%team%", team.getName())
                        .replace("%displayTeam%", team.getDisplayName(player))
                );
            }
        }

        return list;
    }

    private List<String> calcTeamInvited(PlayerTeam pt) {
        List<String> list = new ArrayList<>();

        for (UUID invitedPlayer : pt.getInvitedPlayers()) {
            User user = getInstance().getUserManager().getByUUID(invitedPlayer);

            if (user != null) {
                list.add(getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITES.USER_FORMAT")
                        .replace("%player%", user.getName())
                );
            }
        }

        return list;
    }
}