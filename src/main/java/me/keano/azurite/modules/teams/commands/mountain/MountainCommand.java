package me.keano.azurite.modules.teams.commands.mountain;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.teams.commands.mountain.args.MountainEditLootArg;
import me.keano.azurite.modules.teams.commands.mountain.args.MountainReloadArg;
import me.keano.azurite.modules.teams.commands.mountain.args.MountainRespawnArg;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MountainCommand extends Command {

    public MountainCommand(CommandManager manager) {
        super(
                manager,
                "mountain"
        );
        this.setPermissible("azurite.mountain");
        this.handleArguments(
                new MountainReloadArg(manager),
                new MountainRespawnArg(manager),
                new MountainEditLootArg(manager)
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "mountains",
                "adminglowstone",
                "adminmountains",
                "mt"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("MOUNTAIN_COMMAND.USAGE");
    }
}