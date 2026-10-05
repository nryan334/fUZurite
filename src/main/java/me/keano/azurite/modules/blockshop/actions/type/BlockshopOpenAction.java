package me.keano.azurite.modules.blockshop.actions.type;

import me.keano.azurite.modules.blockshop.BlockshopManager;
import me.keano.azurite.modules.blockshop.actions.BlockshopAction;
import me.keano.azurite.modules.blockshop.menu.BlockshopMenu;
import me.keano.azurite.modules.blockshop.menu.BuyShopMenu;
import me.keano.azurite.modules.blockshop.menu.CustomBlockshopMenu;
import me.keano.azurite.modules.blockshop.menu.SellShopMenu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BlockshopOpenAction extends BlockshopAction {

    private final String menu;

    public BlockshopOpenAction(BlockshopManager manager, String path) {
        super(manager, path);
        this.menu = getBlockshopConfig().getString(path + "NAME").toUpperCase();
    }

    @Override
    public boolean handleClick(Player player, InventoryClickEvent event) {
        MenuManager menuManager = getInstance().getMenuManager();

        switch (menu) {
            case "BLOCKSHOP":
                new BlockshopMenu(menuManager, player).open();
                return true;

            case "BUYSHOP":
                new BuyShopMenu(menuManager, player).open();
                return true;

            case "SELLSHOP":
                new SellShopMenu(menuManager, player).open();
                return true;
        }

        new CustomBlockshopMenu(menuManager, player, "CUSTOM_BLOCKSHOP_MENUS." + menu).open();
        return true;
    }
}