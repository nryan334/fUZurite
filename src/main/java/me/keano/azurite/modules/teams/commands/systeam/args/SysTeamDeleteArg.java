package me.keano.azurite.modules.teams.commands.systeam.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SysTeamDeleteArg extends Argument {

    public SysTeamDeleteArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "delete"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_DELETE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Team team = getInstance().getTeamManager().getTeam(args[0]);

        if (team == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        team.delete();

        for (Claim claim : team.getClaims()) {
            getInstance().getTeamManager().getClaimManager().deleteClaim(claim);
        }

        sendMessage(sender, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_DELETE.DELETED_TEAM")
                .replace("%team%", args[0])
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getSystemTeams().values()
                    .stream()
                    .map(Team::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}