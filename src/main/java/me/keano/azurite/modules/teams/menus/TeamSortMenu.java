package me.keano.azurite.modules.teams.menus;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.settings.TeamListSetting;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamSortMenu extends Menu {

    private final List<TeamListSetting> settings;

    public TeamSortMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getLanguageConfig().getString("TEAM_COMMAND.TEAM_SORT.TITLE"),
                manager.getLanguageConfig().getInt("TEAM_COMMAND.TEAM_SORT.SIZE"),
                false
        );
        this.settings = Arrays.asList(TeamListSetting.values());
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        TeamListSetting currentSetting = user.getTeamListSetting();

        buttons.put(getLanguageConfig().getInt("TEAM_COMMAND.TEAM_SORT.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                user.setTeamListSetting(getSetting(currentSetting));
                update(); // We can update on click, not using a task which would be better.
            }

            @Override
            public ItemStack getItemStack() {
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getLanguageConfig()
                        .getString("TEAM_COMMAND.TEAM_SORT.MATERIAL")))
                        .setName(getLanguageConfig().getString("TEAM_COMMAND.TEAM_SORT.NAME"));

                for (TeamListSetting setting : settings) {
                    builder.addLoreLine(currentSetting == setting ?
                            getLanguageConfig().getString("TEAM_COMMAND.TEAM_SORT.POINTER") + getLanguageConfig().getString(setting.getConfigPath()) :
                            getLanguageConfig().getString(setting.getConfigPath())
                    );
                }

                return builder.toItemStack();
            }
        });

        return buttons;
    }

    public TeamListSetting getSetting(TeamListSetting listSetting) {
        int indexOf = settings.indexOf(listSetting);

        if (indexOf == settings.size() - 1)
            return settings.get(0);

        return settings.get(indexOf + 1);
    }
}