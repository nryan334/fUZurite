package me.keano.azurite.modules.framework.menu.button;

import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public abstract class Button {

    public abstract void onClick(InventoryClickEvent e);

    public abstract ItemStack getItemStack();

}