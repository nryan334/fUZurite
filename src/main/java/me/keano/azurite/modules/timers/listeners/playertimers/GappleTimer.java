package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Formatter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class GappleTimer extends PlayerTimer {

    public GappleTimer(TimerManager manager) {
        super(
                manager,
                null,
                false,
                "Gapple",
                "PLAYER_TIMERS.GAPPLE",
                "TIMERS_COOLDOWN.GAPPLE"
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!getManager().isGapple(item)) return;

        if (hasTimer(player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("GAPPLE_TIMER.COOLDOWN")
                    .replace("%seconds%", getRemainingString(player))
            );
            return;
        }

        if (seconds != 0) {
            long cooldownMillis = seconds * 1000L;

            applyTimer(player, cooldownMillis);

            for (String s : getLanguageConfig().getStringList("GAPPLE_TIMER.ADDED_COOLDOWN")) {
                player.sendMessage(s
                        .replace("%cooldown%", Formatter.formatDetailed(cooldownMillis))
                );
            }
        }
    }
}