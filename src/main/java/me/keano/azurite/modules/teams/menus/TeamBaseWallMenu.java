package me.keano.azurite.modules.teams.menus;

import lombok.Getter;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamBaseWallMenu extends Menu {

    private final List<WallData> data;
    private final Claim claim;

    public TeamBaseWallMenu(MenuManager manager, Player player, Claim claim) {
        super(
                manager,
                player,
                manager.getTeamConfig().getString("BASE_CONFIG.BASE_WALL_MENU.TITLE"),
                manager.getTeamConfig().getInt("BASE_CONFIG.BASE_WALL_MENU.SIZE"),
                false
        );
        this.data = new ArrayList<>();
        this.claim = claim;
        this.load();
    }

    private void load() {
        for (String s : getTeamConfig().getStringList("BASE_CONFIG.BASE_WALL_MENU.BASE_WALLS")) {
            String[] split = s.split(", ");
            Material material = ItemUtils.getMat(split[0]);
            short number = (short) Integer.parseInt(split[1]);
            data.add(new WallData(
                    new ItemBuilder(material).data(getManager(), number).setName(split[2]).toItemStack(),
                    new ItemBuilder(material).data(getManager(), number).toItemStack()
            ));
        }
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int i = 1; i <= data.size(); i++) {
            WallData trap = data.get(i - 1);
            buttons.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                    player.closeInventory();
                    new TeamBaseHeightMenu(getInstance().getMenuManager(), player, trap.getActualItem(), claim).open();
                }

                @Override
                public ItemStack getItemStack() {
                    return trap.getMenuItem();
                }
            });
        }

        return buttons;
    }

    @Getter
    private static class WallData {

        private final ItemStack menuItem;
        private final ItemStack actualItem;

        public WallData(ItemStack menuItem, ItemStack actualItem) {
            this.menuItem = menuItem;
            this.actualItem = actualItem;
        }
    }
}