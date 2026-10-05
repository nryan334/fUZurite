package me.keano.azurite.modules.kits.commands.kitadmin.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.kits.Kit;
import org.bukkit.command.CommandSender;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KitAdminListArg extends Argument {

    public KitAdminListArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "list"
                )
        );
        this.setPermissible("azurite.kitadmin.list");
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (String s : getLanguageConfig().getStringList("KIT_ADMIN_COMMAND.KIT_LIST.KITS_LIST")) {
            if (!s.equalsIgnoreCase("%kits%")) {
                sendMessage(sender, s);
                continue;
            }

            for (Kit kit : getInstance().getKitManager().getKits().values()) {
                sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_LIST.KITS_FORMAT")
                        .replace("%kit%", kit.getName())
                        .replace("%seconds%", String.valueOf(kit.getSeconds()))
                );
            }
        }
    }
}