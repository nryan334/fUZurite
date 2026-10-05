package me.keano.azurite.modules.events.sotw.listener;

import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SOTWListener extends Module<SOTWManager> {

    public SOTWListener(SOTWManager manager) {
        super(manager);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        if (!getManager().isActive()) return;

        Player player = e.getPlayer();
        Claim to = getInstance().getTeamManager().getClaimManager().getClaim(e.getTo());

        // Don't deny entry if their enabled!
        if (getManager().isEnabled(player)) return;

        if (to != null && to.isLocked() && !player.hasPermission("azurite.lockclaim.bypass")) {
            PlayerTeam pt = getInstance().getTeamManager().getPlayerTeam(to.getTeam());

            if (pt.getPlayers().contains(player.getUniqueId())) return;

            e.setTo(e.getFrom());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (!getManager().isActive()) return;

        Player player = e.getPlayer();
        Claim to = getInstance().getTeamManager().getClaimManager().getClaim(e.getTo());

        // Don't deny entry if their enabled!
        if (getManager().isEnabled(player)) return;

        if (to != null && to.isLocked() && !player.hasPermission("azurite.lockclaim.bypass")) {
            PlayerTeam pt = getInstance().getTeamManager().getPlayerTeam(to.getTeam());

            if (pt == null || pt.getPlayers().contains(player.getUniqueId())) return;

            e.setCancelled(true);
            getInstance().getTimerManager().getEnderpearlTimer().removeTimer(player);
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (getManager().isActive() && !getManager().isEnabled(player)) {
            e.setCancelled(true); // always cancel

            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                // Teleport to spawn if they fell in void.
                player.teleport(getInstance().getWaypointManager().getEndSpawn());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        checkFly(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        checkFly(player);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Player player = e.getPlayer();
        checkFly(player);
    }

    private void checkFly(Player player) {
        if (getManager().getFlying().remove(player.getUniqueId())) {
            player.setFlying(false);
        }
    }
}