package me.keano.azurite.modules.events.conquest.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ConquestEndArg extends Argument {

    public ConquestEndArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "end",
                        "stop"
                )
        );
        this.setPermissible("azurite.conquest.end");
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Conquest conquest = getInstance().getConquestManager().getConquest();

        if (!conquest.isActive()) {
            sendMessage(sender, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_END.NOT_ACTIVE"));
            return;
        }

        conquest.end();
        sendMessage(sender, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_END.ENDED"));
    }
}