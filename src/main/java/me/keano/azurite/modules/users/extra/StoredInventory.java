package me.keano.azurite.modules.users.extra;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.utils.BukkitSerialization;
import org.bukkit.inventory.ItemStack;

import java.util.Date;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class StoredInventory {

    private ItemStack[] contents;
    private ItemStack[] armor;
    private Date date;

    public StoredInventory(ItemStack[] contents, ItemStack[] armor, Date date) {
        this.contents = contents;
        this.armor = armor;
        this.date = date;
    }

    public String serialize() {
        return String.join(", ",
                BukkitSerialization.itemStackArrayToBase64(contents),
                BukkitSerialization.itemStackArrayToBase64(armor), String.valueOf(date.getTime()));
    }

    public static StoredInventory fromString(String string) {
        String[] split = string.split(", ");
        return new StoredInventory(
                BukkitSerialization.itemStackArrayFromBase64(split[0]),
                BukkitSerialization.itemStackArrayFromBase64(split[1]),
                new Date(Long.parseLong(split[2]))
        );
    }
}