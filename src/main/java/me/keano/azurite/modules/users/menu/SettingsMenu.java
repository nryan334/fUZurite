package me.keano.azurite.modules.users.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.settings.UserSetting;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
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
public class SettingsMenu extends Menu {

    public SettingsMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getLanguageConfig().getString("SETTINGS_COMMAND.TITLE"),
                manager.getLanguageConfig().getInt("SETTINGS_COMMAND.SIZE"),
                false
        );
        this.setFillEnabled(getLanguageConfig().getBoolean("SETTINGS_COMMAND.FILLER.ENABLED"));
        this.setFiller(new ItemBuilder(ItemUtils.getMat(getLanguageConfig().getString("SETTINGS_COMMAND.FILLER.MATERIAL")))
                .setName(getLanguageConfig().getString("SETTINGS_COMMAND.FILLER.NAME"))
                .setLore(getLanguageConfig().getStringList("SETTINGS_COMMAND.FILLER.LORE"))
                .data(getManager(), getLanguageConfig().getInt("SETTINGS_COMMAND.FILLER.DATA"))
                .toItemStack());
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        for (String key : getLanguageConfig().getConfigurationSection("SETTINGS_COMMAND.ITEMS").getKeys(false)) {
            UserSetting setting = getSetting(key);
            String converted = (setting != null ? key + "." + convert(user, setting) : key);
            String path = "SETTINGS_COMMAND.ITEMS." + converted;
            buttons.put(getLanguageConfig().getInt(path + ".SLOT"), new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);

                    if (getLanguageConfig().contains(path + ".SOUND")) {
                        getManager().playSound(player, getLanguageConfig().getString(path + ".SOUND"), false);
                    }

                    if (setting != null) {
                        switch (setting) {
                            case SCOREBOARD:
                                user.setScoreboard(!user.isScoreboard());
                                break;

                            case SCOREBOARD_CLAIM:
                                user.setScoreboardClaim(!user.isScoreboardClaim());
                                break;

                            case PUBLIC_CHAT:
                                user.setPublicChat(!user.isPublicChat());
                                break;

                            case COBBLE:
                                user.setCobblePickup(!user.isCobblePickup());
                                break;

                            case FOUND_DIAMOND:
                                user.setFoundDiamondAlerts(!user.isFoundDiamondAlerts());
                                break;

                            case DEATH_MESSAGES:
                                user.setDeathMessages(!user.isDeathMessages());
                                break;

                            case LUNAR_NAMETAGS:
                                user.setLunarNametags(!user.isLunarNametags());
                                break;
                        }

                        update(); // Update the menu
                    }
                }

                @Override
                public ItemStack getItemStack() {
                    return new ItemBuilder(ItemUtils.getMat(getLanguageConfig().getString(path + ".MATERIAL")))
                            .setName(getLanguageConfig().getString(path + ".NAME"))
                            .data(getManager(), getLanguageConfig().getInt(path + ".DATA"))
                            .setLore(getLanguageConfig().getStringList(path + ".LORE")).toItemStack();
                }
            });
        }

        return buttons;
    }

    private String convert(User user, UserSetting setting) {
        switch (setting) {
            case SCOREBOARD:
                return convertBoolean(user.isScoreboard());

            case SCOREBOARD_CLAIM:
                return convertBoolean(user.isScoreboardClaim());

            case PUBLIC_CHAT:
                return convertBoolean(user.isPublicChat());

            case COBBLE:
                return convertBoolean(user.isCobblePickup());

            case FOUND_DIAMOND:
                return convertBoolean(user.isFoundDiamondAlerts());

            case LUNAR_NAMETAGS:
                return convertBoolean(user.isLunarNametags());

            case DEATH_MESSAGES:
                return convertBoolean(user.isDeathMessages());
        }
        return "";
    }

    private UserSetting getSetting(String name) {
        try {

            return UserSetting.valueOf(name);

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String convertBoolean(boolean bool) {
        return (bool ? "ENABLED" : "DISABLED");
    }
}