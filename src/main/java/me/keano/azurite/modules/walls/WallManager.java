package me.keano.azurite.modules.walls;

import com.lunarclient.apollo.Apollo;
import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.walls.listener.WallListener;
import me.keano.azurite.modules.walls.task.WallTask;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import me.keano.azurite.utils.extra.NameThreadFactory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@SuppressWarnings("deprecation")
public class WallManager extends Manager {

    private final Map<UUID, Wall> walls;
    private final List<Material> materialList;
    private final ScheduledExecutorService executor;
    private final Cooldown cooldown;

    public WallManager(HCF instance) {
        super(instance);

        this.walls = new ConcurrentHashMap<>();
        this.materialList = new ArrayList<>();
        this.executor = Executors.newScheduledThreadPool(2, new NameThreadFactory("Azurite - Wall Thread"));
        this.executor.scheduleAtFixedRate(new WallTask(this), 0L, 200L, TimeUnit.MILLISECONDS);
        this.cooldown = new Cooldown(this);

        this.load();
        new WallListener(this);
    }

    @Override
    public void disable() {
        executor.shutdown();
    }

    private void load() {
        for (Material material : Material.values()) {
            if (material.isBlock() && material.isSolid()) {
                materialList.add(material);
            }
        }
    }

    public void sendWall(Player player, Claim cuboid, WallType type) {
        Wall wall = walls.get(player.getUniqueId());

        if (Config.ALLOW_LUNAR_WALLS && !getInstance().getClientHook().getClients().isEmpty() && Apollo.getPlayerManager().hasSupport(player.getUniqueId())) {
            Set<Claim> teams = wall.getLunar();

            if (!teams.contains(cuboid)) {
                teams.add(cuboid);
                Tasks.execute(this, () -> getInstance().getClientHook().sendBorderPacket(player, cuboid, type.getLunarColor()));
            }
            return;
        }

        Set<Location> blocks = wall.getWalls();
        Location playerLocation = player.getLocation();
        int blockX = playerLocation.getBlockX();
        int blockY = playerLocation.getBlockY();
        int blockZ = playerLocation.getBlockZ();

        for (Block wallBlock : cuboid.getWalls(blockX, blockY, blockZ, 49)) {
            Location location = wallBlock.getLocation();

            for (int i = 1; i <= 4; i++) {
                // Upwards
                Location toSendPositive = location.clone().add(0, i, 0);
                Block blockPositive = toSendPositive.getBlock();
                sendBlock(player, toSendPositive, blockPositive, type, blocks);

                // Downwards
                Location toSendNegative = location.clone().subtract(0, i, 0);
                Block blockNegative = toSendNegative.getBlock();
                sendBlock(player, toSendNegative, blockNegative, type, blocks);

                // Middle
                sendBlock(player, location, wallBlock, type, blocks);
            }
        }
    }

    private void sendBlock(Player player, Location location, Block block, WallType type, Set<Location> blocks) {
        if (!block.getChunk().isLoaded()) return;
        if (block.getType().isSolid()) return;
        player.sendBlockChange(location, type.getMaterial(), type.getData());
        blocks.add(location);
    }

    public void clearWalls(Player player) {
        Wall wall = walls.get(player.getUniqueId());

        if (wall == null) return;

        Set<Location> blocks = wall.getWalls();

        blocks.removeIf(location -> {
            if (player.getWorld() != location.getWorld()) {
                Block b = location.getBlock();
                player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
                return true;
            }

            Claim claim = getInstance().getTeamManager().getClaimManager().getClaim(location);

            if (claim == null) {
                Block b = location.getBlock();
                player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
                return true;
            }

            Team team = getInstance().getTeamManager().getTeam(claim.getTeam());

            if (team == null || getWallType(claim, team, player) == null) {
                Block b = location.getBlock();
                player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
                return true;
            }

            if (location.distanceSquared(player.getLocation()) > 60) {
                Block b = location.getBlock();
                player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
                return true;
            }

            return false;
        });
    }

    public void clearLunarBorders(Player player, Set<Claim> nearby) {
        Wall wall = walls.get(player.getUniqueId());
        Set<Claim> claims = wall.getLunar();

        if (claims != null) {
            claims.removeIf(claim -> {
                if (nearby.isEmpty() || !nearby.contains(claim)) {
                    Tasks.execute(this, () -> getInstance().getClientHook().sendRemoveBorderPacket(player, claim.getTeam()));
                    return true;
                }

                Team team = getInstance().getTeamManager().getTeam(claim.getTeam());

                if (team == null || getWallType(claim, team, player) == null) {
                    Tasks.execute(this, () -> getInstance().getClientHook().sendRemoveBorderPacket(player, claim.getTeam()));
                    return true;
                }

                return false;
            });
        }
    }

