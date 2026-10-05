package me.keano.azurite.modules.teams.commands.systeam.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SysTeamListArg extends Argument {

    public SysTeamListArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "list",
                        "teams"
                )
        );
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (String s : getLanguageConfig().getStringList("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_LIST.LIST")) {
            if (!s.equalsIgnoreCase("%systemteams%")) {
                sendMessage(sender, s);
                continue;
            }

            for (Team team : getInstance().getTeamManager().getSystemTeams().values()) {
                sendMessage(sender, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_LIST.TEAM_FORMAT")
                        .replace("%team%", (sender instanceof Player ? team.getDisplayName((Player) sender) : team.getName()))
                        .replace("%hq%", team.getHQFormatted())
                );
            }
        }
    }
}