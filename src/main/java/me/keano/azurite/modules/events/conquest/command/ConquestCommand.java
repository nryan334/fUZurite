package me.keano.azurite.modules.events.conquest.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.conquest.command.args.ConquestClaimArg;
import me.keano.azurite.modules.events.conquest.command.args.ConquestEndArg;
import me.keano.azurite.modules.events.conquest.command.args.ConquestSetPointsArg;
import me.keano.azurite.modules.events.conquest.command.args.ConquestStartArg;
import me.keano.azurite.modules.framework.commands.Command;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ConquestCommand extends Command {

    public ConquestCommand(CommandManager manager) {
        super(
                manager,
                "conquest"
        );
        this.setPermissible("azurite.conquest");
        this.handleArguments(
                new ConquestStartArg(manager),
                new ConquestEndArg(manager),
                new ConquestClaimArg(manager),
                new ConquestSetPointsArg(manager)
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("CONQUEST_COMMAND.USAGE");
    }
}