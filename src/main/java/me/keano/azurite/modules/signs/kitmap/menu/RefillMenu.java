package me.keano.azurite.modules.signs.kitmap.menu;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RefillMenu extends Menu {

    public RefillMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getConfig().getString("SIGNS_CONFIG.REFILL_SIGN.MENU_TITLE"),
                manager.getConfig().getInt("SIGNS_CONFIG.REFILL_SIGN.MENU_SIZE"),
                false
        );
        this.setAllowInteract(true);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int i = 1; i <= Config.REFILL_SIGN.length; i++) {
            int copy = i;
            buttons.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    // Empty
                }

                @Override
                public ItemStack getItemStack() {
                    return Config.REFILL_SIGN[copy - 1].clone();
                }
            });
        }

        return buttons;
    }
}