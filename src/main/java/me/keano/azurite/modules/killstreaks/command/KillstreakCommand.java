package me.keano.azurite.modules.killstreaks.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.killstreaks.Killstreak;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KillstreakCommand extends Command {

    public KillstreakCommand(CommandManager manager) {
        super(
                manager,
                "killstreak"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("killstreaks");
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (String s : getLanguageConfig().getStringList("KILLSTREAK_COMMAND.KILLSTREAKS")) {
            if (!s.equalsIgnoreCase("%killstreaks%")) {
                sendMessage(sender, s);
                continue;
            }

            for (Map.Entry<Integer, Killstreak> killstreak : getInstance().getKillstreakManager().getKillstreaks().entrySet()) {
                sendMessage(sender, getLanguageConfig().getString("KILLSTREAK_COMMAND.KILLSTREAK_FORMAT")
                        .replace("%killstreak%", killstreak.getValue().getName())
                        .replace("%kills%", String.valueOf(killstreak.getKey()))
                );
            }
        }
    }
}