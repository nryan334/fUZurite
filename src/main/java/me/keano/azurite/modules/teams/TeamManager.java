package me.keano.azurite.modules.teams;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.teams.claims.ClaimManager;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.extra.ManageTeamData;
import me.keano.azurite.modules.teams.extra.TeamSorting;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.listeners.servertimers.anticlean.AntiCleanTask;
import me.keano.azurite.modules.timers.listeners.servertimers.anticlean.AntiCleanTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class TeamManager extends Manager {

    private final Map<UUID, Team> teams;
    private final Map<UUID, Team> systemTeams;
    private final Map<String, Team> stringTeams;
    private final Map<UUID, PlayerTeam> playerTeams;
    private final Map<UUID, ManageTeamData> manageTeams;

    private final ClaimManager claimManager;
    private final TeamSorting teamSorting;

    public TeamManager(HCF instance) {
        super(instance);

        this.teams = new ConcurrentHashMap<>();
        this.systemTeams = new ConcurrentHashMap<>();
        this.playerTeams = new ConcurrentHashMap<>();
        this.manageTeams = new HashMap<>();
        this.stringTeams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        this.claimManager = new ClaimManager(this);
        this.teamSorting = new TeamSorting(this);
    }

    @Override
    public void disable() {
        for (Team team : systemTeams.values()) {
            team.clearWebs();
        }
    }

    public Team getTeam(String name) {
        return stringTeams.get(name);
    }

    public Team getTeam(UUID uuid) {
        return teams.get(uuid);
    }

    public MountainTeam getMountainTeam(String name) {
        Team team = getTeam(name);
        return (team instanceof MountainTeam ? (MountainTeam) team : null);
    }

    public CitadelTeam getCitadelTeam(String name) {
        Team team = getTeam(name);
        return (team instanceof CitadelTeam ? (CitadelTeam) team : null);
    }

    public PlayerTeam getPlayerTeam(String name) {
        Team team = stringTeams.get(name);
        return (team instanceof PlayerTeam ? (PlayerTeam) team : null);
    }

    public PlayerTeam getPlayerTeam(UUID uuid) {
        Team team = teams.get(uuid);
        return (team instanceof PlayerTeam ? (PlayerTeam) team : null);
    }

    public PlayerTeam getByPlayerOrTeam(String name) {
        Team team = stringTeams.get(name);

        // Always return the team first.
        if (team instanceof PlayerTeam) {
            return (PlayerTeam) team;
        }

        // If no team found return the players team.
        User user = getInstance().getUserManager().getByName(name);
        return (user != null ? playerTeams.get(user.getUniqueID()) : null);
    }

    public Team getFocus(String name) {
        Team team = stringTeams.get(name);

        if (team != null) {
            return team;
        }

        // If no team found return the players team.
        User user = getInstance().getUserManager().getByName(name);
        return (user != null ? playerTeams.get(user.getUniqueID()) : null);
    }

    public PlayerTeam getByPlayer(UUID uuid) {
        return playerTeams.get(uuid);
    }

    public ManageTeamData getManageTeamData(Player player) {
        return manageTeams.get(player.getUniqueId());
    }

    public void checkTeamSorting(UUID uuid) {
        PlayerTeam pt = this.getByPlayer(uuid);

        if (pt != null && pt.getOnlinePlayersSize(false) <= 0) {
            teamSorting.removeList(pt);
        } else {
            teamSorting.sort();
        }
    }

    public void handleDeath(Player player, Player killer, String deathMessage) {
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        PlayerTeam pt = getByPlayer(player.getUniqueId());
        Team at = claimManager.getTeam(player.getLocation());
        World.Environment environment = player.getWorld().getEnvironment();

        int deathPoints = getTeamConfig().getInt("TEAM_POINTS.DEATH");
        int killPoints = getTeamConfig().getInt("TEAM_POINTS.KILLS");
        boolean madeRaidable = false; // Used to check if the player made them raidable
        Date date = new Date();
        PlayerInventory inventory = player.getInventory();
        Location location = player.getLocation();

        user.setDeaths(user.getDeaths() + 1); // increment
        user.setKillstreak(0);
        user.getInventories().add(new StoredInventory(inventory.getContents(), inventory.getArmorContents(), date));
        user.getLastDeaths().add(Config.LAST_DEATH_STRING
                .replace("%deathmessage%", deathMessage)
                .replace("%date%", Formatter.formatDate(date))
                .replace("%x%", String.valueOf(location.getBlockX()))
                .replace("%y%", String.valueOf(location.getBlockY()))
                .replace("%z%", String.valueOf(location.getBlockZ())));
        if (user.getLastDeaths().size() > Config.LAST_DEATHS_LIMIT) user.getLastDeaths().remove(0);
        if (user.getInventories().size() > Config.RESTORE_LIMIT) user.getInventories().remove(0);
        user.save();

        if (getConfig().getBoolean("DEATH_COMMANDS.ENABLED")) {
            for (String s : getConfig().getStringList("DEATH_COMMANDS.COMMANDS")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s
                        .replace("%player%", player.getName())
                );
            }
        }

        if (pt != null) {
            pt.setPoints(getTeamConfig().getBoolean("TEAMS.ALLOW_NEGATIVE_POINTS") ? pt.getPoints() - deathPoints : Math.max(pt.getPoints() - deathPoints, 0));
            pt.setDeaths(pt.getDeaths() + 1); // increment the teams deaths aswell.

            double dtrTake;

            if (at instanceof CitadelTeam) {
                dtrTake = Config.DTR_TAKE_DEATH_CITADEL;

            } else if (at instanceof EventTeam) {
                dtrTake = Config.DTR_TAKE_DEATH_EVENTS;

            } else if (environment == World.Environment.NETHER) {
                dtrTake = Config.DTR_TAKE_DEATH_NETHER;

            } else if (environment == World.Environment.THE_END) {
                dtrTake = Config.DTR_TAKE_DEATH_END;

            } else {
                dtrTake = Config.DTR_TAKE_DEATH_NORMAL;
            }

            madeRaidable = !pt.isRaidable() && (pt.getDtr() - dtrTake <= 0);
            pt.setDtr(pt.getDtr() - Config.DTR_TAKE_DEATH_NORMAL);

            // We do not want to override the timer from EOTW
            if (!getInstance().getEotwManager().isActive()) {
                getInstance().getTimerManager().getTeamRegenTimer().applyTimer(pt);
            }

            pt.save();

            for (String s : getLanguageConfig().getStringList("PLAYER_TEAM_LISTENER.MEMBER_DEATH"))
                pt.broadcast(s
                        .replace("%player%", player.getName())
                        .replace("%dtr%", pt.getDtrString())
                );

           /* pt.broadcast(getLanguageConfig().getString("DEATH_LISTENER.TEAMS_MESSAGES.LOST_POINTS")
                    .replace("%points%", String.valueOf(deathPoints))
                    .replace("%player%", player.getName())
            );*/
        }

        if (killer != null && killer != player) {
            User userKiller = getInstance().getUserManager().getByUUID(killer.getUniqueId());
            PlayerTeam ptKiller = getByPlayer(killer.getUniqueId());
            Location killerLoc = killer.getLocation();

            userKiller.setKills(userKiller.getKills() + 1);
            userKiller.setKillstreak(userKiller.getKillstreak() + 1);

            if (userKiller.getKillstreak() > userKiller.getHighestKillstreak()) {
                userKiller.setHighestKillstreak(userKiller.getKillstreak());
            }

            userKiller.getLastKills().add(Config.LAST_KILL_STRING
                    .replace("%deathmessage%", deathMessage)
                    .replace("%date%", Formatter.formatDate(date))
                    .replace("%x%", String.valueOf(killerLoc.getBlockX()))
                    .replace("%y%", String.valueOf(killerLoc.getBlockY()))
                    .replace("%z%", String.valueOf(killerLoc.getBlockZ())));

            if (userKiller.getLastKills().size() > Config.LAST_KILLS_LIMIT) userKiller.getLastKills().remove(0);
            userKiller.save();
            getInstance().getKillstreakManager().checkKills(killer, userKiller.getKillstreak());

            if (getConfig().getBoolean("KILL_COMMANDS.ENABLED")) {
                for (String s : getConfig().getStringList("KILL_COMMANDS.COMMANDS")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s
                            .replace("%player%", killer.getName())
                    );
                }
            }

            if (ptKiller != null) {
                ptKiller.setPoints(ptKiller.getPoints() + killPoints);
                ptKiller.setKills(ptKiller.getKills() + 1);
                if (madeRaidable) {
                    ptKiller.setRaidablePoints(ptKiller.getRaidablePoints() + 1);
                    getInstance().getTimerManager().getAntiRaidTimer().applyTimer(pt, ptKiller);
                }
                ptKiller.save();

                /*ptKiller.broadcast(getLanguageConfig().getString("DEATH_LISTENER.TEAMS_MESSAGES.GAINED_POINTS")
                        .replace("%points%", String.valueOf(killPoints))
                        .replace("%player%", player.getName())
                );*/
            }
        }
    }

    public boolean canBuild(Player player, Location from) {
        return canBuild(player, from, false);
    }

    public boolean canBuild(Player player, Location from, boolean breakBlock) {
        if (player.getGameMode() == GameMode.CREATIVE) return true;
        if (player.getWorld().getEnvironment() == World.Environment.THE_END) return false;

        Team atBuild = claimManager.getTeam(from);

        if (atBuild instanceof PlayerTeam) {
            PlayerTeam pt = (PlayerTeam) atBuild;
            if (pt.getPlayers().contains(player.getUniqueId())) return true;

            if (pt.isRaidable()) {
                PlayerTeam breaker = getByPlayer(player.getUniqueId());
                return getInstance().getTimerManager().getAntiRaidTimer().canBreak(pt, breaker);
            }
        }

        if (atBuild instanceof WarzoneTeam) {
            WarzoneTeam wt = (WarzoneTeam) atBuild;
            if (wt.canBreak(from)) return true;
        }

        if (breakBlock && atBuild instanceof MountainTeam) { // ONLY ALLOW BREAKING
            MountainTeam mt = (MountainTeam) atBuild;
            if (mt.getAllowedBreak().contains(from.getBlock().getType())) return true;
        }

        if (breakBlock && atBuild instanceof SafezoneTeam) { // ONLY ALLOW BREAKING
            SafezoneTeam st = (SafezoneTeam) atBuild;
            if (st.canMine(from.getBlock())) return true;
        }

        return atBuild instanceof WildernessTeam;
    }

    // Really mess but works really well - Only one listener needed because of this
    public boolean canHit(Player damager, Player damaged, boolean message) {
        PlayerTeam damagerTeam = getByPlayer(damager.getUniqueId());
        PlayerTeam damagedTeam = getByPlayer(damaged.getUniqueId());
        TimerManager timerManager = getInstance().getTimerManager();
        StaffManager staffManager = getInstance().getStaffManager();

        if (staffManager.isStaffEnabled(damager) || staffManager.isVanished(damager)) {
            return false;

        } else if (staffManager.isStaffEnabled(damaged) || staffManager.isVanished(damaged)) {
            return false;

        } else if (staffManager.isFrozen(damager)) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("STAFF_MODE.HIT_DENIED"));
            return false;

        } else if (staffManager.isFrozen(damaged)) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("STAFF_MODE.HIT_FROZEN")
                    .replace("%player%", damaged.getName())
            );
            return false;

        } else if (getInstance().getSotwManager().isActive() && !getInstance().getSotwManager().isEnabled(damaged)) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("SOTW_TIMER.DAMAGED_ATTACK")
                    .replace("%player%", damaged.getName())
            );
            return false;

        } else if (getInstance().getSotwManager().isActive() && !getInstance().getSotwManager().isEnabled(damager)) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("SOTW_TIMER.DAMAGER_ATTACK")
                    .replace("%player%", damaged.getName())
            );
            return false;

        } else if (timerManager.getPvpTimer().hasTimer(damager)) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("PVP_TIMER.DAMAGER_ATTACK")
                    .replace("%player%", damaged.getName())
            );
            return false;

        } else if (timerManager.getPvpTimer().hasTimer(damaged)) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("PVP_TIMER.DAMAGED_ATTACK")
                    .replace("%player%", damaged.getName())
            );
            return false;

        } else if (timerManager.getInvincibilityTimer().hasTimer(damager)) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("INVINCIBILITY.DAMAGER_ATTACK")
                    .replace("%player%", damaged.getName())
            );
            return false;

        } else if (timerManager.getInvincibilityTimer().hasTimer(damaged)) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("INVINCIBILITY.DAMAGED_ATTACK")
                    .replace("%player%", damaged.getName())
            );
            return false;

        } else if (getClaimManager().getTeam(damager.getLocation()).getType() == TeamType.SAFEZONE) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("TEAM_LISTENER.DAMAGER_ATTACK")
                    .replace("%player%", damaged.getName())
            );
            return false;

        } else if (getClaimManager().getTeam(damaged.getLocation()).getType() == TeamType.SAFEZONE) {
            if (!message) return false;

            damager.sendMessage(getLanguageConfig().getString("TEAM_LISTENER.DAMAGED_ATTACK")
                    .replace("%player%", damaged.getName())
            );
            return false;
        }

        if (Config.ANTICLEAN_ENABLED) {
            AntiCleanTimer antiClean = getInstance().getTimerManager().getAntiCleanTimer();
            // We don't have anti-clean on capzone or from capzone
            boolean canHit = antiClean.canHit(damager, damaged);

            // Tasks
            AntiCleanTask damagedTask;
            AntiCleanTask damagerTask;

            // Teams
            PlayerTeam antiCleanDamager = null;
            PlayerTeam antiCleanDamaged = null;

            // Cooldowns
            long antiCleanCooldownDamager = 0L;
            long antiCleanCooldownDamaged = 0L;

            if (damagedTeam != null && damagedTeam.getAntiCleanTask() != null) {
                damagedTask = damagedTeam.getAntiCleanTask();
                antiCleanDamaged = getPlayerTeam(damagedTask.getTarget());
                antiCleanCooldownDamaged = damagedTask.getRemaining();
            }

            if (damagerTeam != null && damagerTeam.getAntiCleanTask() != null) {
                damagerTask = damagerTeam.getAntiCleanTask();
                antiCleanDamager = getPlayerTeam(damagerTask.getTarget());
                antiCleanCooldownDamager = damagerTask.getRemaining();
            }

            if (damagedTeam != null && antiCleanDamaged != null && antiCleanDamaged != damagerTeam && antiCleanCooldownDamaged > 0L && canHit) {
                if (!message) return false;

                damager.sendMessage(getLanguageConfig().getString("ANTICLEAN_TIMER.CANNOT_HIT_DAMAGED")
                        .replace("%team%", damagedTeam.getName())
                        .replace("%team2%", antiCleanDamaged.getName())
                );
                return false;
            }

            if (damagerTeam != null && antiCleanDamager != null && antiCleanDamager != damagedTeam && antiCleanCooldownDamager > 0L && canHit) {
                if (!message) return false;

                damager.sendMessage(getLanguageConfig().getString("ANTICLEAN_TIMER.CANNOT_HIT_DAMAGER")
                        .replace("%team%", antiCleanDamager.getName())
                );
                return false;
            }
        }

        if (damagerTeam != null && damagedTeam != null) {
            if (damagerTeam == damagedTeam && !damagerTeam.isFriendlyFire()) {
                if (!message) return false;

                damager.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.MEMBER_HURT")
                        .replace("%player%", damaged.getName())
                        .replace("%role%", damagedTeam.getMember(damaged.getUniqueId()).getAsterisk())
                );
                return false;
            }

            if (damagerTeam.getAllies().contains(damagedTeam.getUniqueID())) {
                if (!message) return false;

                damager.sendMessage(getLanguageConfig().getString("PLAYER_TEAM_LISTENER.ALLY_HURT")
                        .replace("%player%", damaged.getName())
                );
                return false;
            }
        }

        return true;
    }
}