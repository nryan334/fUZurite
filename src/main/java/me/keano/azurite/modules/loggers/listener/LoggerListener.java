package me.keano.azurite.modules.loggers.listener;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.loggers.Logger;
import me.keano.azurite.modules.loggers.LoggerManager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LoggerListener extends Module<LoggerManager> {

    public LoggerListener(LoggerManager manager) {
        super(manager);
        this.load();
    }

    private void load() {
        if (Utils.isModernVer()) {
            getManager().registerListener(new Listener() {
                @EventHandler
                public void onInteract(PlayerInteractAtEntityEvent e) {
                    Entity entity = e.getRightClicked();

                    if (!(entity instanceof Villager)) return;
                    if (!getManager().getLoggers().containsKey(entity.getUniqueId())) return;

                    e.setCancelled(true);
                }

                @EventHandler
                public void onEntitySpawn(ChunkLoadEvent e) {
                    for (Entity entity : e.getChunk().getEntities()) {
                        if (!(entity instanceof Villager)) continue;

                        Logger logger = getManager().getLoggers().remove(entity.getUniqueId());

                        if (logger != null) {
                            entity.remove();
                            logger.cancel();
                        }
                    }
                }
            });

        } else {
            getManager().registerListener(new Listener() {
                @EventHandler // if they try trading with a villager
                public void onInteract(PlayerInteractEntityEvent e) {
                    Entity entity = e.getRightClicked();

                    if (!(entity instanceof Villager)) return;
                    if (!getManager().getLoggers().containsKey(entity.getUniqueId())) return;

                    e.setCancelled(true);
                }
            });
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Villager)) return;
        Logger logger = getManager().getLoggers().remove(entity.getUniqueId());
        if (logger == null) return;

        Player player = logger.getPlayer();
        Player killer = e.getEntity().getKiller();
        getManager().getPlayerLoggers().remove(player.getUniqueId());

        // Set the drops
        e.getDrops().clear();
        e.getDrops().addAll(Arrays.stream(logger.getContents()).filter(Objects::nonNull).collect(Collectors.toList()));
        e.getDrops().addAll(Arrays.stream(logger.getArmorContents()).filter(Objects::nonNull).collect(Collectors.toList()));
        e.setDroppedExp((int) logger.getExp());

        // Save the inventory for /restore
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        user.getInventories().add(new StoredInventory(logger.getContents(), logger.getArmorContents(), new Date()));
        if (user.getInventories().size() > Config.RESTORE_LIMIT) user.getInventories().remove(0);
        user.save();

        String message = getDeathMessage(logger, killer);
        Bukkit.broadcastMessage(message);

        // Handle logger death / deathban of the player. / Points and kills/deaths adding.
        getInstance().getVersionManager().getVersion().handleLoggerDeath(logger);
        if (killer != null) getInstance().getTeamManager().handleDeath(player, killer, message);
        if (!getInstance().isKits()) getInstance().getDeathbanManager().applyDeathban(player);
    }

    private String getDeathMessage(Logger logger, Player killer) {
        if (killer != null) {
            return Config.DEATH_LOGGER_KILLER
                    .replace("%player%", format(logger.getPlayer(), false))
                    .replace("%killer%", format(killer, true));

        } else {
            return Config.DEATH_LOGGER
                    .replace("%player%", format(logger.getPlayer(), false));
        }
    }

    private String format(Player player, boolean killer) {
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        return Config.DEATH_FORMAT
                .replace("%player%", player.getName())
                .replace("%kills%", String.valueOf(user.getKills() + (killer ? 1 : 0)));
    }

    @EventHandler // activating a pressure plate
    public void onInteract(EntityInteractEvent e) {
        Entity entity = e.getEntity();

        if (e.getBlock().getType() != ItemUtils.getMat("STONE_PLATE")) return; // Can only activate stone
        if (!(entity instanceof Villager)) return;
        if (!getManager().getLoggers().containsKey(entity.getUniqueId())) return;

        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true) // Deny teammates hitting in sotw timer etc...
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();

        if (!(entity instanceof Villager)) return;
        Logger logger = getManager().getLoggers().get(entity.getUniqueId());
        if (logger == null) return;
        Player damager = Utils.getDamager(e.getDamager());
        if (damager == null) return;

        // Don't let teammates damage team loggers or in sotw etc..
        if (!getInstance().getTeamManager().canHit(damager, logger.getPlayer(), false)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true) // Entering portals
    public void onPortal(EntityPortalEvent e) {
        Entity entity = e.getEntity();

        if (!(entity instanceof Villager)) return;
        if (!getManager().getLoggers().containsKey(entity.getUniqueId())) return;

        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true) // if they get pushed into a minecart or something
    public void onEnter(VehicleEnterEvent e) {
        Entity entity = e.getEntered();

        if (!(entity instanceof Villager)) return;
        if (!getManager().getLoggers().containsKey(entity.getUniqueId())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onUnload(ChunkUnloadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            if (!(entity instanceof Villager)) continue;

            Logger logger = getManager().getLoggers().remove(entity.getUniqueId());

            if (logger != null) {
                entity.remove();
                logger.cancel();
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Team team = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
        TimerManager timerManager = getInstance().getTimerManager();

        if (player.isDead()) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (player.hasMetadata("loggedout")) return; // Used /logout

        if (team instanceof SafezoneTeam) return;

        if (getInstance().getDeathbanManager().isDeathbanned(player)) return; // Don't spawn in
        if (getInstance().getStaffManager().isVanished(player)) return;
        if (getInstance().getStaffManager().isStaffEnabled(player)) return;
        if (getInstance().getSotwManager().isActive() && !getInstance().getSotwManager().isEnabled(player)) return;

        if (timerManager.getPvpTimer().hasTimer(player)) return;
        if (timerManager.getInvincibilityTimer().hasTimer(player)) return;

        Logger logger = new Logger(getManager(), player);
        getManager().getLoggers().put(logger.getVillagerUUID(), logger);
        getManager().getPlayerLoggers().put(player.getUniqueId(), logger);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Logger logger = getManager().getPlayerLoggers().remove(player.getUniqueId());

        if (logger != null) {
            logger.cancel();
            getManager().getLoggers().remove(logger.getVillagerUUID());

            Tasks.executeLater(getManager(), 5L, () -> {
                Villager villager = logger.getVillager();

                if (villager != null) {
                    villager.remove();
                }
            });
        }
    }
}