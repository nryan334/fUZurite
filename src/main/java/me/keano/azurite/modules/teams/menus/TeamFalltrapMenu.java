package me.keano.azurite.modules.teams.menus;

import lombok.Getter;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.task.FalltrapTask;
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
public class TeamFalltrapMenu extends Menu {

    private final List<FalltrapData> data;
    private final Claim claim;

    public TeamFalltrapMenu(MenuManager manager, Player player, Claim claim) {
        super(
                manager,
                player,
                manager.getTeamConfig().getString("FALLTRAP_CONFIG.FALLTRAP_MENU.TITLE"),
                manager.getTeamConfig().getInt("FALLTRAP_CONFIG.FALLTRAP_MENU.SIZE"),
                false
        );
        this.data = new ArrayList<>();
        this.claim = claim;
        this.load();
    }

    private void load() {
        for (String s : getTeamConfig().getStringList("FALLTRAP_CONFIG.FALLTRAP_MENU.FALLTRAP_WALLS")) {
            String[] split = s.split(", ");

            if (split[0].equalsIgnoreCase("NONE")) {
                data.add(new FalltrapData(
                        new ItemBuilder(Material.QUARTZ).setName(split[2]).toItemStack(),
                        null
                ));
                continue;
            }

            Material material = ItemUtils.getMat(split[0]);
            int number = Integer.parseInt(split[1]);
            data.add(new FalltrapData(
                    new ItemBuilder(material).data(getManager(), number).setName(split[2]).toItemStack(),
                    new ItemBuilder(material).data(getManager(), number).toItemStack()
            ));
        }
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int i = 1; i <= data.size(); i++) {
            FalltrapData trap = data.get(i - 1);
            buttons.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
                    e.setCancelled(true);

                    if (user.getFalltrapTokens() <= 0) {
                        player.closeInventory();
                        player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_FALLTRAP.NOT_ENOUGH_TOKENS"));
                        return;
                    }

                    player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_FALLTRAP.STARTED_PROCESS"));
                    player.closeInventory();
                    user.setFalltrapTokens(user.getFalltrapTokens() - 1);
                    user.save();
                    PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
                    pt.getFalltrapTasks().add(new FalltrapTask(getManager(), player.getUniqueId(), claim, trap.getActualItem()));
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
    private static class FalltrapData {

        private final ItemStack menuItem;
        private final ItemStack actualItem;

        public FalltrapData(ItemStack menuItem, ItemStack actualItem) {
            this.menuItem = menuItem;
            this.actualItem = actualItem;
        }
    }
}