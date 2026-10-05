package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class HelpCommand extends Command {

    public HelpCommand(CommandManager manager) {
        super(
                manager,
                "help"
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "?",
                "info"
        );
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (String s : getLanguageConfig().getStringList("HELP_COMMAND.HELP_MESSAGE")) {
            sendMessage(sender, s);
        }
    }
}