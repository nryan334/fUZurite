package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.event.AsyncTimerExpireEvent;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ArcherTagTimer extends PlayerTimer {

    public ArcherTagTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.ARCHER_TAG,
                false,
                "ArcherTag",
                "PLAYER_TIMERS.ARCHER_TAG",
                "TIMERS_COOLDOWN.ARCHER_TAG"
        );
    }

    @EventHandler
    public void onExpire(AsyncTimerExpireEvent e) {
        if (e.getTimer() != this) return;

        Player player = Bukkit.getPlayer(e.getPlayer());

        if (player != null) {
            // Below cannot be async
            Tasks.execute(getManager(), () -> {
                PotionEffect restore = getInstance().getClassManager().getRestores().remove(player.getUniqueId(), PotionEffectType.INVISIBILITY);

                if (restore != null) {
                    player.addPotionEffect(restore);
                }
            });
        }
    }
}