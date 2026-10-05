package me.keano.azurite.modules.kits.commands.kitadmin.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
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
public class KitAdminSetAdminCooldownArg extends Argument {

    public KitAdminSetAdminCooldownArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "setcooldown",
                        "setseconds"
                )
        );
        this.setPermissible("azurite.kitadmin.setcooldown");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_SETCOOLDOWN.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        String name = args[0];
        Integer seconds = getInt(args[1]);
        Kit kit = getInstance().getKitManager().getKit(name);

        if (kit == null) {
            sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.NOT_FOUND")
                    .replace("%kit%", name)
            );
            return;
        }

        if (seconds == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[1])
            );
            return;
        }

        kit.setSeconds(seconds);
        kit.save();

        sendMessage(sender, getLanguageConfig().getString("KIT_ADMIN_COMMAND.KIT_SETCOOLDOWN.SET_COOLDOWN")
                .replace("%kit%", kit.getName())
                .replace("%seconds%", String.valueOf(seconds))
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