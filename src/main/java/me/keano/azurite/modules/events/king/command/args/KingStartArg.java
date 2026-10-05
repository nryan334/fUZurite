package me.keano.azurite.modules.events.king.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KingStartArg extends Argument {

    public KingStartArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "start"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KING_COMMAND.KING_START.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (getInstance().getKingManager().isActive()) {
            sendMessage(sender, getLanguageConfig().getString("KING_COMMAND.KING_START.ALREADY_ACTIVE"));
            return;
        }

        getInstance().getKingManager().startKing(target, reason);
    }
}