package me.keano.azurite.modules.hooks.clients;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.hooks.clients.type.CheatBreakerClient;
import me.keano.azurite.modules.hooks.clients.type.LunarClient;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class ClientHook extends Manager implements Client {

    private final List<Client> clients;

    public ClientHook(HCF instance) {
        super(instance);
        this.clients = new ArrayList<>();
        this.load();
    }

    private void load() {
        if (Utils.verifyPlugin("Apollo-Bukkit", getInstance())) {
            clients.add(new LunarClient(this));
        }

        if (Utils.verifyPlugin("CheatBreakerAPI", getInstance())) {
            clients.add(new CheatBreakerClient(this));
        }
    }

    @Override
    public void overrideNametags(Player target, Player viewer, List<String> tag) {
        for (Client client : clients) {
            client.overrideNametags(target, viewer, tag);
        }
    }

    @Override
    public void clearNametags(Player player) {
        for (Client client : clients) {
            client.clearNametags(player);
        }
    }

    @Override
    public void sendWaypoint(Player player, Location location, WaypointAzurite waypoint, UnaryOperator<String> replacer) {
        for (Client client : clients) {
            client.sendWaypoint(player, location, waypoint, replacer);
        }
    }

    @Override
    public void removeWaypoint(Player player, Location location, WaypointAzurite waypoint, UnaryOperator<String> replacer) {
        for (Client client : clients) {
            client.removeWaypoint(player, location, waypoint, replacer);
        }
    }

    @Override
    public void sendTeamViewer(Player player, PlayerTeam pt) {
        for (Client client : clients) {
            client.sendTeamViewer(player, pt);
        }
    }

    @Override
    public void clearTeamViewer(Player player) {
        for (Client client : clients) {
            client.clearTeamViewer(player);
        }
    }

    @Override
    public void giveStaffModules(Player player) {
        for (Client client : clients) {
            client.giveStaffModules(player);
        }
    }

    @Override
    public void disableStaffModules(Player player) {
        for (Client client : clients) {
            client.disableStaffModules(player);
        }
    }

    @Override
    public void sendBorderPacket(Player player, Claim claim, Color color) {
        for (Client client : clients) {
            client.sendBorderPacket(player, claim, color);
        }
    }

    @Override
    public void sendRemoveBorderPacket(Player player, UUID id) {
        for (Client client : clients) {
            client.sendRemoveBorderPacket(player, id);
        }
    }

    @Override
    public void handleJoin(Player player) {
        for (Client client : clients) {
            client.handleJoin(player);
        }
    }
}