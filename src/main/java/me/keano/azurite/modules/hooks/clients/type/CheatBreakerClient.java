package me.keano.azurite.modules.hooks.clients.type;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.object.CBWaypoint;
import com.cheatbreaker.nethandler.server.CBPacketWorldBorder;
import com.cheatbreaker.nethandler.server.CBPacketWorldBorderRemove;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.hooks.clients.Client;
import me.keano.azurite.modules.hooks.clients.ClientHook;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import me.keano.azurite.modules.waypoints.WaypointType;
import org.bukkit.Bukkit;
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
public class CheatBreakerClient extends Module<ClientHook> implements Client {

    public CheatBreakerClient(ClientHook manager) {
        super(manager);
    }

    @Override
    public void overrideNametags(Player target, Player viewer, List<String> tag) {
        CheatBreakerAPI.getInstance().overrideNametag(target, tag, viewer);
    }

    @Override
    public void clearNametags(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            CheatBreakerAPI.getInstance().resetNametag(onlinePlayer, player);
        }
    }

    @Override
    public void sendWaypoint(Player player, Location location, WaypointAzurite waypoint, UnaryOperator<String> replacer) {
        CheatBreakerAPI.getInstance().sendWaypoint(player, new CBWaypoint(
                replacer.apply(waypoint.getName()),
                (waypoint.getWaypointType() == WaypointType.KOTH || waypoint.getWaypointType() == WaypointType.CONQUEST ? location.subtract(0, 1, 0) : location),
                Color.decode(replacer.apply(waypoint.getColor())).getRGB(),
                true,
                true
        ));
    }

    @Override
    public void removeWaypoint(Player player, Location location, WaypointAzurite waypoint, UnaryOperator<String> replacer) {
        CheatBreakerAPI.getInstance().removeWaypoint(player, new CBWaypoint(
                replacer.apply(waypoint.getName()),
                (waypoint.getWaypointType() == WaypointType.KOTH || waypoint.getWaypointType() == WaypointType.CONQUEST ? location.subtract(0, 1, 0) : location),
                Color.decode(replacer.apply(waypoint.getColor())).getRGB(),
                true,
                true
        ));
    }

    @Override
    public void sendTeamViewer(Player player, PlayerTeam pt) {

    }

    @Override
    public void clearTeamViewer(Player player) {

    }

    @Override
    public void giveStaffModules(Player player) {
        CheatBreakerAPI.getInstance().giveAllStaffModules(player);
    }

    @Override
    public void disableStaffModules(Player player) {
        CheatBreakerAPI.getInstance().disableAllStaffModules(player);
    }

    @Override
    public void sendBorderPacket(Player player, Claim claim, Color color) {
        CBPacketWorldBorder packet = new CBPacketWorldBorder(
                claim.getTeam().toString(), claim.getWorldName(), false, false,
                color.getRGB(), claim.getMinimumX(), claim.getMinimumZ(), claim.getMaximumX(), claim.getMaximumZ()
        );

        CheatBreakerAPI.getInstance().sendPacket(player, packet);
    }

    @Override
    public void sendRemoveBorderPacket(Player player, UUID id) {
        CBPacketWorldBorderRemove packet = new CBPacketWorldBorderRemove(id.toString());
        CheatBreakerAPI.getInstance().sendPacket(player, packet);
    }

    @Override
    public void handleJoin(Player player) {
    }
}