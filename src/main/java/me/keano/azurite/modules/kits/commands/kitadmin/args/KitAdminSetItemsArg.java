package me.keano.azurite.modules.kits.commands.kitadmin.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.kits.Kit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KitAdminSetItemsArg extends Argument {

    public KitAdminSetItemsArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "setitems",
                        "edit"
                )
        );
        this.setPermissible("azurite.kitadmin.setitems");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_SETITEMS.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        String name = args[0];
        Kit kit = getInstance().getKitManager().getKit(name);

        if (kit == null) {
            sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.NOT_FOUND")
                    .replace("%kit%", name)
            );
            return;
        }

        kit.update(player.getInventory().getContents(), player.getInventory().getArmorContents());
        kit.save();

        sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_SETITEMS.SET_ITEMS")
                .replace("%kit%", kit.getName())
        );
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