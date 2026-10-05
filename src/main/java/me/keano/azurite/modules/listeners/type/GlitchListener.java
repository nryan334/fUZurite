package me.keano.azurite.modules.listeners.type;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class GlitchListener extends Module<ListenerManager> {

    private final Cooldown hitCooldown;

    public GlitchListener(ListenerManager manager) {
        super(manager);
        this.hitCooldown = new Cooldown(manager);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlock(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Location location = e.getBlock().getLocation();

        if (e.isCancelled() || !getInstance().getTeamManager().canBuild(player, location, true)) {
            hitCooldown.applyCooldownTicks(player, 950);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Player player = e.getPlayer();
        Location location = e.getClickedBlock().getLocation();

        if (e.getClickedBlock().getType().name().contains("SIGN")) return;

        if (e.useInteractedBlock() == Event.Result.DENY || !getInstance().getTeamManager().canBuild(player, location)) {
            hitCooldown.applyCooldownTicks(player, 950);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;
        if (!hitCooldown.hasCooldown(damager)) return;

        // if the player can't see the damaged player - keqno
        if (!damager.hasLineOfSight(e.getEntity())) {
            hitCooldown.removeCooldown(damager);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        hitCooldown.removeCooldown(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent e) {
        hitCooldown.removeCooldown(e.getPlayer());
    }
}