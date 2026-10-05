package me.keano.azurite.modules.kits.commands.kitadmin.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.kits.Kit;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KitAdminApplyArg extends Argument {

    public KitAdminApplyArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "apply"
                )
        );
        this.setPermissible("azurite.kitadmin.apply");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_APPLY.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        String name = args[1];
        Kit kit = getInstance().getKitManager().getKit(name);

        if (kit == null) {
            sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.NOT_FOUND")
                    .replace("%kit%", name)
            );
            return;
        }

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        kit.equip(target);
        sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_APPLY.APPLIED")
                .replace("%kit%", kit.getName())
                .replace("%player%", target.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 2) {
            String string = args[args.length - 1];
            return getInstance().getKitManager().getKits().keySet()
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}