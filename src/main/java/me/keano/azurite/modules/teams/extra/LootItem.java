package me.keano.azurite.modules.teams.extra;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.utils.BukkitSerialization;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class LootItem {

    private ItemStack item;
    private double percentage;

    public LootItem(ItemStack item, double percentage) {
        this.item = item;
        this.percentage = percentage;
    }

    public LootItem(String string) {
        String[] split = string.split(", ");
        this.item = BukkitSerialization.itemStackFromBase64(split[0]);
        this.percentage = Double.parseDouble(split[1]);
    }

    public String serialize() {
        return String.join(", ", BukkitSerialization.itemStackToBase64(item), String.valueOf(percentage));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LootItem lootItem = (LootItem) object;
        return Double.compare(percentage, lootItem.percentage) == 0 && Objects.equals(item, lootItem.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, percentage);
    }
}