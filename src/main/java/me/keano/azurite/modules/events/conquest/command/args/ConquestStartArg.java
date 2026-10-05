package me.keano.azurite.modules.events.conquest.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.events.conquest.extra.ConquestType;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ConquestStartArg extends Argument {

    public ConquestStartArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "start"
                )
        );
        this.setPermissible("azurite.conquest.start");
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Conquest conquest = getInstance().getConquestManager().getConquest();

        if (conquest.isActive()) {
            sendMessage(sender, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_START.ALREADY_ACTIVE"));
            return;
        }

        for (ConquestType type : ConquestType.values()) {
            if (!conquest.getCapzones().containsKey(type)) {
                sendMessage(sender, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_START.CAPZONES_NOT_CLAIMED")
                        .replace("%color%", type.getColor().toString())
                        .replace("%capzone%", type.getName())
                );
                return;
            }
        }

        conquest.start();
        sendMessage(sender, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_START.STARTED"));

        for (String s : getLanguageConfig().getStringList("CONQUEST_EVENTS.BROADCAST_START")) {
            Bukkit.broadcastMessage(s
                    .replace("%maxpoints%", String.valueOf(Config.CONQUEST_POINTS_CAPTURE))
            );
        }
    }
}