    public void clearPillar(Player player, Location location) {
        if (location == null) return;

        for (int i = 0; i < location.getWorld().getMaxHeight(); i++) {
            Location clone = location.clone();
            clone.setY(i);
            Block block = clone.getBlock();
            player.sendBlockChange(clone, block.getType(), block.getData());
        }
    }

    public void sendTeamMap(Player player) {
        int teamMapRadius = getTeamConfig().getInt("TEAMS.TEAM_MAP_RADIUS");
        Wall wall = walls.get(player.getUniqueId());
        Set<Claim> nearby = getInstance().getTeamManager().getClaimManager().getNearbyCuboids(player.getLocation(), teamMapRadius);
        List<String> msg = new ArrayList<>();

        if (nearby.isEmpty()) {
            getInstance().getUserManager().getByUUID(player.getUniqueId()).setClaimsShown(false);
            player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_MAP.NO_TEAMS")
                    .replace("%radius%", String.valueOf(teamMapRadius))
            );
            return;
        }

        int i = 0;

        // Loop through teams in a certain radius
        for (Claim cuboid : nearby) {
            Team team = getInstance().getTeamManager().getTeam(cuboid.getTeam());
            Material material = materialList.get(i);

            // Loop through corners and send pillar
            for (Block block : cuboid.getCornerBlocks()) {
                Location clone = block.getLocation().clone();
                clone.setY(player.getLocation().getBlockY());
                sendPillar(player, material, clone);
                wall.getTeamMap().add(block.getLocation());
            }

            i++;
            msg.add(getLanguageConfig().getString("TEAM_COMMAND.TEAM_MAP.CLAIM_FORMAT")
                    .replace("%material%", material.name())
                    .replace("%team%", team.getDisplayName(player))
            );
        }

        // Send all the claim info we just sent pillars for
        for (String string : getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_MAP.MAP_SHOWN")) {
            if (!string.equalsIgnoreCase("%claims%")) {
                player.sendMessage(string);
                continue;
            }

            for (String materials : msg) player.sendMessage(materials);
            msg.clear(); // clear ram
        }
    }

    public void clearTeamMap(Player player) {
        Wall wall = walls.get(player.getUniqueId());

        if (wall != null) {
            for (Location location : wall.getTeamMap()) {
                clearPillar(player, location);
            }

            wall.getTeamMap().clear();
        }
    }

    public WallType getWallType(Claim claim, Team team, Player player) {
        TimerManager timerManager = getInstance().getTimerManager();
        SOTWManager sotwManager = getInstance().getSotwManager();

        if (timerManager.getCombatTimer().hasTimer(player) && team instanceof SafezoneTeam) return WallType.COMBAT_TAG;
        if (timerManager.getPvpTimer().checkEntry(player, team)) return WallType.PVP_TIMER;
        if (timerManager.getInvincibilityTimer().checkEntry(player, team)) return WallType.INVINCIBILITY;

        if (sotwManager.isActive() && !player.hasPermission("azurite.lockclaim.bypass") &&
                !sotwManager.isEnabled(player) && team instanceof PlayerTeam) {
            PlayerTeam pt = (PlayerTeam) team;

            if (claim.isLocked() && !pt.getPlayers().contains(player.getUniqueId())) {
                return WallType.LOCKED_CLAIM;
            }
        }

        if (WallType.EVENT_DENIED.shouldLimit(player, team)) return WallType.EVENT_DENIED;
        if (WallType.CITADEL_DENIED.shouldLimit(player, team)) return WallType.CITADEL_DENIED;
        if (WallType.CONQUEST_DENIED.shouldLimit(player, team)) return WallType.CONQUEST_DENIED;

        return null;
    }

    public void sendPillar(Player player, Location location) {
        sendPillar(player, Material.DIAMOND_BLOCK, location);
    }

    private void sendPillar(Player player, Material material, Location location) {
        Location clone = location.clone();

        for (int i = 0; i < 256; i++) {
            clone.setY(i);
            Material type = clone.getBlock().getType();

            if (!type.isTransparent()) continue;
            if (type.isSolid() && !type.name().contains("WATER")) continue;

            // Need to limit the height otherwise it's going to lag the client on 1.16
            if (Utils.isModernVer() && i >= location.getBlockY() + 20) break;

            // If multiple of 5 send the different material
            if (i % 5 == 0) {
                player.sendBlockChange(clone, material, (byte) 0);
                continue;
            }

            player.sendBlockChange(clone, Material.GLASS, (byte) 0);
        }
    }
}