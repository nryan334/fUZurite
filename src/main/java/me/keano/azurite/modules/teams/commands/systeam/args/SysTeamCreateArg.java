package me.keano.azurite.modules.teams.commands.systeam.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.enums.MountainType;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.type.*;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SysTeamCreateArg extends Argument {

    public SysTeamCreateArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "create"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CREATE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        String name = args[0];
        TeamType type;

        // need to handle differently because the mountain types are in a different enum than TeamType
        if (args[1].equalsIgnoreCase("GLOWSTONE") || args[1].equalsIgnoreCase("ORE_MOUNTAIN")) {
            if (getInstance().getTeamManager().getTeam(name) != null) {
                sendMessage(sender, Config.TEAM_ALREADY_EXISTS
                        .replace("%team%", args[0])
                );
                return;
            }

            new MountainTeam(getInstance().getTeamManager(), name, MountainType.valueOf(args[1].toUpperCase())).save();
            sendMessage(sender, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CREATE.CREATED_TEAM")
                    .replace("%team%", args[0])
            );
            return;
        }

        try {

            type = TeamType.valueOf(args[1].toUpperCase());

        } catch (IllegalArgumentException e) {
            type = null;
        }

        if (type == TeamType.MOUNTAIN) type = null;

        if (type == null || type == TeamType.PLAYER || type == TeamType.WARZONE || type == TeamType.WILDERNESS) {
            sendMessage(sender, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CREATE.TYPE_INVALID")
                    .replace("%type%", args[1])
            );
            return;
        }

        if (getInstance().getTeamManager().getTeam(name) != null) {
            sendMessage(sender, Config.TEAM_ALREADY_EXISTS
                    .replace("%team%", args[0])
            );
            return;
        }

        Team team = createTeam(name, type);
        if (team != null) team.save();
        sendMessage(sender, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CREATE.CREATED_TEAM")
                .replace("%team%", args[0])
        );
    }

    private Team createTeam(String name, TeamType type) {
        TeamManager manager = getInstance().getTeamManager();

        switch (type) {
            case SAFEZONE:
                return new SafezoneTeam(manager, name);

            case ROAD:
                return new RoadTeam(manager, name);

            case EVENT:
                return new EventTeam(manager, name);

            case CITADEL:
                return new CitadelTeam(manager, name);

            case CONQUEST:
                return new ConquestTeam(manager, name);
        }

        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 2) {
            String string = args[args.length - 1];
            List<String> tab = Arrays.stream(TeamType.values())
                    .filter(type -> type != TeamType.PLAYER && type != TeamType.WILDERNESS && type != TeamType.WARZONE && type != TeamType.MOUNTAIN)
                    .map(TeamType::name)
                    .collect(Collectors.toList());
            tab.add("GLOWSTONE");
            tab.add("ORE_MOUNTAIN");
            return tab.stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}