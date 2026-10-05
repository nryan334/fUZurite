package me.keano.azurite.modules.teams.commands.citadel;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.teams.commands.citadel.args.CitadelReloadArg;
import me.keano.azurite.modules.teams.commands.citadel.args.CitadelRespawnArg;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CitadelCommand extends Command {

    public CitadelCommand(CommandManager manager) {
        super(
                manager,
                "citadel"
        );
        this.setPermissible("azurite.citadel");
        this.handleArguments(
                new CitadelReloadArg(manager),
                new CitadelRespawnArg(manager)
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("CITADEL_COMMAND.USAGE");
    }
}