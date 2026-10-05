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
public class TeamAnnouncementArg extends Argument {

    public TeamAnnouncementArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "announcement",
                        "announce"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_ANNOUNCEMENT.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

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

        String announcement = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        boolean clear = announcement.equalsIgnoreCase("clear");

        if (announcement.trim().length() > Config.TEAM_ANNOUNCEMENT_MAX) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_ANNOUNCEMENT.TOO_LONG")
                    .replace("%amount%", String.valueOf(Config.TEAM_ANNOUNCEMENT_MAX))
            );
            return;
        }

        if (clear) {
            pt.setAnnouncement(null);
            pt.save();
            pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_ANNOUNCEMENT.CLEARED")
                    .replace("%player%", player.getName())
            );
            return;
        }

        pt.setAnnouncement(announcement);
        pt.save();
        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_ANNOUNCEMENT.SET_ANNOUNCEMENT")
                .replace("%player%", player.getName())
                .replace("%announcement%", announcement)
        );
    }
}