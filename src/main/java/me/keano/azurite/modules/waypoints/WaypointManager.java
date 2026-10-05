package me.keano.azurite.modules.waypoints;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.waypoints.listener.WaypointListener;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class WaypointManager extends Manager {

    private WaypointAzurite spawnWaypoint;
    private WaypointAzurite endSpawnWaypoint;
    private WaypointAzurite netherSpawnWaypoint;

    private WaypointAzurite glowstoneWaypoint;
    private WaypointAzurite oreMountainWaypoint;
    private WaypointAzurite kothWaypoint;
    private WaypointAzurite endExitWaypoint;

    private WaypointAzurite hqWaypoint;
    private WaypointAzurite focusWaypoint;
    private WaypointAzurite rallyWaypoint;
    private WaypointAzurite conquestWaypoint;

    private Location worldSpawn;
    private Location netherSpawn;
    private Location endSpawn;
    private Location endExit;
    private Location endWorldExit; // TODO: find a better place for this.

    public WaypointManager(HCF instance) {
        super(instance);

        this.spawnWaypoint = getWaypoint(WaypointType.SPAWN);
        this.endSpawnWaypoint = getWaypoint(WaypointType.END_SPAWN);
        this.netherSpawnWaypoint = getWaypoint(WaypointType.NETHER_SPAWN);

        this.glowstoneWaypoint = getWaypoint(WaypointType.GLOWSTONE);
        this.oreMountainWaypoint = getWaypoint(WaypointType.ORE_MOUNTAIN);
        this.kothWaypoint = getWaypoint(WaypointType.KOTH);
        this.endExitWaypoint = getWaypoint(WaypointType.END_EXIT);

        this.hqWaypoint = getWaypoint(WaypointType.HQ);
        this.focusWaypoint = getWaypoint(WaypointType.FOCUS);
        this.rallyWaypoint = getWaypoint(WaypointType.RALLY_POINT);
        this.conquestWaypoint = getWaypoint(WaypointType.CONQUEST);

        this.endExit = Serializer.fetchLocationAddOffset(getMiscConfig().getString("END_EXIT"));
        this.endWorldExit = Serializer.fetchLocationAddOffset(getMiscConfig().getString("WORLD_EXIT"));
        this.worldSpawn = Serializer.fetchLocationAddOffset(getMiscConfig().getString("OVERWORLD_SPAWN"));
        this.netherSpawn = Serializer.fetchLocationAddOffset(getMiscConfig().getString("NETHER_SPAWN"));
        this.endSpawn = Serializer.fetchLocationAddOffset(getMiscConfig().getString("END_SPAWN"));

        this.loadWorlds();
        new WaypointListener(this);
    }

    @Override
    public void reload() {
        this.spawnWaypoint = getWaypoint(WaypointType.SPAWN);
        this.endSpawnWaypoint = getWaypoint(WaypointType.END_SPAWN);
        this.netherSpawnWaypoint = getWaypoint(WaypointType.NETHER_SPAWN);
        this.glowstoneWaypoint = getWaypoint(WaypointType.GLOWSTONE);
        this.oreMountainWaypoint = getWaypoint(WaypointType.ORE_MOUNTAIN);
        this.kothWaypoint = getWaypoint(WaypointType.KOTH);
        this.endExitWaypoint = getWaypoint(WaypointType.END_EXIT);
        this.hqWaypoint = getWaypoint(WaypointType.HQ);
        this.focusWaypoint = getWaypoint(WaypointType.FOCUS);
        this.rallyWaypoint = getWaypoint(WaypointType.RALLY_POINT);
        this.conquestWaypoint = getWaypoint(WaypointType.CONQUEST);
    }

    private WaypointAzurite getWaypoint(WaypointType type) {
        String path = "WAYPOINTS." + type.name() + ".";
        return new WaypointAzurite(this,
                getLunarConfig().getString(path + "NAME"), type,
                getLunarConfig().getUntranslatedString(path + "COLOR"),
                getLunarConfig().getBoolean(path + "ENABLED")
        );
    }

    public void enableStaffModules(Player player) {
        getInstance().getClientHook().giveStaffModules(player);
    }

    public void disableStaffModules(Player player) {
        getInstance().getClientHook().disableStaffModules(player);
    }

    public void loadWorlds() {
        Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
            for (World world : Bukkit.getServer().getWorlds()) {
                world.setWeatherDuration(Integer.MAX_VALUE);
                world.setThundering(false);
                world.setStorm(false);
                world.setGameRuleValue("mobGriefing", "false");

                if (Utils.isModernVer()) {
                    world.setGameRuleValue("maxEntityCramming", "0");
                    world.setGameRuleValue("doTraderSpawning", "false"); // disable the wandering traders thing
                    world.setGameRuleValue("doPatrolSpawning", "false");
                    world.setGameRuleValue("doInsomnia", "false"); // phantoms
                    world.setGameRuleValue("disableRaids", "true");
                }
            }
        }, 20 * 10L);
    }
}