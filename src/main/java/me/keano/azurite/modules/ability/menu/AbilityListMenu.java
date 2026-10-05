package me.keano.azurite.modules.ability.menu;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.framework.menu.paginated.PaginatedMenu;
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
public class AbilityListMenu extends PaginatedMenu {

    public AbilityListMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getAbilitiesConfig().getString("ABILITY_MENUS.LIST_TITLE"),
                manager.getAbilitiesConfig().getInt("ABILITY_MENUS.LIST_SIZE"),
                false
        );
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int slot = 1;

        for (Ability ability : getInstance().getAbilityManager().getAbilities().values()) {
            if (!ability.isEnabled()) continue;

            buttons.put(slot, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if (!(e.getWhoClicked() instanceof Player)) return;

                    Player clicked = (Player) e.getWhoClicked();

                    if (clicked.hasPermission("azurite.abilitymenu.click")) {
                        clicked.getInventory().addItem(ability.getItem().clone());
                    }

                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return ability.getItem().clone();
                }
            });
            slot++;
        }

        return buttons;
    }
}