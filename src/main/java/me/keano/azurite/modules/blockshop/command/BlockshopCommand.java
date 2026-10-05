package me.keano.azurite.modules.blockshop.command;

import me.keano.azurite.modules.blockshop.menu.BlockshopMenu;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BlockshopCommand extends Command {

    public BlockshopCommand(CommandManager manager) {
        super(
                manager,
                "blockshop"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "shop"
        );
    }

    @Override
    public List<String> usage() {
        return Collections.emptyList();
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

        new BlockshopMenu(getInstance().getMenuManager(), player).open();
    }
}