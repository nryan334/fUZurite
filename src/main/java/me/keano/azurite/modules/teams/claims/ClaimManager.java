package me.keano.azurite.modules.teams.claims;

import lombok.Getter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.type.WarzoneTeam;
import me.keano.azurite.modules.teams.type.WildernessTeam;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class ClaimManager extends Module<TeamManager> {

    private final Map<String, Map<Coordinate, Claim>> claims;
    private final WildernessTeam wildernessTeam;
    private final WarzoneTeam warzoneTeam;

    public ClaimManager(TeamManager manager) {
        super(manager);
        this.claims = new ConcurrentHashMap<>();
        this.wildernessTeam = new WildernessTeam(manager);
        this.warzoneTeam = new WarzoneTeam(manager);
    }

    public void saveClaim(Claim claim) {
        String worldName = claim.getWorldName();
        Map<Coordinate, Claim> map = claims.getOrDefault(worldName, new ConcurrentHashMap<>());

        for (int x = claim.getMinimumX(); x <= claim.getMaximumX(); x++) {
            for (int z = claim.getMinimumZ(); z <= claim.getMaximumZ(); z++) {
                map.put(new Coordinate(x, z), claim);
            }
        }

        claims.put(worldName, map);
    }

    public void deleteClaim(Claim claim) {
        String worldName = claim.getWorldName();
        Map<Coordinate, Claim> map = claims.getOrDefault(worldName, new ConcurrentHashMap<>());

        for (int x = claim.getMinimumX(); x <= claim.getMaximumX(); x++) {
            for (int z = claim.getMinimumZ(); z <= claim.getMaximumZ(); z++) {
                map.remove(new Coordinate(x, z));
            }
        }

        claims.put(worldName, map);
    }

    public void teleportSafe(Player player) {
        Location safe = getSafeLocation(player.getLocation());

        if (safe == null) {
            player.setMetadata("loggedout", new FixedMetadataValue(getInstance(), true));
            player.kickPlayer(getLanguageConfig().getString("STUCK_TIMER.NO_SAFE_LOC"));
            return;
        }

        player.teleport(safe);
    }

    public Location getSafeLocation(Location location) {
        if (getClaim(location) == null) {
            return Utils.getActualHighestBlock(location.getBlock()).getLocation().add(0.5, 1, 0.5);
        }

        // Uses the same idea as FrozenOrb. (Starts looping outwards negatively and positively
        // Name of first loop
        for (int xPos = 2, xNeg = -2; xPos < 250; xPos += 2, xNeg -= 2) {
            for (int zPos = 2, zNeg = -2; zPos < 250; zPos += 2, zNeg -= 2) {
                Location atPos = location.clone().add(xPos, 0, zPos);

                if (getClaim(atPos) == null && canTeleport(atPos)) {
                    return Utils.getActualHighestBlock(atPos.getBlock()).getLocation().add(0.5, 1, 0.5);
                }

                Location atNeg = location.clone().add(xNeg, 0, zNeg);

                if (getClaim(atNeg) == null && canTeleport(atNeg)) {
                    return Utils.getActualHighestBlock(atNeg.getBlock()).getLocation().add(0.5, 1, 0.5);
                }
            }
        }

        return null;
    }

    public Team getTeam(Location location) {
        return getTeam(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    public Team getTeam(World world, int x, int z) {
        Claim claim = getClaim(world.getName(), x, z);

        if (claim != null) {
            return getManager().getTeam(claim.getTeam());
        }

        if (world.getEnvironment() == World.Environment.NORMAL) {
            if (x <= Config.WARZONE_NORMAL && x >= -Config.WARZONE_NORMAL
                    && z <= Config.WARZONE_NORMAL && z >= -Config.WARZONE_NORMAL) return warzoneTeam;

        } else {
            if (x <= Config.WARZONE_NETHER && x >= -Config.WARZONE_NETHER
                    && z <= Config.WARZONE_NETHER && z >= -Config.WARZONE_NETHER) return warzoneTeam;
        }

        return wildernessTeam;
    }

    public Set<Claim> getNearbyCuboids(Location location, int radius) {
        Set<Claim> cuboids = new HashSet<>();
        String worldName = location.getWorld().getName();
        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Claim cuboid = getClaim(worldName, blockX + x, blockZ + z);
                if (cuboid != null) cuboids.add(cuboid);
            }
        }

        return cuboids;
    }

    public Claim getClaim(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return getClaim(location.getWorld().getName(), x, z);
    }

    public Claim getClaim(String world, int x, int z) {
        Map<Coordinate, Claim> map = claims.computeIfAbsent(world, k -> new ConcurrentHashMap<>());
        return map.get(new Coordinate(x, z));
    }

    /*
    Credits iHCF
     */
    public int getPrice(Claim claim, boolean selling) {
        int areaMultiplier = getTeamConfig().getInt("CLAIMING.MULTIPLIER_AREA");
        int claimMultiplier = getTeamConfig().getInt("CLAIMING.MULTIPLIER_CLAIM");

        double pricePerBlock = getTeamConfig().getDouble("CLAIMING.PRICE_PER_BLOCK");
        double sellMultiplier = getTeamConfig().getDouble("CLAIMING.SELL_MULTIPLIER");

        int currentClaims = getManager().getPlayerTeam(claim.getTeam()).getClaims().size();
        int multiplier = 1;
        int remaining = claim.getArea();
        double price = 0;

        while (remaining > 0) {
            if (--remaining % areaMultiplier == 0) {
                multiplier++;
            }

            price += (pricePerBlock * multiplier);
        }

        if (currentClaims != 0) {
            currentClaims = Math.max(currentClaims + (selling ? -1 : 0), 0);
            price += (currentClaims * claimMultiplier);
        }

        if (selling) {
            price *= sellMultiplier;
        }

        return (int) price;
    }

    private boolean canTeleport(Location location) {
        Block block = location.getBlock();

        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.DOWN || face == BlockFace.UP) continue;

            if (getClaim(block.getRelative(face).getLocation()) != null) {
                return false;
            }
        }

        return true;
    }
}