package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamForceDisbandArg extends Argument {

    public TeamForceDisbandArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "forcedisband",
                        "staffdisband"
                )
        );
        this.setPermissible("azurite.team.forcedisband");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEDISBAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        PlayerTeam pt = getInstance().getTeamManager().getPlayerTeam(args[0]);

        if (pt == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        pt.broadcast(getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEDISBAND.BROADCAST_DISBAND"));
        pt.disband();
        Bukkit.broadcastMessage(getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEDISBAND.BROADCAST_DISBAND_ALL")
                .replace("%team%", pt.getName())
                .replace("%player%", sender.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getTeams().values()
                    .stream()
                    .filter(team -> team instanceof PlayerTeam)
                    .map(Team::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}