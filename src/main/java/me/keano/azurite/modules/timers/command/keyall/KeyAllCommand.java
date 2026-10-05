package me.keano.azurite.modules.timers.command.keyall;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.timers.command.keyall.args.KeyAllCreateArg;
import me.keano.azurite.modules.timers.command.keyall.args.KeyAllDeleteArg;
import me.keano.azurite.modules.timers.command.keyall.args.KeyAllListArg;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KeyAllCommand extends Command {

    public KeyAllCommand(CommandManager manager) {
        super(
                manager,
                "keyall"
        );
        this.setPermissible("azurite.keyall");
        this.handleArguments(
                new KeyAllCreateArg(manager),
                new KeyAllDeleteArg(manager),
                new KeyAllListArg(manager)
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("KEY_ALL_COMMAND.USAGE");
    }
}