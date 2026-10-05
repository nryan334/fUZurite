package me.keano.azurite.modules.teams.commands.team.args.co_leader;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamFriendlyFire extends Argument {

    public TeamFriendlyFire(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "friendlyfire",
                        "ff"
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

        if (!pt.checkRole(player, Role.CO_LEADER)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.CO_LEADER.getName())
            );
            return;
        }

        if (pt.isFriendlyFire()) {
            pt.setFriendlyFire(false);
            pt.save();
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FRIENDLY_FIRE.DISABLED"));
            return;
        }

        pt.setFriendlyFire(true);
        pt.save();
        sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FRIENDLY_FIRE.ENABLED"));
    }
}