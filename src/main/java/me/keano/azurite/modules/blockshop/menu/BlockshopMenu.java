package me.keano.azurite.modules.blockshop.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BlockshopMenu extends Menu {

    public BlockshopMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getBlockshopConfig().getString("MAIN_MENU.TITLE"),
                manager.getBlockshopConfig().getInt("MAIN_MENU.SIZE"),
                true
        );
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> all = getInstance().getBlockshopManager().getAllButtons(player);
        all.putAll(getInstance().getBlockshopManager().getButtons(player, "MAIN_MENU.BUTTONS"));
        return all;
    }
}