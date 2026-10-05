package me.keano.azurite.modules.walls.listener;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.walls.Wall;
import me.keano.azurite.modules.walls.WallManager;
import me.keano.azurite.modules.walls.WallType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class WallListener extends Module<WallManager> {

    public WallListener(WallManager manager) {
        super(manager);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        getManager().getWalls().put(player.getUniqueId(), new Wall(getManager()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.handleQuit(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        this.handleQuit(e.getPlayer());
    }

    private void handleQuit(Player player) {
        getManager().clearTeamMap(player);
        getManager().clearWalls(player);

        // Just to lower ram usage. Will create again once the player rejoins.
        Wall wall = getManager().getWalls().remove(player.getUniqueId());

        if (wall != null) {
            wall.getWalls().clear();
            wall.getTeamMap().clear();
            wall.getLunar().clear();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player player = e.getPlayer();
        Team to = getInstance().getTeamManager().getClaimManager().getTeam(e.getTo());

        if (WallType.EVENT_DENIED.shouldLimit(player, to)) {
            e.setTo(e.getFrom());
            player.sendMessage(getLanguageConfig().getString("WALL_LISTENER.DENIED_EVENT_ENTRY")
                    .replace("%members%", String.valueOf(Config.TEAM_CITADEL_ENTER_LIMIT))
                    .replace("%claim%", to.getDisplayName(player))
            );

        } else if (WallType.CITADEL_DENIED.shouldLimit(player, to)) {
            e.setTo(e.getFrom());
            player.sendMessage(getLanguageConfig().getString("WALL_LISTENER.DENIED_CITADEL_ENTRY")
                    .replace("%members%", String.valueOf(Config.TEAM_CITADEL_ENTER_LIMIT))
                    .replace("%claim%", to.getDisplayName(player))
            );

        } else if (WallType.CONQUEST_DENIED.shouldLimit(player, to)) {
            e.setTo(e.getFrom());
            player.sendMessage(getLanguageConfig().getString("WALL_LISTENER.DENIED_CONQUEST_ENTRY")
                    .replace("%members%", String.valueOf(Config.TEAM_CONQUEST_ENTER_LIMIT))
                    .replace("%claim%", to.getDisplayName(player))
            );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player player = e.getPlayer();
        Team to = getInstance().getTeamManager().getClaimManager().getTeam(e.getTo());

        // Don't do citadel since that has anti-pearl already.

        if (WallType.EVENT_DENIED.shouldLimit(player, to)) {
            e.setCancelled(true);
            getInstance().getTimerManager().getEnderpearlTimer().removeTimer(player);
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            player.sendMessage(getLanguageConfig().getString("WALL_LISTENER.DENIED_EVENT_ENTRY")
                    .replace("%members%", String.valueOf(Config.TEAM_EVENT_ENTER_LIMIT))
                    .replace("%claim%", to.getDisplayName(player)));

        } else if (WallType.CONQUEST_DENIED.shouldLimit(player, to)) {
            e.setCancelled(true);
            getInstance().getTimerManager().getEnderpearlTimer().removeTimer(player);
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            player.sendMessage(getLanguageConfig().getString("WALL_LISTENER.DENIED_CONQUEST_ENTRY")
                    .replace("%members%", String.valueOf(Config.TEAM_CONQUEST_ENTER_LIMIT))
                    .replace("%claim%", to.getDisplayName(player))
            );
        }
    }
}