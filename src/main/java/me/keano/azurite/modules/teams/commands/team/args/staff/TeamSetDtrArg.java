package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamSetDtrArg extends Argument {

    public TeamSetDtrArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "setdtr"
                )
        );
        this.setPermissible("azurite.team.setdtr");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_SETDTR.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);
        Double amount = getDouble(args[1]);

        if (pt == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (amount == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[1])
            );
            return;
        }

        pt.setDtr(amount);
        pt.save();

        sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_SETDTR.SETDTR")
                .replace("%team%", args[0])
                .replace("%amount%", args[1])
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