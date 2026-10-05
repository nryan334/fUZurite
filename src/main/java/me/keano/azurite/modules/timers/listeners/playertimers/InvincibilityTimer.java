package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class InvincibilityTimer extends PlayerTimer {

    private final Cooldown portalCooldown;

    public InvincibilityTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.INVINCIBILITY,
                true,
                "Invincibility",
                "PLAYER_TIMERS.INVINCIBILITY",
                "TIMERS_COOLDOWN.INVINCIBILITY"
        );
        this.portalCooldown = new Cooldown(manager);
    }

    @Override
    public void applyTimer(Player player) {
        super.applyTimer(player);

        Team atApply = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (atApply instanceof SafezoneTeam) {
            pauseTimer(player);
        }
    }

    @Override
    public void applyTimer(Player player, long time) {
        super.applyTimer(player, time);

        Team atApply = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (atApply instanceof SafezoneTeam) {
            pauseTimer(player);
        }
    }

    @Override
    public String getRemainingStringBoard(Player player) {
        return super.getRemainingStringBoard(player);
    }

    public boolean checkEntry(Player player, Team team) {
        if (!hasTimer(player)) return false;

        if (team instanceof PlayerTeam) {
            PlayerTeam pt = (PlayerTeam) team;
            boolean enterOwnClaim = getManager().getConfig().getBoolean("INVINCIBILITY.ENTER_OWN_CLAIM");
            if (enterOwnClaim && pt.getPlayers().contains(player.getUniqueId())) return false;
        }

        return !(team instanceof WildernessTeam) && !(team instanceof RoadTeam) && !(team instanceof SafezoneTeam)
                && !(team instanceof WarzoneTeam);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        if (hasTimer(player)) {
            Team to = getInstance().getTeamManager().getClaimManager().getTeam(e.getTo());
            Team from = getInstance().getTeamManager().getClaimManager().getTeam(e.getFrom());

            if (to instanceof SafezoneTeam) {
                if (!getPausedCache().containsKey(player.getUniqueId())) pauseTimer(player);
                if (to == from) return;

                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.setSaturation(20.0F);

            } else if (from instanceof SafezoneTeam) {
                unpauseTimer(player);
            }

            if (checkEntry(player, to)) {
                e.setTo(e.getFrom());
                player.sendMessage(getLanguageConfig().getString("INVINCIBILITY.CANNOT_ENTER")
                        .replace("%claim%", to.getDisplayName(player))
                );
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleportPause(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        Team to = getInstance().getTeamManager().getClaimManager().getTeam(e.getTo());
        Team from = getInstance().getTeamManager().getClaimManager().getTeam(e.getFrom());

        if (hasTimer(player)) {
            if (to instanceof SafezoneTeam) {
                if (!getPausedCache().containsKey(player.getUniqueId())) pauseTimer(player);
                if (to == from) return;

                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.setSaturation(20.0F);

            } else if (from instanceof SafezoneTeam) {
                unpauseTimer(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFood(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (hasTimer(player)) {
            e.setCancelled(true);
            player.setSaturation(20F);
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Team loginLoc = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (loginLoc instanceof SafezoneTeam) {
            pauseTimer(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (hasTimer(player)) {
            removeTimer(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        Team portalLoc = getInstance().getTeamManager().getClaimManager().getTeam(e.getTo());

        if (e.getTo().getWorld().getEnvironment() != World.Environment.THE_END) return;

        if (hasTimer(player) && !getConfig().getBoolean("INVINCIBILITY.END_ENTRY")) {
            e.setCancelled(true);

            if (portalCooldown.hasCooldown(player)) return;

            player.sendMessage(getLanguageConfig().getString("INVINCIBILITY.DENIED_END_ENTRY"));
            portalCooldown.applyCooldown(player, 3);
        }

        if (portalLoc instanceof SafezoneTeam) {
            pauseTimer(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player)) return;

        Player target = (Player) e.getTarget();

        if (hasTimer(target)) {
            e.setTarget(null);
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        // Want them able to die in end.
        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) return;

        Player player = (Player) e.getEntity();

        if (hasTimer(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player player = e.getPlayer();
        Team to = getInstance().getTeamManager().getClaimManager().getTeam(e.getTo());

        if (checkEntry(player, to)) {
            e.setCancelled(true);
            getManager().getEnderpearlTimer().removeTimer(player);
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            player.sendMessage(getLanguageConfig().getString("INVINCIBILITY.CANNOT_TELEPORT")
                    .replace("%claim%", to.getDisplayName(player))
            );
        }
    }
}