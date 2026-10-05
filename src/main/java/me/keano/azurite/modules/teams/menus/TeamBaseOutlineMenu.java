package me.keano.azurite.modules.teams.menus;

import lombok.Getter;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.task.BaseTask;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
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
public class TeamBaseOutlineMenu extends Menu {

    private final List<OutlineData> data;
    private final Claim claim;
    private final ItemStack wallItem;

    public TeamBaseOutlineMenu(MenuManager manager, Player player, Claim claim, ItemStack wallItem) {
        super(
                manager,
                player,
                manager.getTeamConfig().getString("BASE_CONFIG.BASE_OUTLINE_MENU.TITLE"),
                manager.getTeamConfig().getInt("BASE_CONFIG.BASE_OUTLINE_MENU.SIZE"),
                false
        );
        this.data = new ArrayList<>();
        this.claim = claim;
        this.wallItem = wallItem;
        this.load();
    }

    private void load() {
        for (String s : getTeamConfig().getStringList("BASE_CONFIG.BASE_OUTLINE_MENU.BASE_OUTLINES")) {
            String[] split = s.split(", ");
            Material material = ItemUtils.getMat(split[0]);
            short number = (short) Integer.parseInt(split[1]);
            data.add(new OutlineData(
                    new ItemBuilder(material).data(getManager(), number).setName(split[2]).toItemStack(),
                    new ItemBuilder(material).data(getManager(), number).toItemStack()
            ));
        }
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int i = 1; i <= data.size(); i++) {
            OutlineData trap = data.get(i - 1);
            buttons.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
                    e.setCancelled(true);

                    if (user.getBaseTokens() <= 0) {
                        player.closeInventory();
                        player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_BASE.NOT_ENOUGH_TOKENS"));
                        return;
                    }

                    player.closeInventory();
                    player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_BASE.STARTED_PROCESS"));
                    user.setBaseTokens(user.getBaseTokens() - 1);
                    user.save();
                    PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
                    pt.getBaseTasks().add(new BaseTask(getManager(), player.getUniqueId(), claim, wallItem, trap.getActualItem()));
                    pt.save();
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
    private static class OutlineData {

        private final ItemStack menuItem;
        private final ItemStack actualItem;

        public OutlineData(ItemStack menuItem, ItemStack actualItem) {
            this.menuItem = menuItem;
            this.actualItem = actualItem;
        }
    }
}