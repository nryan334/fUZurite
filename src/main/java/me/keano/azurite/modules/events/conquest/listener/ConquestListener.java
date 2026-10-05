package me.keano.azurite.modules.events.conquest.listener;

import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.events.conquest.ConquestManager;
import me.keano.azurite.modules.events.conquest.extra.Capzone;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.timers.TimerManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ConquestListener extends Module<ConquestManager> {

    public ConquestListener(ConquestManager manager) {
        super(manager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        this.checkMove(e.getPlayer(), e.getFrom(), e.getTo());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        this.checkMove(e.getPlayer(), e.getFrom(), e.getTo());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Location location = e.getPlayer().getLocation();
        this.checkMove(e.getPlayer(), location, location);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Conquest conquest = getManager().getConquest();
        Capzone capzone = getManager().getZone(player.getLocation());

        conquest.handleDeath(player);

        if (capzone != null && conquest.isActive() && capzone.getCapturing() == player) {
            capzone.setCapturing(null);
        }
    }

    private void checkMove(Player player, Location from, Location to) {
        Capzone conqFrom = getManager().getZone(from);
        Capzone conqTo = getManager().getZone(to);

        // they are coming from zone -> to none cap-zone outside
        if (conqFrom != null && conqTo == null) {
            if (conqFrom.getCapturing() == player) {
                conqFrom.setCapturing(null);
            }

            conqFrom.getOnCap().remove(player);
            return; // Below can't be true so no reason checking.
        }

        // They are coming inside the zone or are inside it already.
        if (conqTo != null) {
            TimerManager timerManager = getInstance().getTimerManager();
            StaffManager staffManager = getInstance().getStaffManager();

            // Make sure staff can't capture
            if (staffManager.isStaffEnabled(player)) return;
            if (staffManager.isVanished(player)) return;
            if (timerManager.getInvincibilityTimer().hasTimer(player)) return;
            if (timerManager.getPvpTimer().hasTimer(player)) return;

            PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

            if (conqTo.getCapturing() == null && pt != null) {
                conqTo.setCapturing(player);
            }

            if (!conqTo.getOnCap().contains(player)) {
                conqTo.getOnCap().add(player);
            }
        }
    }
}