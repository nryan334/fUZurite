package me.keano.azurite.modules.timers.type;

import lombok.Getter;
import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.timers.Timer;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.event.AsyncTimerExpireEvent;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class PlayerTimer extends Timer {

    protected final Map<UUID, Long> timerCache;
    protected final Map<UUID, Long> pausedCache;
    protected final ActionBarConfig actionBar;
    protected final boolean pausable;

    public PlayerTimer(TimerManager manager, ActionBarConfig actionBar, boolean pausable, String name, String scoreboardPath, String secondsPath) {
        super(manager, name, scoreboardPath, secondsPath);

        this.timerCache = new ConcurrentHashMap<>();
        this.pausedCache = new ConcurrentHashMap<>();
        this.actionBar = actionBar;
        this.pausable = pausable;

        manager.getPlayerTimers().put(name, this);
    }

    public void applyTimer(Player player) {
        timerCache.put(player.getUniqueId(), System.currentTimeMillis() + (1000L * seconds));
    }

    public void applyTimer(Player player, long time) {
        timerCache.put(player.getUniqueId(), System.currentTimeMillis() + time);
    }

    public void removeTimer(Player player) {
        UUID uuid = player.getUniqueId();
        pausedCache.remove(uuid);
        timerCache.remove(uuid);
    }

    public boolean hasTimer(Player player) {
        UUID uuid = player.getUniqueId();

        if (pausedCache.containsKey(uuid)) {
            return true;
        }

        Long remaining = timerCache.get(uuid);
        return remaining != null && remaining >= System.currentTimeMillis();
    }

    public void pauseTimer(Player player) {
        if (!timerCache.containsKey(player.getUniqueId())) return;
        pausedCache.put(player.getUniqueId(), timerCache.get(player.getUniqueId()) - System.currentTimeMillis());
        timerCache.remove(player.getUniqueId());
    }

    public void unpauseTimer(Player player) {
        if (!pausedCache.containsKey(player.getUniqueId())) return;
        timerCache.put(player.getUniqueId(), System.currentTimeMillis() + pausedCache.get(player.getUniqueId()));
        pausedCache.remove(player.getUniqueId());
    }

    public long getRemaining(Player player) {
        return timerCache.get(player.getUniqueId()) - System.currentTimeMillis();
    }

    public String getRemainingActionBar(Player player) {
        Long paused = pausedCache.get(player.getUniqueId());

        if (paused != null) {
            return Formatter.getRemaining(paused, false);
        }

        long rem = timerCache.get(player.getUniqueId()) - System.currentTimeMillis();
        return Formatter.getRemaining(rem, false);
    }

    public String getRemainingString(Player player) {
        if (pausedCache.containsKey(player.getUniqueId())) {
            return Formatter.getRemaining(pausedCache.get(player.getUniqueId()), true);
        }

        long rem = timerCache.get(player.getUniqueId()) - System.currentTimeMillis();
        return Formatter.getRemaining(rem, true);
    }

    public String getRemainingStringBoard(Player player) {
        Long paused = pausedCache.get(player.getUniqueId());

        if (paused != null) {
            return Formatter.getRemaining(paused, !getConfig().getBoolean("TIMERS_COOLDOWN.OLD_TIMER_FORMAT"));
        }

        long rem = timerCache.get(player.getUniqueId()) - System.currentTimeMillis();
        return Formatter.getRemaining(rem, !getConfig().getBoolean("TIMERS_COOLDOWN.OLD_TIMER_FORMAT"));
    }

    public void tick() {
        timerCache.entrySet().removeIf(entry -> {
            if (entry.getValue() < System.currentTimeMillis()) {
                Bukkit.getServer().getPluginManager().callEvent(new AsyncTimerExpireEvent(this, entry.getKey()));
                return true;
            }

            return false;
        });
    }
}