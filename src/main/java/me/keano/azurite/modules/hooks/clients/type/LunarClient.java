package me.keano.azurite.modules.hooks.clients.type;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.cuboid.Cuboid2D;
import com.lunarclient.apollo.common.location.ApolloBlockLocation;
import com.lunarclient.apollo.common.location.ApolloLocation;
import com.lunarclient.apollo.module.border.Border;
import com.lunarclient.apollo.module.border.BorderModule;
import com.lunarclient.apollo.module.combat.CombatModule;
import com.lunarclient.apollo.module.nametag.Nametag;
import com.lunarclient.apollo.module.nametag.NametagModule;
import com.lunarclient.apollo.module.staffmod.StaffModModule;
import com.lunarclient.apollo.module.team.TeamMember;
import com.lunarclient.apollo.module.team.TeamModule;
import com.lunarclient.apollo.module.waypoint.Waypoint;
import com.lunarclient.apollo.module.waypoint.WaypointModule;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.hooks.clients.Client;
import me.keano.azurite.modules.hooks.clients.ClientHook;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LunarClient extends Module<ClientHook> implements Client {

    private final NametagModule nametagModule;
    private final WaypointModule waypointModule;
    private final TeamModule teamModule;
    private final StaffModModule staffModModule;
    private final BorderModule borderModule;
    private final CombatModule combatModule;

    public LunarClient(ClientHook manager) {
        super(manager);
        this.nametagModule = Apollo.getModuleManager().getModule(NametagModule.class);
        this.waypointModule = Apollo.getModuleManager().getModule(WaypointModule.class);
        this.teamModule = Apollo.getModuleManager().getModule(TeamModule.class);
        this.staffModModule = Apollo.getModuleManager().getModule(StaffModModule.class);
        this.borderModule = Apollo.getModuleManager().getModule(BorderModule.class);
        this.combatModule = Apollo.getModuleManager().getModule(CombatModule.class);
    }

    @Override
    public void overrideNametags(Player target, Player viewer, List<String> tag) {
        Apollo.getPlayerManager().getPlayer(viewer.getUniqueId()).ifPresent(apolloPlayer -> {
            List<Component> components = tag.stream().map(Component::text).collect(Collectors.toList());
            Collections.reverse(components);
            nametagModule.overrideNametag(apolloPlayer, target.getUniqueId(), Nametag.builder().lines(components).build());
        });
    }

    @Override
    public void clearNametags(Player player) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(nametagModule::resetNametags);
    }

    @Override
    public void sendWaypoint(Player player, Location location, WaypointAzurite waypoint, UnaryOperator<String> replacer) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer ->
                waypointModule.displayWaypoint(apolloPlayer, Waypoint.builder()
                        .name(replacer.apply(waypoint.getName()))
                        .hidden(false)
                        .preventRemoval(true)
                        .location(ApolloBlockLocation.builder()
                                .x(location.getBlockX()).y(location.getBlockY()).z(location.getBlockZ())
                                .world(location.getWorld().getName())
                                .build())
                        .color(Color.decode(replacer.apply(waypoint.getColor())))
                        .build()));
    }

    @Override
    public void removeWaypoint(Player player, Location location, WaypointAzurite waypoint, UnaryOperator<String> replacer) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer ->
                waypointModule.removeWaypoint(apolloPlayer, replacer.apply(waypoint.getName()))
        );
    }

    @Override
    public void sendTeamViewer(Player player, PlayerTeam pt) {
        if (!getLunarConfig().getBoolean("TEAM_VIEWER.ENABLED")) return;

        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> {
            List<TeamMember> teamMembers = new ArrayList<>();

            for (Player member : pt.getOnlinePlayers(false)) {
                TeamMember.TeamMemberBuilder builder = TeamMember.builder();
                Location location = member.getLocation();
                Color color = Color.decode(getLunarConfig().getUntranslatedString("TEAM_VIEWER.COLOR"));

                if (getLunarConfig().getBoolean("TEAM_VIEWER.SHOW_DISPLAY_NAME")) {
                    builder.displayName(Component.text(member.getName()));
                }

                if (getLunarConfig().getBoolean("TEAM_VIEWER.PER_CLASS_COLOR.ENABLED")) {
                    PvPClass active = getInstance().getClassManager().getActiveClass(member);

                    if (active != null) {
                        String name = active.getName().toUpperCase();
                        color = Color.decode(getLunarConfig().getUntranslatedString("TEAM_VIEWER.PER_CLASS_COLOR." + name));
                    }
                }

                teamMembers.add(builder.location(ApolloLocation.builder()
                                .x(location.getX()).y(location.getY()).z(location.getZ())
                                .world(member.getWorld().getName())
                                .build())
                        .markerColor(color)
                        .playerUuid(member.getUniqueId())
                        .build());
            }

            teamModule.updateTeamMembers(apolloPlayer, teamMembers);
        });
    }

    @Override
    public void clearTeamViewer(Player player) {
        if (!getLunarConfig().getBoolean("TEAM_VIEWER.ENABLED")) return;
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(teamModule::resetTeamMembers);
    }

    @Override
    public void giveStaffModules(Player player) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(staffModModule::enableAllStaffMods);
    }

    @Override
    public void disableStaffModules(Player player) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(staffModModule::disableAllStaffMods);
    }

    @Override
    public void sendBorderPacket(Player player, Claim claim, Color color) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer ->
                borderModule.displayBorder(apolloPlayer, Border.builder()
                        .id(claim.getTeam().toString())
                        .world(claim.getWorldName())
                        .cancelEntry(true)
                        .bounds(Cuboid2D.builder()
                                .minX(claim.getMinimumX())
                                .minZ(claim.getMinimumZ())
                                .maxX(claim.getMaximumX())
                                .maxZ(claim.getMaximumZ())
                                .build())
                        .color(color)
                        .build())
        );
    }

    @Override
    public void sendRemoveBorderPacket(Player player, UUID id) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer ->
                borderModule.removeBorder(apolloPlayer, id.toString())
        );
    }

    @Override
    public void handleJoin(Player player) {
        Apollo.getPlayerManager().getPlayer(player.getUniqueId()).ifPresent(apolloPlayer -> combatModule.getOptions()
                .set(apolloPlayer, CombatModule.DISABLE_MISS_PENALTY, getLunarConfig().getBoolean("LUNAR_API.FIX_1_8_HIT_DELAY"))
        );
    }
}