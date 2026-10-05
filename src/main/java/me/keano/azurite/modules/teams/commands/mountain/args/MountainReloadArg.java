package me.keano.azurite.modules.teams.commands.mountain.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.MountainTeam;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MountainReloadArg extends Argument {

    public MountainReloadArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "reload"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("MOUNTAIN_COMMAND.MOUNTAIN_RELOAD.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        MountainTeam mt = getInstance().getTeamManager().getMountainTeam(args[0]);

        if (mt == null) {
            sendMessage(sender, getLanguageConfig().getString("MOUNTAIN_COMMAND.MOUNTAIN_NOT_FOUND")
                    .replace("%mountain%", args[0])
            );
            return;
        }

        mt.saveBlocks();
        sendMessage(sender, getLanguageConfig().getString("MOUNTAIN_COMMAND.MOUNTAIN_RELOAD.RELOADED")
                .replace("%mountain%", mt.getName()));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getTeams().values()
                    .stream()
                    .filter(t -> t instanceof MountainTeam)
                    .map(Team::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}