package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamForceRenameArg extends Argument {

    public TeamForceRenameArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "forcerename"
                )
        );
        this.setPermissible("azurite.team.forcerename");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCE_RENAME.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);
        String name = args[1];

        if (pt == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (pt.getName().equals(name)) {
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCE_RENAME.ALREADY_NAME")
                    .replace("%name%", name)
            );
            return;
        }

        if (getInstance().getTeamManager().getTeam(name) != null) {
            sendMessage(sender, Config.TEAM_ALREADY_EXISTS
                    .replace("%team%", name)
            );
            return;
        }

        if (Utils.isNotAlphanumeric(name)) {
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCE_RENAME.TEAM_RENAME.NOT_ALPHANUMERICAL"));
            return;
        }

        String oldName = pt.getName();
        getInstance().getTeamManager().getStringTeams().remove(pt.getName());
        getInstance().getTeamManager().getStringTeams().put(name, pt);

        pt.setName(name);
        pt.save();

        sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCE_RENAME.RENAMED")
                .replace("%team%", oldName)
                .replace("%newname%", name)
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