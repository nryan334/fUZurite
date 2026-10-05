package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.event.AsyncTimerExpireEvent;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
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
public class HQTimer extends PlayerTimer {

    public HQTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.HQ_TIMER,
                false,
                "HQ",
                "PLAYER_TIMERS.HQ",
                "TIMERS_COOLDOWN.HQ"
        );
    }

    @Override
    public void applyTimer(Player player) {
        super.applyTimer(player, getSecondsEnemy(player) * 1000L);
    }

    public long getSecondsEnemy(Player player) {
        Team atPlayer = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (atPlayer instanceof PlayerTeam && !((PlayerTeam) atPlayer).getPlayers().contains(player.getUniqueId())) {
            return getConfig().getInt("TIMERS_COOLDOWN.HQ_ENEMY");
        }

        return seconds;
    }

    @EventHandler
    public void onExpire(AsyncTimerExpireEvent e) {
        if (e.getTimer() != this) return;

        Player player = Bukkit.getPlayer(e.getPlayer());

        if (player != null) {
            tpHq(player);
        }
    }

    public void tpHq(Player player) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) return;
        if (pt.getHq() == null) return;

        // Teleporting asynchronously isn't allowed on 1.16...
        Tasks.execute(getManager(), () -> player.teleport(pt.getHq()));
        player.sendMessage(getLanguageConfig().getString("HQ_TIMER.WARPED")
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
            damaged.sendMessage(getLanguageConfig().getString("HQ_TIMER.DAMAGED"));
        }

        if (hasTimer(damager)) {
            removeTimer(damager);
            damager.sendMessage(getLanguageConfig().getString("HQ_TIMER.DAMAGED"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        if (hasTimer(player)) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("HQ_TIMER.MOVED"));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        if (hasTimer(player)) {
            removeTimer(player);
            player.sendMessage(getLanguageConfig().getString("HQ_TIMER.MOVED"));
        }
    }
}