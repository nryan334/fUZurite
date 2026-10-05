package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RodTimer extends PlayerTimer {

    public RodTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.ROD,
                false,
                "Rod",
                "PLAYER_TIMERS.FISHING_ROD",
                "ROD_TIMER.TIME"
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof FishHook)) return;

        FishHook fishHook = (FishHook) e.getDamager();

        if (!(fishHook.getShooter() instanceof Player)) return;

        Player damager = (Player) fishHook.getShooter();
        PvPClass pvpClass = getInstance().getClassManager().getActiveClass(damager);

        if (pvpClass != null) {
            int time = getConfig().getInt("ROD_TIMER.CLASSES_TIME." + pvpClass.getName().toUpperCase());
            if (time > 0) applyTimer(damager, (time * 1000L));
            return;
        }

        if (seconds > 0) {
            applyTimer(damager);
        }
    }

    @EventHandler
    public void onRod(PlayerInteractEvent e) {
        if (!e.hasItem()) return;
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item.getType() != ItemUtils.getMat("FISHING_ROD")) return;

        if (hasTimer(player)) {
            Collection<FishHook> fishHooks = player.getWorld().getEntitiesByClass(FishHook.class);

            // This will check if the player has a fishing rod launched, if it does then we don't want to cancel it.
            for (FishHook fishHook : fishHooks) {
                if (!fishHook.isDead() && fishHook.getShooter() != null && fishHook.getShooter() instanceof Player) {
                    Player shooter = (Player) fishHook.getShooter();

                    if (shooter.getUniqueId() == player.getUniqueId()) {
                        return;
                    }
                }
            }

            e.setCancelled(true);
        }
    }
}