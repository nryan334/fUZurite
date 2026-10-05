package me.keano.azurite.modules.events.koth.listener;

import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.events.koth.KothManager;
import me.keano.azurite.modules.events.koth.task.KothTickTask;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.staff.StaffManager;
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
public class KothListener extends Module<KothManager> {

    public KothListener(KothManager manager) {
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
        Koth koth = getManager().getZone(player.getLocation());

        if (koth != null && koth.isActive() && koth.getCapturing() == player) {
            koth.setCapturing(null);
            koth.setRemaining(koth.getMinutes());
            koth.getOnCap().remove(player);
        }
    }

    private void checkMove(Player player, Location from, Location to) {
        Koth kothFrom = getManager().getZone(from);
        Koth kothTo = getManager().getZone(to);

        // they are coming from zone -> to none cap-zone outside
        if (kothFrom != null && kothTo == null) {
            if (kothFrom.getCapturing() == player) {
                if (kothFrom.isActive()) {
                    kothFrom.checkLostCapMessage(player);
                }

                kothFrom.setCapturing(null);
                kothFrom.setRemaining(kothFrom.getMinutes());
            }

            kothFrom.getOnCap().remove(player);

            // Cancel task
            if (kothFrom.isActive() && kothFrom.getOnCap().isEmpty() && kothFrom.getBukkitTask() != null) {
                kothFrom.getBukkitTask().cancel();
                kothFrom.setBukkitTask(null);
            }
            return; // Below can't be true so no reason checking.
        }

        // They are coming inside the zone or are inside it already.
        if (kothTo != null) {
            TimerManager timerManager = getInstance().getTimerManager();
            StaffManager staffManager = getInstance().getStaffManager();

            // Make sure staff can't capture
            if (staffManager.isStaffEnabled(player)) return;
            if (staffManager.isVanished(player)) return;
            if (timerManager.getInvincibilityTimer().hasTimer(player)) return;
            if (timerManager.getPvpTimer().hasTimer(player)) return;

            if (kothTo.getCapturing() == null) {
                kothTo.setCapturing(player);

                if (kothTo.getBukkitTask() == null) {
                    kothTo.setBukkitTask(new KothTickTask(kothTo));
                }

                if (kothTo.isActive()) {
                    for (String s : getLanguageConfig().getStringList("KOTH_EVENTS.PLAYER_CONTROLLING"))
                        player.sendMessage(s
                                .replace("%koth%", kothTo.getName())
                                .replace("%color%", kothTo.getColor())
                        );
                }
            }

            if (!kothTo.getOnCap().contains(player)) {
                kothTo.getOnCap().add(player);
            }
        }
    }
}