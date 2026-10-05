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
public class StaffCommand extends Command {

    public StaffCommand(CommandManager manager) {
        super(
                manager,
                "staff"
        );
        this.setPermissible("azurite.staff");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "mod",
                "modmode",
                "sm",
                "staffmode",
                "h",
                "mm"
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

        if (args.length == 1 && player.hasPermission("azurite.staff.other")) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[0])
                );
                return;
            }

            if (manager.isStaffEnabled(target)) {
                manager.disableStaff(target, true);
                sendMessage(target, getLanguageConfig().getString("STAFF_MODE.DISABLED_STAFF"));
                sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.DISABLED_STAFF_TARGET")
                        .replace("%player%", target.getName())
                );
                return;
            }

            manager.enableStaff(target, true);
            sendMessage(target, getLanguageConfig().getString("STAFF_MODE.ENABLED_STAFF"));
            sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.ENABLED_STAFF_TARGET")
                    .replace("%player%", target.getName())
            );
            return;
        }

        if (manager.isStaffEnabled(player)) {
            manager.disableStaff(player, true);
            sendMessage(sender, getLanguageConfig().getString("STAFF_MODE.DISABLED_STAFF"));
            return;
        }

        manager.enableStaff(player, true);
        player.sendMessage(getLanguageConfig().getString("STAFF_MODE.ENABLED_STAFF"));
    }
}