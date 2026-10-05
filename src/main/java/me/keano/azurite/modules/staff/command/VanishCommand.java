package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.StaffManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class VanishCommand extends Command {

    public VanishCommand(CommandManager manager) {
        super(
                manager,
                "vanish"
        );
        this.setPermissible("azurite.vanish");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "v",
                "vanished"
        );
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        StaffManager manager = getInstance().getStaffManager();


        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[0])
                );
                return;
            }

            if (manager.isVanished(target)) {
                manager.disableVanish(target);
                sendMessage(target, getLanguageConfig().getString("STAFF_MODE.DISABLED_VANISH"));
                sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.DISABLED_VANISH_TARGET")
                        .replace("%player%", target.getName())
                );
                return;
            }

            manager.enableVanish(target);
            sendMessage(target, getLanguageConfig().getString("STAFF_MODE.ENABLED_VANISH"));
            sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.ENABLED_VANISH_TARGET")
                    .replace("%player%", target.getName())
            );
            return;
        }

        if (manager.isVanished(player)) {
            manager.disableVanish(player);
            sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.DISABLED_VANISH"));
            return;
        }

        manager.enableVanish(player);
        sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.ENABLED_VANISH"));
    }
}