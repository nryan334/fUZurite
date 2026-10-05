package me.keano.azurite.modules.users.menu;

import me.keano.azurite.modules.discord.type.RestoreWebhook;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class InventoryMenu extends Menu {

    private final StoredInventory inventory;
    private final Player target;
    private final ItemStack pane;

    public InventoryMenu(MenuManager manager, Player player, Player target, StoredInventory inventory) {
        super(
                manager,
                player,
                manager.getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.TITLE")
                        .replace("%date%", Formatter.formatDate(inventory.getDate())),
                manager.getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.SIZE"),
                false
        );
        this.inventory = inventory;
        this.target = target;
        this.pane = new ItemBuilder(ItemUtils.getMat(getConfig()
                .getString("INVENTORY_RESTORE.INVENTORY_MENU.FILLER.MATERIAL")))
                .setName(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.FILLER.NAME"))
                .data(manager, getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.FILLER.DATA"))
                .toItemStack();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

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
                    return inventory.getContents()[copy];
                }
            });
        }

        // Confirm inventory
        buttons.put(getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                PlayerInventory toRestore = target.getInventory();
                toRestore.setContents(inventory.getContents());
                toRestore.setArmorContents(inventory.getArmor());
                player.sendMessage(getLanguageConfig().getString("RESTORE_COMMAND.RESTORED")
                        .replace("%player%", target.getName())
                );
                target.sendMessage(getLanguageConfig().getString("RESTORE_COMMAND.TARGET")
                        .replace("%player%", player.getName())
                );
                player.closeInventory();
                target.updateInventory();

                new RestoreWebhook(getManager(), player, target, inventory).executeAsync();
            }

            @Override
            public ItemStack getItemStack() {
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig()
                        .getString("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.MATERIAL")))
                        .setName(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.NAME"))
                        .setLore(getConfig().getStringList("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.LORE"))
                        .data(getManager(), getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.CONFIRM_RESTORE.DATA"));

                return builder.toItemStack();
            }
        });

        // Back Button
        buttons.put(getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.SLOT"), new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                player.closeInventory();
                new RestoreMenu(getManager(), player, target).open();
            }

            @Override
            public ItemStack getItemStack() {
                ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getConfig()
                        .getString("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.MATERIAL")))
                        .setName(getConfig().getString("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.NAME"))
                        .setLore(getConfig().getStringList("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.LORE"))
                        .data(getManager(), getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU.BACK_BUTTON.DATA"));

                return builder.toItemStack();
            }
        });

        // Armor
        for (int i = 0; i < 4; i++) {
            String part = (i == 0 ? "HELMET" : i == 1 ? "CHESTPLATE" : i == 2 ? "LEGGINGS" : "BOOTS");

            buttons.put(getConfig().getInt("INVENTORY_RESTORE.INVENTORY_MENU." + part + "_SLOT"), new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return part.equals("HELMET") ?
                            inventory.getArmor()[3] : part.equals("CHESTPLATE") ?
                            inventory.getArmor()[2] : part.equals("LEGGINGS") ?
                            inventory.getArmor()[1] : inventory.getArmor()[0];
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
        for (int i = 50; i <= 52; i++) {
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