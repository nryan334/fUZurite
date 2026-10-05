package me.keano.azurite.modules.kits.commands.kitadmin.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.kits.Kit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KitAdminSetNameArg extends Argument {

    public KitAdminSetNameArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "setname",
                        "updatename"
                )
        );
        this.setPermissible("azurite.kitadmin.setname");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_SETNAME.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Kit kit = getInstance().getKitManager().getKit(args[0]);
        String newName = args[1];

        if (kit == null) {
            sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.NOT_FOUND")
                    .replace("%kit%", args[0])
            );
            return;
        }

        sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_SETNAME.SET_NAME")
                .replace("%oldName%", kit.getName())
                .replace("%newName%", newName)
        );

        getInstance().getKitManager().getKits().remove(kit.getName()); // remove the old name
        getInstance().getKitManager().getKits().put(newName, kit); // put the new name

        kit.setName(newName);
        kit.save();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getKitManager().getKits().keySet()
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}