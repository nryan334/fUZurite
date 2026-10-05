package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.Staff;
import me.keano.azurite.modules.staff.StaffManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class HideStaffCommand extends Command {

    public HideStaffCommand(CommandManager manager) {
        super(
                manager,
                "hidestaff"
        );
        this.setPermissible("azurite.hidestaff");
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "hs"
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
        StaffManager staffManager = getInstance().getStaffManager();

        if (staffManager.isHideStaff(player)) {
            // Remove
            staffManager.getHideStaff().remove(player.getUniqueId());

            // Show if they are only vanished
            for (Staff staff : staffManager.getStaffMembers().values()) {
                if (staffManager.isVanished(staff.getPlayer())) {
                    player.showPlayer(staff.getPlayer());
                }
            }

            sendMessage(sender, getLanguageConfig().getString("HIDE_STAFF_COMMAND.DISABLED"));
            return;
        }

        staffManager.getHideStaff().add(player.getUniqueId());
        sendMessage(sender, getLanguageConfig().getString("HIDE_STAFF_COMMAND.ENABLED"));

        // Hide if they are only vanished
        for (Staff staff : staffManager.getStaffMembers().values()) {
            if (staffManager.isVanished(staff.getPlayer())) {
                player.hidePlayer(staff.getPlayer());
            }
        }
    }
}