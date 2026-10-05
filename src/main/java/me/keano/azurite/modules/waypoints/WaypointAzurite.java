package me.keano.azurite.modules.waypoints;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class WaypointAzurite extends Module<WaypointManager> {

    private final String name;
    private final WaypointType waypointType;
    private final String color;
    private final boolean enabled;

    public WaypointAzurite(WaypointManager manager, String name, WaypointType waypointType, String color, boolean enabled) {
        super(manager);
        this.name = name;
        this.waypointType = waypointType;
        this.color = color;
        this.enabled = enabled;
    }

    public void remove(Player player, Location location, UnaryOperator<String> replacer) {
        if (location == null) return;
        if (!enabled) return;
        getInstance().getClientHook().removeWaypoint(player, location, this, replacer);
    }

    public void send(Player player, Location location, UnaryOperator<String> replacer) {
        if (location == null) return;
        if (!enabled) return;
        getInstance().getClientHook().sendWaypoint(player, location, this, replacer);
    }
}