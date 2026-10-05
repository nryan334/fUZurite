package me.keano.azurite.modules.staff.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SilentViewMenu extends Menu {

    private final Inventory viewing;

    public SilentViewMenu(MenuManager manager, Player player, Inventory viewing) {
        super(
                manager,
                player,
                manager.getConfig().getString("STAFF_MODE.SILENT_VIEW_TITLE"),
                true, // Update the items
                viewing
        );
        this.viewing = viewing;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        getInventory().setContents(viewing.getContents());
        return buttons;
    }
}
