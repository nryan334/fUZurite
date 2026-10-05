package me.keano.azurite.modules.kits;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.BukkitSerialization;
import me.keano.azurite.utils.extra.Cooldown;
import me.keano.azurite.utils.fanciful.FancyMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Kit extends Module<KitManager> {

    private String name;
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    private Cooldown cooldown;
    private Set<UUID> confirmation;
    private boolean systemKit;
    private int seconds;

    public Kit(KitManager manager, Map<String, Object> map) {
        super(manager);
        this.name = (String) map.get("name");
        this.seconds = Integer.parseInt((String) map.get("seconds"));
        this.systemKit = Boolean.parseBoolean((String) map.get("systemKit"));
        this.cooldown = new Cooldown(manager);
        this.confirmation = new HashSet<>();
        this.contents = BukkitSerialization.itemStackArrayFromBase64((String) map.get("contents"));
        this.armorContents = BukkitSerialization.itemStackArrayFromBase64((String) map.get("armorContents"));
    }

    public Kit(KitManager manager, String name, boolean systemKit) {
        super(manager);
        this.name = name;
        this.seconds = 3;
        this.systemKit = systemKit;
        this.cooldown = new Cooldown(manager);
        this.confirmation = new HashSet<>();
        this.contents = new ItemStack[36];
        this.armorContents = new ItemStack[4];
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("name", name);
        map.put("seconds", String.valueOf(seconds));
        map.put("systemKit", String.valueOf(systemKit));
        map.put("contents", BukkitSerialization.itemStackArrayToBase64(contents));
        map.put("armorContents", BukkitSerialization.itemStackArrayToBase64(armorContents));

        return map;
    }

    public boolean checkConfirmation(Player player) {
        if (getConfig().getBoolean("SIGNS_CONFIG.KIT_SIGN.CONFIRM_NOT_EMPTY")) {
            if (confirmation.remove(player.getUniqueId())) {
                return false;
            }

            boolean empty = true;

            for (ItemStack content : player.getInventory().getContents()) {
                if (content != null && content.getType() != Material.AIR) {
                    empty = false;
                    break;
                }
            }

            if (!empty) {
                FancyMessage fancyMessage = new FancyMessage(getLanguageConfig().getString("CUSTOM_SIGNS.KIT_SIGNS.INVENTORY_NOT_EMPTY"))
                        .tooltip(getLanguageConfig().getString("CUSTOM_SIGNS.KIT_SIGNS.HOVER"))
                        .command("/kit " + name);

                fancyMessage.send(player);
                confirmation.add(player.getUniqueId());
                return true;
            }
        }

        return false;
    }

    public void equip(Player player) {
        player.getInventory().clear();

        for (int i = 0; i < contents.length; i++) {
            ItemStack toSet = contents[i];

            if (toSet == null || toSet.getType() == Material.AIR) continue;

            player.getInventory().setItem(i, toSet);
        }

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack toSet = armorContents[i];

            if (toSet == null || toSet.getType() == Material.AIR) continue;

            player.getInventory().setItem(36 + i, toSet); // armor slot starts at 36
        }

        cooldown.applyCooldown(player, seconds);
        player.updateInventory();
    }

    public void update(ItemStack[] contents, ItemStack[] armorContents) {
        this.contents = new ItemStack[contents.length];
        this.armorContents = new ItemStack[armorContents.length];

        for (int i = 0; i < contents.length; i++) {
            ItemStack toSet = contents[i];
            this.contents[i] = (toSet != null ? toSet.clone() : null);
        }

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack toSet = armorContents[i];
            this.armorContents[i] = (toSet != null ? toSet.clone() : null);
        }
    }

    public void save() {
        getManager().getKits().put(name, this);
        getKitsData().getValues().put(name, serialize());
        getKitsData().save();
    }

    public void delete() {
        getManager().getKits().remove(name);
        getKitsData().getValues().remove(name);
        getKitsData().save();
    }
}