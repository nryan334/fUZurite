package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.command.CommandSender;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothListArg extends Argument {

    public KothListArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "list"
                )
        );
        this.setPermissible("azurite.koth.list");
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (String s : getLanguageConfig().getStringList("KOTH_COMMAND.KOTH_LIST.LIST")) {
            if (!s.equalsIgnoreCase("%koths%")) {
                sendMessage(sender, s);
                continue;
            }

            for (Koth koth : getInstance().getKothManager().getKoths().values()) {
                sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_LIST.FORMAT")
                        .replace("%color%", koth.getColor())
                        .replace("%koth%", koth.getName())
                        .replace("%mins%", String.valueOf(koth.getMinutes() / (60 * 1000L)))
                );
            }
        }
    }
}