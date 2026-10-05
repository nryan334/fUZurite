package me.keano.azurite.modules.timers.listeners.playertimers;

import lombok.Getter;
import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.event.AsyncTimerExpireEvent;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class StuckTimer extends PlayerTimer {

    private final Map<UUID, Location> locations;
    private int maxMoveBlocks;

    public StuckTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.F_STUCK,
                false,
                "Stuck",
                "PLAYER_TIMERS.STUCK",
                "TIMERS_COOLDOWN.STUCK"
        );
        this.locations = new HashMap<>();
        this.maxMoveBlocks = getTeamConfig().getInt("TEAMS.F_STUCK_MAX_MOVE");
    }

    @Override
    public void reload() {
        this.maxMoveBlocks = getTeamConfig().getInt("TEAMS.F_STUCK_MAX_MOVE");
    }

    @Override
    public void applyTimer(Player player) {
        locations.put(player.getUniqueId(), player.getLocation());
        super.applyTimer(player);
    }

    @Override
    public void applyTimer(Player player, long time) {
        locations.put(player.getUniqueId(), player.getLocation());
        super.applyTimer(player, time);
    }

    @Override
    public void removeTimer(Player player) {
        locations.remove(player.getUniqueId());
        super.removeTimer(player);
    }

    @EventHandler
    public void onExpire(AsyncTimerExpireEvent e) {
        if (e.getTimer() != this) return;

        Player player = Bukkit.getPlayer(e.getPlayer());

        if (player == null) return; // they logged out

        Tasks.execute(getManager(), () -> getInstance().getTeamManager().getClaimManager().teleportSafe(player));
        player.sendMessage(getLanguageConfig().getString("STUCK_TIMER.TELEPORTED"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removeTimer(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (hasTimer(player)) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("STUCK_TIMER.DAMAGED"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        if (hasTimer(player)) {
            check(player, e.getTo());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        if (hasTimer(player)) {
            check(player, e.getTo());
        }
    }

    private void check(Player player, Location to) {
        if (!hasTimer(player)) return;

        Location start = locations.get(player.getUniqueId());

        int x = Math.abs(start.getBlockX() - to.getBlockX());
        int y = Math.abs(start.getBlockY() - to.getBlockY());
        int z = Math.abs(start.getBlockZ() - to.getBlockZ());

        if (x > maxMoveBlocks || y > maxMoveBlocks || z > maxMoveBlocks) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("STUCK_TIMER.MOVED")
                    .replace("%amount%", String.valueOf(maxMoveBlocks))
            );
        }
    }
}