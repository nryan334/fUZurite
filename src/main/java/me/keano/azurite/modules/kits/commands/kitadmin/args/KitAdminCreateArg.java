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
public class KitAdminCreateArg extends Argument {

    public KitAdminCreateArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "create"
                )
        );
        this.setPermissible("azurite.kitadmin.create");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_CREATE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        String name = args[0];

        if (getInstance().getKitManager().getKit(name) != null) {
            sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_CREATE.ALREADY_EXISTS")
                    .replace("%kit%", name)
            );
            return;
        }

        Kit kit = new Kit(getInstance().getKitManager(), name, false);
        kit.save();

        sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_CREATE.CREATED")
                .replace("%kit%", name)
        );
    }
}