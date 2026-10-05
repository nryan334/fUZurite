package me.keano.azurite.modules.timers.command.timer;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.timers.command.timer.args.TimerAddArg;
import me.keano.azurite.modules.timers.command.timer.args.TimerRemoveArg;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TimerCommand extends Command {

    public TimerCommand(CommandManager manager) {
        super(
                manager,
                "timer"
        );

        this.setPermissible("azurite.timer");
        this.handleArguments(
                new TimerRemoveArg(manager),
                new TimerAddArg(manager)
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "timers",
                "cooldown"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("TIMER_COMMAND.USAGE");
    }
}