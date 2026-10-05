package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.extra.ManageTeamData;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ManageTeamCommand extends Command {

    public ManageTeamCommand(CommandManager manager) {
        super(
                manager,
                "manageteam"
        );
        this.setPermissible("azurite.manageteam");
        this.completions.add(new TabCompletion(Arrays.asList("regen", "rename", "freeze", "dtrregen", "balance", "dtr"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("MANAGE_TEAM_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        TeamManager teamManager = getInstance().getTeamManager();
        PlayerTeam target = teamManager.getByPlayerOrTeam(args[1]);
        ManageTeamData.ManageTeamType manageType = null;

        if (target == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[1])
            );
            return;
        }

        switch (args[0].toLowerCase()) {
            case "freeze":
            case "dtrregen":
            case "regen":
                manageType = ManageTeamData.ManageTeamType.REGEN;
                sendMessage(sender, getLanguageConfig().getString("MANAGE_TEAM_COMMAND.MANAGE_REGEN")
                        .replace("%team%", target.getName())
                );
                break;

            case "rename":
                manageType = ManageTeamData.ManageTeamType.RENAME;
                sendMessage(sender, getLanguageConfig().getString("MANAGE_TEAM_COMMAND.MANAGE_RENAME")
                        .replace("%team%", target.getName())
                );
                break;

            case "balance":
                manageType = ManageTeamData.ManageTeamType.BALANCE;
                sendMessage(sender, getLanguageConfig().getString("MANAGE_TEAM_COMMAND.MANAGE_BALANCE")
                        .replace("%team%", target.getName())
                );
                break;

            case "dtr":
                manageType = ManageTeamData.ManageTeamType.DTR;
                sendMessage(sender, getLanguageConfig().getString("MANAGE_TEAM_COMMAND.MANAGE_DTR")
                        .replace("%team%", target.getName())
                );
                break;

            case "points":
                manageType = ManageTeamData.ManageTeamType.POINTS;
                sendMessage(sender, getLanguageConfig().getString("MANAGE_TEAM_COMMAND.MANAGE_POINTS")
                        .replace("%team%", target.getName())
                );
                break;
        }

        if (manageType != null) {
            teamManager.getManageTeams().put(player.getUniqueId(), new ManageTeamData(manageType, target.getUniqueID()));
            return;
        }

        sendUsage(sender);
    }
}