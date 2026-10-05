package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamInfoArg extends Argument {

    public TeamInfoArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "info",
                        "who",
                        "i",
                        "show"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_INFO.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

                if (pt != null) {
                    pt.sendTeamStats(player);
                    return;
                }

                sendMessage(sender, Config.NOT_IN_TEAM);
                return;
            }

            sendUsage(sender);
            return;
        }

        User user = getInstance().getUserManager().getByName(args[0]);
        PlayerTeam ptPlayer = (user != null ? getInstance().getTeamManager().getByPlayer(user.getUniqueID()) : null);
        Team pt = getInstance().getTeamManager().getTeam(args[0]);

        if (ptPlayer == null && pt == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (ptPlayer != null) {
            ptPlayer.sendTeamStats(sender);
        }

        if (pt != null) {
            pt.sendTeamStats(sender);
        }
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