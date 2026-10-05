package me.keano.azurite.modules.blockshop.command;

import me.keano.azurite.modules.blockshop.menu.SellShopMenu;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SellShopCommand extends Command {

    public SellShopCommand(CommandManager manager) {
        super(
                manager,
                "sellshop"
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "sell",
                "sellblocks"
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

        if (getInstance().getBlockshopManager().cannotUseShop(player)) {
            return;
        }

        new SellShopMenu(getInstance().getMenuManager(), player).open();
    }
}