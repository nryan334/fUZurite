package me.keano.azurite.modules.masks;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.utils.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MaskGUI extends Menu {

    private final MaskManager maskManager;
    private final MaskGUIConfig guiConfig;

    public MaskGUI(MenuManager manager, Player player, MaskManager maskManager, MaskGUIConfig guiConfig) {
        super(manager, player, guiConfig.getGuiTitle(), guiConfig.getGuiSize(), false);
        this.maskManager = maskManager;
        this.guiConfig = guiConfig;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        Set<Integer> occupiedSlots = new HashSet<>(guiConfig.getMaskSlotValues());

        for (Map.Entry<String, Integer> entry : guiConfig.getMaskSlots().entrySet()) {
            String maskName = entry.getKey();
            int slot = entry.getValue();
            if (slot < 0 || slot >= guiConfig.getGuiSize()) continue;

            final String maskKey = maskName;
            ItemStack maskItem = maskManager.createMaskItem(maskName);
            if (maskItem == null) continue;

            buttons.put(slot + 1, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                    if (maskManager.getMask(maskKey) == null) return;
                    player.getInventory().addItem(maskManager.createMaskItem(maskKey));
                    player.sendMessage(CC.t("&aYou received a &c" +
                            maskManager.getMask(maskKey).getDisplayName() + "&a!"));
                    player.closeInventory();
                }

                @Override
                public ItemStack getItemStack() {
                    return maskItem.clone();
                }
            });
        }

        for (MaskGUIConfig.DecorativeItem dec : guiConfig.getDecorativeItems()) {
            if (occupiedSlots.contains(dec.getSlot())) continue;
            if (dec.getSlot() < 0 || dec.getSlot() >= guiConfig.getGuiSize()) continue;

            ItemStack decItem = guiConfig.createDecorativeItem(dec);
            final int slot = dec.getSlot();

            buttons.put(slot + 1, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return decItem.clone();
                }
            });
        }

        return buttons;
    }
}
