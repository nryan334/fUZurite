package me.keano.azurite.modules.ability.type;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.WildernessTeam;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AntiTrapBeaconAbility extends Ability {

    private final List<Location> denied;
    private final List<Material> deniedInteract;
    private final Map<Location, BeaconData> beacons;

    private final boolean denyPlace;
    private final boolean denyBreak;
    private final boolean denyInteract;

    private final int radius;
    private final int despawn;
    private final int hitpoints;

    public AntiTrapBeaconAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Anti Trap Beacon"
        );
        this.denied = new ArrayList<>(); // we need duplicates otherwise if 2 beacons placed near it will bug out.
        this.beacons = new HashMap<>();
        this.deniedInteract = getAbilitiesConfig().getStringList("ANTI_TRAP_BEACON.DENIED_INTERACT").stream().map(ItemUtils::getMat).collect(Collectors.toList());

        this.denyPlace = getAbilitiesConfig().getBoolean("ANTI_TRAP_BEACON.DENY_PLACE");
        this.denyBreak = getAbilitiesConfig().getBoolean("ANTI_TRAP_BEACON.DENY_BREAK");
        this.denyInteract = getAbilitiesConfig().getBoolean("ANTI_TRAP_BEACON.DENY_INTERACT");

        this.radius = getAbilitiesConfig().getInt("ANTI_TRAP_BEACON.RADIUS");
        this.despawn = getAbilitiesConfig().getInt("ANTI_TRAP_BEACON.DESPAWN");
        this.hitpoints = getAbilitiesConfig().getInt("ANTI_TRAP_BEACON.HITPOINTS");
    }

    public void handlePlace(BlockPlaceEvent e, Team atPlace) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Material replaced = e.getBlockReplacedState().getType();

        if (!(atPlace instanceof PlayerTeam) && !(atPlace instanceof WildernessTeam)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_BEACON.CANNOT_PLACE"));
            return;
        }

        if (replaced.isBlock() && replaced != Material.AIR) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_BEACON.BLOCK_EMPTY"));
            return;
        }

        if (!block.getRelative(BlockFace.DOWN).getType().isSolid()) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_BEACON.BLOCK_BELOW"));
            return;
        }

        if (cannotUse(player) || hasCooldown(player)) {
            e.setCancelled(true);
            return;
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        List<Location> cloned = new ArrayList<>();
        int oldX = block.getX();
        int oldY = block.getY();
        int oldZ = block.getZ();

        applyCooldown(player);
        takeItem(player);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location location = new Location(player.getWorld(), oldX + x, oldY + y, oldZ + z);
                    denied.add(location);
                    cloned.add(location);
                }
            }
        }

        BeaconData data = new BeaconData(player.getUniqueId(), block.getLocation(), cloned, hitpoints);
        data.setRemoveTask(new BukkitRunnable() {
            @Override
            public void run() {
                destroyBeacon(data);
                data.setRemoveTask(null);
            }
        }.runTaskLater(getInstance(), despawn * 20L));

        beacons.put(block.getLocation(), data);

        if (pt != null) {
            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_BEACON.TEAM_MESSAGE")) {
                pt.broadcast(s
                        .replace("%player%", player.getName())
                        .replace("%loc%", Utils.formatLocation(block.getLocation()))
                );
            }
        }

        if (atPlace instanceof PlayerTeam) {
            PlayerTeam enemyTeam = (PlayerTeam) atPlace;

            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_BEACON.ENEMY_MESSAGE")) {
                enemyTeam.broadcast(s);
            }
        }

        for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_BEACON.USED")) {
            player.sendMessage(s);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (!denyPlace) return;

        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (denied.contains(block.getLocation()) && !beacons.containsKey(block.getLocation())) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_BEACON.CANNOT_USE"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!denyBreak) return;

        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (denied.contains(block.getLocation())) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_BEACON.CANNOT_USE"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBeacon(BlockBreakEvent e) {
        if (beacons.containsKey(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("BLOCK")) return;
        if (!e.hasBlock()) return;
        if (!denyInteract) return;

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        BeaconData beacon = beacons.get(block.getLocation());

        if (beacon != null) {
            e.setCancelled(true);
            beacon.setHitpoints(beacon.getHitpoints() - 1);

            if (beacon.getHitpoints() <= 0) {
                destroyBeacon(beacon);

                if (beacon.getRemoveTask() != null) {
                    beacon.getRemoveTask().cancel();
                    beacon.setRemoveTask(null);
                }
            }
            return;
        }

        if (denied.contains(block.getLocation()) && deniedInteract.contains(block.getType())) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_BEACON.CANNOT_USE"));
        }
    }

    public void destroyBeacon(BeaconData data) {
        Player player = Bukkit.getPlayer(data.getPlayer());
        Block block = data.getLocation().getBlock();

        if (block.getType() == item.getType()) {
            block.setType(Material.AIR);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_BEACON.BEACON_DESTROYED"));
        }

        for (Location location : data.getDenied()) {
            denied.remove(location);
        }
    }

    @Getter
    @Setter
    private static class BeaconData {

        private UUID player;
        private Location location;
        private List<Location> denied;
        private BukkitTask removeTask;
        private int hitpoints;

        public BeaconData(UUID player, Location location, List<Location> denied, int hitpoints) {
            this.player = player;
            this.location = location;
            this.denied = denied;
            this.removeTask = null;
            this.hitpoints = hitpoints;
        }
    }
}