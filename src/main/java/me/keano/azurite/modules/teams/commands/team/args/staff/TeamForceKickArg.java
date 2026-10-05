package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamForceKickArg extends Argument {

    public TeamForceKickArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "forcekick",
                        "staffkick"
                )
        );
        this.setPermissible("azurite.team.forcekick");
    }


    @Override
    public String usage() {
        return getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEKICK.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        User target = getInstance().getUserManager().getByName(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(target.getUniqueID());

        if (pt == null) {
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEKICK.NOT_IN_TEAM"));
            return;
        }

        if (pt.getPlayers().size() == 1) {
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEKICK.TOO_LITTLE_MEMBER"));
            return;
        }

        pt.removeMember(target);
        pt.broadcast(getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEKICK.BROADCAST_FORCEKICK")
                .replace("%player%", target.getName())
        );

        Player playerObject = target.getPlayer();

        if (playerObject != null) {
            sendMessage(playerObject, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEKICK.FORCEKICKED")
                    .replace("%team%", pt.getName())
            );
        }
    }
}