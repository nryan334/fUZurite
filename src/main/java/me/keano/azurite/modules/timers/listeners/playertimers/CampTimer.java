package me.keano.azurite.modules.timers.listeners.playertimers;

import lombok.Getter;
import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.teams.type.PlayerTeam;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class CampTimer extends PlayerTimer {

    private final Map<UUID, UUID> teams;

    public CampTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.CAMP_TIMER,
                false,
                "Camp",
                "PLAYER_TIMERS.CAMP",
                "TIMERS_COOLDOWN.CAMP"
        );
        this.teams = new HashMap<>();
    }

    @Override
    public void removeTimer(Player player) {
        super.removeTimer(player);
        teams.remove(player.getUniqueId());
    }

    @EventHandler
    public void onExpire(AsyncTimerExpireEvent e) {
        if (e.getTimer() != this) return;

        Player player = Bukkit.getPlayer(e.getPlayer());

        if (player != null) {
            UUID uuid = teams.remove(player.getUniqueId());
            if (uuid == null) return;
            tpSafe(player, uuid);
        }
    }

    public void tpSafe(Player player, UUID uuid) {
        PlayerTeam pt = getInstance().getTeamManager().getPlayerTeam(uuid);

        if (pt == null) return;
        if (pt.getHq() == null) return;

        Location safe = getInstance().getTeamManager().getClaimManager().getSafeLocation(pt.getHq());

        // Teleporting asynchronously isn't allowed on 1.16...
        Tasks.execute(getManager(), () -> player.teleport(safe));
        player.sendMessage(getLanguageConfig().getString("CAMP_TIMER.WARPED")
                .replace("%team%", pt.getName())
        );
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
            damaged.sendMessage(getLanguageConfig().getString("CAMP_TIMER.DAMAGED"));
        }

        if (hasTimer(damager)) {
            removeTimer(damager);
            damager.sendMessage(getLanguageConfig().getString("CAMP_TIMER.DAMAGED"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        if (hasTimer(player)) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("CAMP_TIMER.MOVED"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        if (hasTimer(player)) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("CAMP_TIMER.MOVED"));
        }
    }
}