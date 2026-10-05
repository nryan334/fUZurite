package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BorderListener extends Module<ListenerManager> {

    private final Map<String, Integer> worldBorders;

    public BorderListener(ListenerManager manager) {
        super(manager);

        this.worldBorders = new HashMap<>();

        for (String name : getConfig().getConfigurationSection("BORDERS").getKeys(false)) {
            worldBorders.put(name, getConfig().getInt("BORDERS." + name));
        }
    }

    public boolean inBorder(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int border = worldBorders.getOrDefault(location.getWorld().getName(), 2000);
        return Math.abs(x) <= border && Math.abs(z) <= border;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFill(PlayerBucketFillEvent e) {
        Location location = e.getBlockClicked().getLocation();
        Player player = e.getPlayer();

        if (!inBorder(location)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("BORDER_LISTENER.CANNOT_INTERACT"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEmpty(PlayerBucketEmptyEvent e) {
        Location location = e.getBlockClicked().getLocation();
        Player player = e.getPlayer();

        if (!inBorder(location)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("BORDER_LISTENER.CANNOT_PLACE"));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Location location = e.getClickedBlock().getLocation();
        Player player = e.getPlayer();

        if (!inBorder(location)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("BORDER_LISTENER.CANNOT_INTERACT"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Location location = e.getBlock().getLocation();
        Player player = e.getPlayer();

        if (!inBorder(location)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("BORDER_LISTENER.CANNOT_BREAK"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Location location = e.getBlockPlaced().getLocation();
        Player player = e.getPlayer();

        if (!inBorder(location)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("BORDER_LISTENER.CANNOT_PLACE"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();

        if (!inBorder(entity.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        Entity entity = e.getEntity();

        if (!inBorder(entity.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        if (!inBorder(e.getTo())) {
            e.setTo(e.getFrom());
            player.sendMessage(getLanguageConfig().getString("BORDER_LISTENER.CANNOT_WALK"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        if (inBorder(e.getTo())) return;

        Player player = e.getPlayer();

        e.setCancelled(true);
        getInstance().getTimerManager().getEnderpearlTimer().removeTimer(player);
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        player.sendMessage(getLanguageConfig().getString("BORDER_LISTENER.CANNOT_TELEPORT"));
    }

    @EventHandler(ignoreCancelled = true) // Credits iHCF
    public void onPortal(PlayerPortalEvent e) {
        Location to = e.getTo();

        if (inBorder(to)) return;
        if (to.getWorld().getEnvironment() != World.Environment.NORMAL) return;

        int x = to.getBlockX();
        int z = to.getBlockZ();
        boolean extended = false;
        int worldBorder = worldBorders.getOrDefault("world", 1000);

        if (Math.abs(x) > worldBorder) {
            to.setX(x > 0 ? worldBorder - 50 : -worldBorder + 50);
            extended = true;
        }

        if (Math.abs(z) > worldBorder) {
            to.setZ(z > 0 ? worldBorder - 50 : -worldBorder + 50);
            extended = true;
        }

        if (extended) {
            to.add(0.5D, 0.0D, 0.5D);
            e.setTo(to);
        }
    }
}