package me.keano.azurite.modules.hooks.clients;

import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public interface Client {

    void overrideNametags(Player target, Player viewer, List<String> tag);

    void clearNametags(Player player);

    void sendWaypoint(Player player, Location location, WaypointAzurite waypoint, UnaryOperator<String> replacer);

    void removeWaypoint(Player player, Location location, WaypointAzurite waypoint, UnaryOperator<String> replacer);

    void sendTeamViewer(Player player, PlayerTeam pt);

    void clearTeamViewer(Player player);

    void giveStaffModules(Player player);

    void disableStaffModules(Player player);

    void sendBorderPacket(Player player, Claim claim, Color color);

    void sendRemoveBorderPacket(Player player, UUID id);

    void handleJoin(Player player);
}