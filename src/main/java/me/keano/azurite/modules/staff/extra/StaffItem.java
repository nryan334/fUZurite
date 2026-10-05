package me.keano.azurite.modules.staff.extra;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.staff.StaffManager;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class StaffItem extends Module<StaffManager> {

    private final String name;
    private final StaffItemAction action;
    private final String replacement;
    private final String command;
    private final ItemStack item;
    private final int slot;

    public StaffItem(StaffManager manager, String name, StaffItemAction action, String replacement, String command, ItemStack item, int slot) {
        super(manager);
        this.name = name;
        this.action = action;
        this.replacement = replacement;
        this.command = command;
        this.item = item;
        this.slot = slot;
    }
}