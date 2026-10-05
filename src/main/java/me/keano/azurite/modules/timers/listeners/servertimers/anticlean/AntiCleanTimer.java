package me.keano.azurite.modules.timers.listeners.servertimers.anticlean;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.timers.Timer;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AntiCleanTimer extends Timer {

    private final Map<UUID, AntiCleanStats> antiCleanStats;
    private List<String> deniedWorlds;
    private int membersRequired;

    public AntiCleanTimer(TimerManager manager) {
        super(
                manager,
                "AntiClean",
                "",
                "ANTI_CLEAN.COOLDOWN"
        );
        this.antiCleanStats = new HashMap<>();
        this.deniedWorlds = getConfig().getStringList("ANTI_CLEAN.DENIED_WORLDS");
        this.membersRequired = getConfig().getInt("ANTI_CLEAN.MEMBERS_REQUIRED");
    }

    @Override
    public void reload() {
        this.deniedWorlds = getConfig().getStringList("ANTI_CLEAN.DENIED_WORLDS");
        this.membersRequired = getConfig().getInt("ANTI_CLEAN.MEMBERS_REQUIRED");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!Config.ANTICLEAN_ENABLED) return;

        Player damaged = (Player) e.getEntity();
        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;
        if (damaged == damager) return;
        if (!canHit(damaged, damager)) return;

        PlayerTeam damagedTeam = getInstance().getTeamManager().getByPlayer(damaged.getUniqueId());
        PlayerTeam damagerTeam = getInstance().getTeamManager().getByPlayer(damager.getUniqueId());

        // Tasks
        AntiCleanTask damagedTask;
        AntiCleanTask damagerTask;

        // Teams
        PlayerTeam antiCleanDamaged = null;

        // Cooldowns
        long antiCleanCooldownDamager = 0L;
        long antiCleanCooldownDamaged = 0L;

        if (damagedTeam != null && damagedTeam.getAntiCleanTask() != null) {
            damagedTask = damagedTeam.getAntiCleanTask();
            antiCleanDamaged = getInstance().getTeamManager().getPlayerTeam(damagedTask.getTarget());
            antiCleanCooldownDamaged = damagedTask.getRemaining();
        }

        if (damagerTeam != null && damagerTeam.getAntiCleanTask() != null) {
            damagerTask = damagerTeam.getAntiCleanTask();
            antiCleanCooldownDamager = damagerTask.getRemaining();
        }

        // Update Anti-Clean
        if (damagedTeam != null && damagerTeam != null &&
                // Update if no anti-clean
                (antiCleanCooldownDamager <= 0L && antiCleanCooldownDamaged <= 0L) ||
                // Update if the team doesn't exist anymore
                (damagedTeam != null && damagerTeam != null && antiCleanDamaged == null) ||
                // Update if anti-clean but same team
                (antiCleanCooldownDamager > 0L && antiCleanCooldownDamaged > 0L && antiCleanDamaged == damagerTeam)) {

            // Member Check
            if (damagedTeam.getOnlinePlayersSize(false) < membersRequired) return;
            if (damagerTeam.getOnlinePlayersSize(false) < membersRequired) return;
            if (damagerTeam == damagedTeam) return;

            // Increment hits and damage
            double damage = e.getDamage();

            // Damage Dealt
            AntiCleanStats damagerStats = antiCleanStats.getOrDefault(damagerTeam.getUniqueID(), new AntiCleanStats());
            damagerStats.incrementInt(damager, damagerStats.getHits());
            damagerStats.incrementDouble(damager, damagerStats.getDamageDealt(), damage);
            antiCleanStats.put(damagerTeam.getUniqueID(), damagerStats);
            antiCleanStats.putIfAbsent(damagedTeam.getUniqueID(), new AntiCleanStats());

            // Update if active
            if (damagedTeam.getAntiCleanTask() != null) {
                damagedTeam.getAntiCleanTask().updateTime(seconds);

            } else {
                // Create new
                damagedTeam.setAntiCleanTask(new AntiCleanTask(getManager(), damagedTeam.getUniqueID(), damagerTeam.getUniqueID(), seconds));
            }

            // Update if active
            if (damagerTeam.getAntiCleanTask() != null) {
                damagerTeam.getAntiCleanTask().updateTime(seconds);

            } else {
                // Create new
                damagerTeam.setAntiCleanTask(new AntiCleanTask(getManager(), damagerTeam.getUniqueID(), damagedTeam.getUniqueID(), seconds));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEat(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();

        if (item.getType() != Material.GOLDEN_APPLE || getManager().isGapple(item)) return;

        Player player = e.getPlayer();
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt != null) {
            AntiCleanStats stats = antiCleanStats.get(pt.getUniqueID());
            if (stats != null) stats.incrementInt(player, stats.getGoldenApples());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPot(PotionSplashEvent e) {
        Player player = Utils.getDamager(e.getPotion());
        ItemStack health = ItemUtils.tryGetPotion(getManager(), ItemUtils.getMat("SPLASH_POTION"), 16421);

        if (player == null) return;
        if (!e.getPotion().getItem().isSimilar(health)) return;

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt != null) {
            AntiCleanStats stats = antiCleanStats.get(pt.getUniqueID());
            if (stats != null) stats.incrementInt(player, stats.getPotsUsed());
        }
    }

    public void incrementDamage(Player player, double amount) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt != null) {
            AntiCleanStats stats = antiCleanStats.get(pt.getUniqueID());
            if (stats != null) stats.incrementDouble(player, stats.getDamageDealt(), amount);
        }
    }

    public void incrementArcherTag(Player player) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt != null) {
            AntiCleanStats stats = antiCleanStats.get(pt.getUniqueID());
            if (stats != null) stats.incrementInt(player, stats.getArcherTags());
        }
    }

    public void sendStats(PlayerTeam pt) {
        AntiCleanStats stats = antiCleanStats.remove(pt.getUniqueID());

        if (stats == null) return;

        List<Pair<UUID, Integer>> topHits = stats.getTopHits();
        int totalHits = stats.getTotalInt(stats.getHits());
        double totalDamage = stats.getTotalDouble(stats.getDamageDealt());

        for (Player member : pt.getOnlinePlayers(true)) {
            for (String string : Config.ANTICLEAN_STATS_MESSAGE) {
                member.sendMessage(string
                        // Top Hits
                        .replace("%tophits1%", stats.getTopHitsMessage(getManager(), 1, topHits))
                        .replace("%tophits2%", stats.getTopHitsMessage(getManager(), 2, topHits))
                        .replace("%tophits3%", stats.getTopHitsMessage(getManager(), 3, topHits))
                        .replace("%tophits4%", stats.getTopHitsMessage(getManager(), 4, topHits))
                        .replace("%tophits5%", stats.getTopHitsMessage(getManager(), 5, topHits))

                        // User Stats
                        .replace("%hits%", String.valueOf(stats.getStatInt(member, stats.getHits())))
                        .replace("%apples%", String.valueOf(stats.getStatInt(member, stats.getGoldenApples())))
                        .replace("%potions%", String.valueOf(stats.getStatInt(member, stats.getPotsUsed())))
                        .replace("%archertags%", String.valueOf(stats.getStatInt(member, stats.getArcherTags())))
                        .replace("%damage%", Formatter.formatBardEnergy(stats.getStatDouble(member, stats.getDamageDealt())))

                        // Total
                        .replace("%totalhits%", String.valueOf(totalHits))
                        .replace("%totaldamage%", Formatter.formatBardEnergy(totalDamage))
                );
            }
        }
    }

    public boolean canHit(Player damager, Player damaged) {
        if (damaged == damager) return false;
        if (checkRadius(damaged.getLocation()) || checkRadius(damager.getLocation())) return false;
        if (checkWorld(damager.getLocation()) || checkWorld(damaged.getLocation())) return false;
        if (getInstance().getDeathbanManager().isDeathbanned(damager)) return false;
        if (getInstance().getDeathbanManager().isDeathbanned(damaged)) return false;
        if (getInstance().getKothManager().isAntiCleanRadius(damager.getLocation())) return false;
        if (getInstance().getKothManager().isAntiCleanRadius(damaged.getLocation())) return false;
        if (getInstance().getKothManager().getZone(damaged.getLocation()) != null) return false;
        return getInstance().getKothManager().getZone(damager.getLocation()) == null;
    }

    public boolean checkWorld(Location location) {
        return deniedWorlds.contains(location.getWorld().getName());
    }

    public boolean checkRadius(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return (x > Config.ANTICLEAN_RADIUS || x < -Config.ANTICLEAN_RADIUS
                || z > Config.ANTICLEAN_RADIUS || z < -Config.ANTICLEAN_RADIUS);
    }
}