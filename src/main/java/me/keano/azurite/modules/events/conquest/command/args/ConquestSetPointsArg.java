package me.keano.azurite.modules.events.conquest.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ConquestSetPointsArg extends Argument {

    public ConquestSetPointsArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "setpoints",
                        "managepoints"
                )
        );
        this.setPermissible("azurite.conquest.setpoints");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_SETPOINTS.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);
        Conquest conquest = getInstance().getConquestManager().getConquest();
        Integer amount = getInt(args[1]);

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

        conquest.getPoints().put(pt.getUniqueID(), amount);
        conquest.sortPoints();
        sendMessage(sender, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_SETPOINTS.SET_POINTS")
                .replace("%team%", pt.getName())
                .replace("%amount%", String.valueOf(amount))
        );
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