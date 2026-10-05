package me.keano.azurite.modules.pvpclass.listener;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.timers.event.AsyncTimerExpireEvent;
import me.keano.azurite.modules.timers.listeners.playertimers.WarmupTimer;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PvPClassListener extends Module<PvPClassManager> {

    public PvPClassListener(PvPClassManager manager) {
        super(manager);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        PvPClass active = getManager().getActiveClass(player);

        if (active != null) {
            active.unEquip(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        PvPClass active = getManager().getActiveClass(player);

        if (active != null) {
            active.unEquip(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Don't check armor if they haven't played before
        if (player.hasPlayedBefore()) {
            getManager().checkArmor(player);
            getManager().checkClassLimit(player);
        }
    }

    @EventHandler
    public void onExpire(AsyncTimerExpireEvent e) {
        if (!(e.getTimer() instanceof WarmupTimer)) return;

        Player player = Bukkit.getPlayer(e.getPlayer());

        if (player == null) return; // they left while warming up

        WarmupTimer timer = (WarmupTimer) e.getTimer();
        String name = timer.getWarmups().remove(player.getUniqueId());

        if (name != null) {
            PvPClass pvpClass = getManager().getClasses().get(name);

            if (pvpClass == null) return;

            Tasks.execute(getManager(), () -> {
                if (pvpClass.hasArmor(player)) { // FIX!!!!!!!!!!!!!!!
                    pvpClass.equip(player);
                    getManager().checkClassLimit(player);
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        PvPClass active = getManager().getActiveClass(player);

        if (e.getItem().getType() == ItemUtils.getMat("MILK_BUCKET") && active != null) {
            Tasks.execute(getManager(), () -> active.addEffects(player)); // delay a tick
        }
    }
}