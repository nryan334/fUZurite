package me.keano.azurite.modules.events.king.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KingEndArg extends Argument {

    public KingEndArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "stop",
                        "end"
                )
        );
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!getInstance().getKingManager().isActive()) {
            sendMessage(sender, getLanguageConfig().getString("KING_COMMAND.KING_END.NOT_ACTIVE"));
            return;
        }

        getInstance().getKingManager().stopKing(true);
    }
}