package me.keano.azurite.modules.staff.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class InspectionMenu extends Menu {

    private final Player target;
    private final ItemStack pane;

    public InspectionMenu(MenuManager manager, Player player, Player target) {
        super(
                manager,
                player,
                manager.getConfig().getString("STAFF_MODE.INSPECTION_MENU.TITLE"),
                manager.getConfig().getInt("STAFF_MODE.INSPECTION_MENU.SIZE"),
                true // We need it to have accurate potion effect times.
        );
        this.target = target;
        this.pane = new ItemBuilder(ItemUtils.getMat(getConfig()
                .getString("STAFF_MODE.INSPECTION_MENU.FILLER.MATERIAL")))
                .setName(getConfig().getString("STAFF_MODE.INSPECTION_MENU.FILLER.NAME"))
                .data(manager, getConfig().getInt("STAFF_MODE.INSPECTION_MENU.FILLER.DATA"))
                .toItemStack();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        PlayerInventory inventory = target.getInventory();

        // Normal items
        for (int i = 0; i < inventory.getContents().length; i++) {
            int copy = i;

            buttons.put(i + 1, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return inventory.getItem(copy);
                }
            });
        }

        // Close inventory
        buttons.put(getConfig().getInt("STAFF_MODE.INSPECTION_MENU.CLOSE_INSPECTION.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                player.closeInventory();
            }

            @Override
            public ItemStack getItemStack() {
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig()
                        .getString("STAFF_MODE.INSPECTION_MENU.CLOSE_INSPECTION.MATERIAL")))
                        .setName(getConfig().getString("STAFF_MODE.INSPECTION_MENU.CLOSE_INSPECTION.NAME"))
                        .setLore(getConfig().getStringList("STAFF_MODE.INSPECTION_MENU.CLOSE_INSPECTION.LORE"))
                        .data(getManager(), getConfig().getInt("STAFF_MODE.INSPECTION_MENU.CLOSE_INSPECTION.DATA"));

                return builder.toItemStack();
            }
        });

        // Potion Effects
        buttons.put(getConfig().getInt("STAFF_MODE.INSPECTION_MENU.POTIONS_VIEWER.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
            }

            @Override
            public ItemStack getItemStack() {
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig()
                        .getString("STAFF_MODE.INSPECTION_MENU.POTIONS_VIEWER.MATERIAL")))
                        .setName(getConfig().getString("STAFF_MODE.INSPECTION_MENU.POTIONS_VIEWER.NAME"))
                        .data(getManager(), getConfig().getInt("STAFF_MODE.INSPECTION_MENU.POTIONS_VIEWER.DATA"));

                for (PotionEffect effect : target.getActivePotionEffects()) {
                    int duration = effect.getDuration();
                    int amplifier = effect.getAmplifier() + 1;
                    long durationLong = (duration / 20L) * 1000L; // Convert to second and then long.

                    builder.addLoreLine(getConfig().getString("STAFF_MODE.INSPECTION_MENU.POTIONS_VIEWER.FORMAT")
                            .replace("%effect%", Utils.convertName(effect.getType()))
                            .replace("%time%", (duration > 1000000 ? "Permanent" : Formatter.getRemaining(durationLong, false)))
                            .replace("%amplifier%", String.valueOf(amplifier))
                    );
                }

                return builder.toItemStack();
            }
        });

        // Player Info
        buttons.put(getConfig().getInt("STAFF_MODE.INSPECTION_MENU.PLAYER_INFO.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
            }

            @Override
            public ItemStack getItemStack() {
                User targetUser = getInstance().getUserManager().getByUUID(target.getUniqueId());
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig().getString("STAFF_MODE.INSPECTION_MENU.PLAYER_INFO.MATERIAL")))
                        .setSkullOwner(target.getName())
                        .setName(getConfig().getString("STAFF_MODE.INSPECTION_MENU.PLAYER_INFO.NAME").replace("%player%", target.getName()))
                        .data(getManager(), getConfig().getInt("STAFF_MODE.INSPECTION_MENU.PLAYER_INFO.DATA"));

                if (targetUser == null) {
                    return new ItemBuilder(Material.WOOL).data(getManager(), (byte) 14).setName("&cNULL USER!").toItemStack();
                }

                for (String s : getConfig().getStringList("STAFF_MODE.INSPECTION_MENU.PLAYER_INFO.LORE")) {
                    builder.addLoreLine(s
                            .replace("%kills%", String.valueOf(targetUser.getKills()))
                            .replace("%deaths%", String.valueOf(targetUser.getDeaths()))
                            .replace("%lives%", String.valueOf(targetUser.getLives()))
                    );
                }

                return builder.toItemStack();
            }
        });

        // Armor
        for (int i = 0; i < 4; i++) {
            String part = (i == 0 ? "HELMET" : i == 1 ? "CHESTPLATE" : i == 2 ? "LEGGINGS" : "BOOTS");

            buttons.put(getConfig().getInt("STAFF_MODE.INSPECTION_MENU." + part + "_SLOT"), new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return part.equals("HELMET") ?
                            inventory.getHelmet() : part.equals("CHESTPLATE") ?
                            inventory.getChestplate() : part.equals("LEGGINGS") ?
                            inventory.getLeggings() : inventory.getBoots();
                }
            });
        }

        // Some fillers
        for (int i = 37; i <= 45; i++) {
            buttons.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return pane;
                }
            });
        }

        // More fillers
        for (int i = 52; i <= 53; i++) {
            buttons.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return pane;
                }
            });
        }

        return buttons;
    }
}