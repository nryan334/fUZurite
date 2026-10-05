package me.keano.azurite.modules.listeners.type.team;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.extra.ManageTeamData;
import me.keano.azurite.modules.teams.task.TeamViewerTask;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PlayerTeamListener extends Module<TeamManager> {

    private final Cooldown messageCooldown;
    private final Cooldown netherCooldown;
    private final Cooldown endCooldown;

    private final int netherMinutes;
    private final int endMinutes;

    public PlayerTeamListener(TeamManager manager) {
        super(manager);

        this.messageCooldown = new Cooldown(manager);
        this.netherCooldown = new Cooldown(manager);
        this.endCooldown = new Cooldown(manager);

        this.netherMinutes = getConfig().getInt("DEATH_PORTAL_BAN.NETHER");
        this.endMinutes = getConfig().getInt("DEATH_PORTAL_BAN.END");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent e) {
        Player player = e.getPlayer();

        if (e.getTo().getWorld().getEnvironment() == World.Environment.NETHER && netherCooldown.hasCooldown(player)) {
            e.setCancelled(true);

            if (!messageCooldown.hasCooldown(player)) {
                messageCooldown.applyCooldown(player, 3);
                player.sendMessage(getLanguageConfig().getString("PORTAL_LISTENER.CANNOT_USE_NETHER")
                        .replace("%time%", netherCooldown.getRemaining(player))
                );
            }
            return;
        }

        if (e.getTo().getWorld().getEnvironment() == World.Environment.THE_END && endCooldown.hasCooldown(player)) {
            e.setCancelled(true);

            if (!messageCooldown.hasCooldown(player)) {
                messageCooldown.applyCooldown(player, 3);
                player.sendMessage(getLanguageConfig().getString("PORTAL_LISTENER.CANNOT_USE_NETHER")
                        .replace("%time%", endCooldown.getRemaining(player))
                );
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        ManageTeamData data = getManager().getManageTeamData(player);

        if (data != null) {
            PlayerTeam target = getInstance().getTeamManager().getPlayerTeam(data.getTeam());
            String message = e.getMessage();
            e.setCancelled(true);
            boolean remove = false;

            if (target == null || message.equalsIgnoreCase("cancel")) {
                getManager().getManageTeams().remove(player.getUniqueId());
                player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.CANCELLED"));
                return;
            }

            switch (data.getManageTeamType()) {
                case BALANCE:
                    Integer balance = Utils.parseInt(message.replaceAll("\\$", ""));

                    if (balance == null) {
                        player.sendMessage(Config.NOT_VALID_NUMBER
                                .replace("%number%", message)
                        );
                        break;
                    }

                    remove = true;
                    target.setBalance(balance);
                    target.save();
                    player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.CHANGED_BALANCE")
                            .replace("%team%", target.getName())
                            .replace("%balance%", String.valueOf(balance))
                    );
                    break;

                case DTR:
                    Double dtr = Utils.parseDouble(message);

                    if (dtr == null) {
                        player.sendMessage(Config.NOT_VALID_NUMBER
                                .replace("%number%", message)
                        );
                        break;
                    }

                    remove = true;
                    target.setDtr(dtr);
                    target.save();
                    player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.CHANGED_DTR")
                            .replace("%team%", target.getName())
                            .replace("%dtr%", Formatter.formatDtr(dtr))
                    );
                    break;

                case RENAME:
                    if (getInstance().getTeamManager().getTeam(message) != null) {
                        player.sendMessage(Config.TEAM_ALREADY_EXISTS
                                .replace("%team%", message)
                        );
                        break;
                    }

                    if (Utils.isNotAlphanumeric(message)) {
                        player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.NOT_ALPHANUMERICAL"));
                        break;
                    }

                    if (message.length() < Config.TEAM_NAME_MIN_LENGTH) {
                        player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.MIN_LENGTH")
                                .replace("%amount%", String.valueOf(Config.TEAM_NAME_MIN_LENGTH))
                        );
                        break;
                    }

                    if (message.length() > Config.TEAM_NAME_MAX_LENGTH) {
                        player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.MAX_LENGTH")
                                .replace("%amount%", String.valueOf(Config.TEAM_NAME_MAX_LENGTH))
                        );
                        break;
                    }

                    remove = true;
                    getInstance().getTeamManager().getStringTeams().remove(target.getName());
                    getInstance().getTeamManager().getStringTeams().put(message, target);

                    player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.RENAMED")
                            .replace("%team%", target.getName())
                            .replace("%newname%", message)
                    );

                    target.setName(message);
                    target.save();
                    break;

                case REGEN:
                    Long time = Formatter.parse(message);

                    if (time == null) {
                        player.sendMessage(Config.NOT_VALID_NUMBER
                                .replace("%number%", message)
                        );
                        return;
                    }

                    remove = true;
                    getInstance().getTimerManager().getTeamRegenTimer().applyTimer(target, time);
                    player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.SET_REGEN")
                            .replace("%team%", target.getName())
                            .replace("%time%", Formatter.formatDetailed(time))
                    );
                    break;

                case POINTS:
                    Integer points = Utils.parseInt(message);

                    if (points == null) {
                        player.sendMessage(Config.NOT_VALID_NUMBER
                                .replace("%number%", message)
                        );
                        break;
                    }

                    remove = true;
                    target.setPoints(points);
                    target.save();
                    player.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.TEAM_MANAGE.CHANGED_POINTS")
                            .replace("%team%", target.getName())
                            .replace("%points%", String.valueOf(points))
                    );
                    break;
            }

            if (remove) {
                getManager().getManageTeams().remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        switch (player.getWorld().getEnvironment()) {
            case NETHER:
                if (netherMinutes != -1) netherCooldown.applyCooldown(player, netherMinutes * 60);
                return;

            case THE_END:
                if (endMinutes != -1) endCooldown.applyCooldown(player, endMinutes * 60);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerTeam pt = getManager().getByPlayer(player.getUniqueId());

        if (pt == null) return;

        // Start the task
        if (pt.getTeamViewerTask() == null) {
            pt.setTeamViewerTask(new TeamViewerTask(getManager(), pt.getUniqueID()));
        }

        getInstance().getClientHook().clearTeamViewer(player);
        Tasks.execute(getManager(), () -> getManager().checkTeamSorting(player.getUniqueId()));

        pt.broadcast(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.MEMBER_ONLINE")
                .replace("%player%", player.getName())
        );
        pt.broadcastAlly(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.ALLY_ONLINE")
                .replace("%player%", player.getName())
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        handleQuit(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        handleQuit(e.getPlayer());
    }

    private void handleQuit(Player player) {
        PlayerTeam pt = getManager().getByPlayer(player.getUniqueId());

        if (pt == null) return;

        // Cancel if no members online
        if (pt.getTeamViewerTask() != null && pt.getOnlinePlayersSize(true) == 0) { // Fix
            pt.getTeamViewerTask().cancel();
            pt.setTeamViewerTask(null);
        }

        getInstance().getClientHook().clearTeamViewer(player);
        Tasks.execute(getManager(), () -> getManager().checkTeamSorting(player.getUniqueId()));

        pt.broadcast(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.MEMBER_OFFLINE")
                .replace("%player%", player.getName())
        );
        pt.broadcastAlly(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.ALLY_OFFLINE")
                .replace("%player%", player.getName())
        );
    }
}