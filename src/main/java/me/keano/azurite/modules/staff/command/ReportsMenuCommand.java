package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.menu.ReportsMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ReportsMenuCommand extends Command {

    public ReportsMenuCommand(CommandManager manager) {
        super(
                manager,
                "reportsmenu"
        );
        this.setPermissible("azurite.reportsmenu");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "reports",
                "listreports"
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
        new ReportsMenu(getInstance().getMenuManager(), player).open();
    }
}