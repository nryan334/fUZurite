package me.keano.azurite.modules.teams.commands.systeam.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
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
public class SysTeamUnclaimArg extends Argument {

    public SysTeamUnclaimArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "unclaim"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_UNCLAIM.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.INSUFFICIENT_PERM);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        Team team = getInstance().getTeamManager().getTeam(args[0]);
        Claim atPlayer = getInstance().getTeamManager().getClaimManager().getClaim(player.getLocation());

        if (team == null || !getInstance().getTeamManager().getSystemTeams().containsKey(team.getUniqueID())) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (atPlayer == null || atPlayer.getTeam() != team.getUniqueID()) {
            sendMessage(sender, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_UNCLAIM.NOT_IN_CLAIM")
                    .replace("%team%", team.getName())
            );
            return;
        }

        if (team.getHq() != null && atPlayer.contains(team.getHq())) team.setHq(null);

        getInstance().getTeamManager().getClaimManager().deleteClaim(atPlayer);
        team.getClaims().remove(atPlayer);
        team.save();
        player.sendMessage(getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_UNCLAIM.UNCLAIMED")
                .replace("%team%", team.getName())
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