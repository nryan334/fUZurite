package me.keano.azurite.modules.events.king.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.king.command.args.KingEndArg;
import me.keano.azurite.modules.events.king.command.args.KingStartArg;
import me.keano.azurite.modules.framework.commands.Command;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KingCommand extends Command {

    public KingCommand(CommandManager manager) {
        super(
                manager,
                "king"
        );
        this.setPermissible("azurite.ktk");
        this.handleArguments(
                new KingEndArg(manager),
                new KingStartArg(manager)
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "ktk"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("KING_COMMAND.USAGE");
    }
}