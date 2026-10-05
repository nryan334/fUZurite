package me.keano.azurite.modules.blockshop;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.blockshop.actions.BlockshopAction;
import me.keano.azurite.modules.blockshop.actions.type.*;
import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.spawners.Spawner;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.teams.type.WarzoneTeam;
import me.keano.azurite.modules.teams.type.WildernessTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BlockshopManager extends Manager {

    public BlockshopManager(HCF instance) {
        super(instance);
    }

    public boolean cannotUseShop(Player player) {
        SOTWManager sotwManager = getInstance().getSotwManager();
        Team at = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (getConfig().getBoolean("BLOCKSHOP_CONFIG.ONLY_SOTW") && !sotwManager.isActive()) {
            player.sendMessage(getLanguageConfig().getString("BLOCKSHOP_COMMAND.DISABLED_SOTW"));
            return true;

        } else if (!getConfig().getBoolean("BLOCKSHOP_CONFIG.ALLOW_WARZONE") && at instanceof WarzoneTeam) {
            player.sendMessage(getLanguageConfig().getString("BLOCKSHOP_COMMAND.DISABLED_WARZONE"));
            return true;

        } else if (!getConfig().getBoolean("BLOCKSHOP_CONFIG.ALLOW_WILDERNESS") && at instanceof WildernessTeam) {
            player.sendMessage(getLanguageConfig().getString("BLOCKSHOP_COMMAND.DISABLED_WILDERNESS"));
            return true;

        } else if (!getConfig().getBoolean("BLOCKSHOP_CONFIG.ALLOW_COMBAT") && getInstance().getTimerManager().getCombatTimer().hasTimer(player)) {
            player.sendMessage(getLanguageConfig().getString("BLOCKSHOP_COMMAND.DISABLED_COMBAT"));
            return true;

        } else if (at instanceof PlayerTeam) {
            PlayerTeam pt = (PlayerTeam) at;

            if (getConfig().getBoolean("BLOCKSHOP_CONFIG.ALLOW_OWN_TEAM") && pt.getPlayers().contains(player.getUniqueId())) {
                return false;

            } else if (!getConfig().getBoolean("BLOCKSHOP_CONFIG.ALLOW_ENEMY_TEAM") && !pt.getPlayers().contains(player.getUniqueId())) {
                player.sendMessage(getLanguageConfig().getString("BLOCKSHOP_COMMAND.DISABLED_ENEMY"));
                return true;
            }

        } else if (!(at instanceof SafezoneTeam)) {
            player.sendMessage(getLanguageConfig().getString("BLOCKSHOP_COMMAND.DISABLED_HERE"));
            return true;
        }

        return false;
    }

    public Map<Integer, Button> getAllButtons(Player player) {
        return getButtons(player, "EVERY_MENU_BUTTONS");
    }

    public Map<Integer, Button> getButtons(Player player, String mainPath) {
        Map<Integer, Button> map = new HashMap<>();

        for (String key : getBlockshopConfig().getConfigurationSection(mainPath).getKeys(false)) {
            String path = mainPath + "." + key + ".";
            String subPath = mainPath + "." + key;
            List<BlockshopAction> actions = new ArrayList<>();
            ConfigurationSection section = getBlockshopConfig().getConfigurationSection(subPath);

            // Handle the actions
            if (section != null) {
                for (String key2 : section.getKeys(false)) {
                    BlockshopAction action = getAction(key2, path + key2 + ".");

                    if (action != null) {
                        actions.add(action);
                    }
                }
            }

            String slot = getBlockshopConfig().getString(path + "SLOT");
            Button button = new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);

                    for (BlockshopAction action : actions) {
                        List<ClickType> clickTypes = action.getClickTypes();

                        if (!clickTypes.isEmpty() && clickTypes.stream().noneMatch(clickType -> clickType == e.getClick())) {
                            continue;
                        }

                        if (!action.handleClick(player, e)) {
                            return;
                        }
                    }

                    if (getBlockshopConfig().contains(path + "SOUND")) {
                        playSound(player, getBlockshopConfig().getString(path + "SOUND"), false);
                    }
                }

                @Override
                public ItemStack getItemStack() {
                    return loadItem(player, path);
                }
            };

            // Handle slot that can be multiple
            if (slot.contains("-")) {
                String[] split = slot.split("-");
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);

                for (int i = min; i <= max; i++) {
                    map.put(i, button);
                }

            } else {
                map.put(Integer.parseInt(slot), button);
            }
        }

        return map;
    }

    public ItemStack loadItem(Player player, String path) {
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        String material = getBlockshopConfig().getString(path + "MATERIAL");
        ItemBuilder builder;
        UnaryOperator<String> replacer = s -> s
                .replace("%player%", player.getName())
                .replace("%balance%", String.valueOf(user.getBalance()));

        if (material.equalsIgnoreCase("CROWBAR")) {
            builder = new ItemBuilder(Config.CROWBAR.clone());

        } else if (material.contains("_SPAWNER")) {
            String spawnerName = material.split("_")[0];
            Spawner spawner = getInstance().getSpawnerManager().getByName(spawnerName);

            if (spawner == null) {
                throw new IllegalArgumentException("Spawner " + spawnerName + " not found.");
            }

            builder = new ItemBuilder(spawner.getItemStack().clone());

        } else {
            builder = new ItemBuilder(ItemUtils.getMat(getBlockshopConfig().getString(path + "MATERIAL")));
        }

        if (getBlockshopConfig().contains(path + "AMOUNT")) {
            builder.setAmount(getBlockshopConfig().getInt(path + "AMOUNT"));
        }
        if (getBlockshopConfig().contains(path + "NAME")) {
            builder.setName(replacer.apply(getBlockshopConfig().getString(path + "NAME")));
        }
        if (getBlockshopConfig().contains(path + "LORE")) {
            builder.setLore(getBlockshopConfig().getStringList(path + "LORE"), replacer);
        }
        if (getBlockshopConfig().contains(path + "DATA")) {
            builder.data(this, getBlockshopConfig().getInt(path + "DATA"));
        }
        builder.setSkullOwner(player.getName()); // Set skull owner if skull meta
        return builder.toItemStack();
    }

    public BlockshopAction getAction(String key, String path) {
        switch (key) {
            case "BUY_BLOCK":
                return new BlockshopBuyAction(this, path);

            case "SELL_BLOCK":
                return new BlockshopSellAction(this, path);

            case "SELL_ALL_BLOCK_SPECIFIC":
                return new BlockshopSellAllSpecificAction(this, path);

            case "SELL_ALL":
                return new BlockshopSellAllAction(this, path);

            case "OPEN_MENU":
                return new BlockshopOpenAction(this, path);
        }
        return null;
    }
}