package me.keano.azurite.modules.timers.listeners.playertimers;

import lombok.Getter;
import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collection;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class EnderpearlTimer extends PlayerTimer {

    public EnderpearlTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.ENDERPEARL,
                false,
                "Enderpearl",
                "PLAYER_TIMERS.ENDER_PEARL",
                "TIMERS_COOLDOWN.ENDER_PEARL"
        );
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof EnderPearl)) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) e.getEntity().getShooter();

        if (!hasTimer(player)) {
            long cooldownMillis = seconds * 1000L;

            applyTimer(player, cooldownMillis);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Collection<EnderPearl> enderPearls = player.getWorld().getEntitiesByClass(EnderPearl.class);

        for (EnderPearl enderPearl : enderPearls) {
            if (enderPearl.getShooter() != null && enderPearl.getShooter() == player) {
                enderPearl.remove();
                break;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.ENDER_PEARL) return;

        Player player = e.getPlayer();

        if (hasTimer(player)) {
            e.setCancelled(true);
            player.updateInventory();
            player.sendMessage(getLanguageConfig().getString("ENDERPEARL_TIMER.COOLDOWN")
                    .replace("%seconds%", getRemainingString(player))
            );
        }
    }
}