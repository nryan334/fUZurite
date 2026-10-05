package me.keano.azurite.modules.staff;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.staff.task.ActionBarTask;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class Staff extends Module<StaffManager> {

    private final Player player;
    private final ItemStack[] contents;
    private final ItemStack[] armorContents;
    private final GameMode gameMode;
    private final List<PotionEffect> effects;
    private final ActionBarTask actionBarTask;

    public Staff(StaffManager manager, Player player, GameMode gameMode) {
        super(manager);
        this.player = player;
        this.contents = player.getInventory().getContents();
        this.armorContents = player.getInventory().getArmorContents();
        this.gameMode = gameMode;
        this.actionBarTask = (!getConfig().getBoolean("STAFF_MODE.ACTION_BAR_ENABLED") ? null : new ActionBarTask(manager, player));
        this.effects = new ArrayList<>();
    }
}