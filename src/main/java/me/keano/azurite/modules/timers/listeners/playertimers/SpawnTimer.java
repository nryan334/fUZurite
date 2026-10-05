package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.event.AsyncTimerExpireEvent;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SpawnTimer extends PlayerTimer {

    public SpawnTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.SPAWN_TIMER,
                false,
                "Spawn",
                "PLAYER_TIMERS.SPAWN",
                "SPAWN_TIMER.TIME"
        );
    }

    @EventHandler
    public void onExpire(AsyncTimerExpireEvent e) {
        if (!(e.getTimer() instanceof SpawnTimer)) return;

        Player player = Bukkit.getPlayer(e.getPlayer());
        Location spawn = getInstance().getWaypointManager().getWorldSpawn();

        if (player != null) {
            Tasks.execute(getManager(), () -> player.teleport(spawn.clone().add(0.5, 0, 0.5)));
            player.sendMessage(getLanguageConfig().getString("SPAWN_TIMER.WARPED"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removeTimer(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = Utils.getDamager(e.getDamager());
        Player damaged = (Player) e.getEntity();

        if (damager == null) return;
        if (damager == damaged) return;

        if (hasTimer(damaged)) {
            removeTimer(damaged);
            damaged.sendMessage(getLanguageConfig().getString("SPAWN_TIMER.DAMAGED"));
        }

        if (hasTimer(damager)) {
            removeTimer(damager);
            damager.sendMessage(getLanguageConfig().getString("SPAWN_TIMER.DAMAGED"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        if (hasTimer(player)) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("SPAWN_TIMER.MOVED"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        if (hasTimer(player)) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("SPAWN_TIMER.MOVED"));
        }
    }
}