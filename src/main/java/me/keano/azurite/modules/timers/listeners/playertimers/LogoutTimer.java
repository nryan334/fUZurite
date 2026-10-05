package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.event.AsyncTimerExpireEvent;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LogoutTimer extends PlayerTimer {

    public LogoutTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.LOGOUT,
                false,
                "Logout",
                "PLAYER_TIMERS.LOGOUT",
                "TIMERS_COOLDOWN.LOGOUT"
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();
        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;

        if (hasTimer(damaged)) {
            removeTimer(damaged);
            damaged.sendMessage(getLanguageConfig().getString("LOGOUT_COMMAND.DAMAGED_CANCELLED"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        if (hasTimer(player)) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("LOGOUT_COMMAND.MOVED_CANCELLED"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // call first
    public void onExpire(AsyncTimerExpireEvent e) {
        if (!(e.getTimer() instanceof LogoutTimer)) return;

        Player player = Bukkit.getPlayer(e.getPlayer());

        if (player == null) return; // they force logged.

        Tasks.execute(getManager(), () -> {
            player.setMetadata("loggedout", new FixedMetadataValue(getInstance(), true));
            player.kickPlayer(getLanguageConfig().getString("LOGOUT_COMMAND.LOGGED_OUT"));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (hasTimer(player)) {
            removeTimer(player);
        }
    }
}