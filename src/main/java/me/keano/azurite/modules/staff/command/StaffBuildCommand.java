package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
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
public class StaffBuildCommand extends Command {

    public StaffBuildCommand(CommandManager manager) {
        super(
                manager,
                "staffbuild"
        );
        this.setPermissible("azurite.staffbuild");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
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

        if (!staffManager.isStaffEnabled(player)) {
            sendMessage(sender, getLanguageConfig().getString("STAFF_BUILD_COMMAND.NOT_IN_STAFF"));
            return;
        }

        if (staffManager.isStaffBuild(player)) {
            staffManager.getStaffBuild().remove(player.getUniqueId());
            sendMessage(sender, getLanguageConfig().getString("STAFF_BUILD_COMMAND.BUILD_DISABLED"));
            return;
        }

        staffManager.getStaffBuild().add(player.getUniqueId());
        sendMessage(sender, getLanguageConfig().getString("STAFF_BUILD_COMMAND.BUILD_ENABLED"));
    }
}