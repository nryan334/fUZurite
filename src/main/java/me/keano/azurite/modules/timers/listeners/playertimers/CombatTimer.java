package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CombatTimer extends PlayerTimer {

    private final Cooldown portalCooldown;
    private List<String> blockedCommands;

    public CombatTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.COMBAT,
                false,
                "Combat",
                "PLAYER_TIMERS.COMBAT_TAG",
                "TIMERS_COOLDOWN.COMBAT_TAG"
        );
        this.portalCooldown = new Cooldown(manager);
        this.blockedCommands = getConfig().getStringList("COMBAT_TIMER.BLOCKED_COMMANDS");
    }

    @Override
    public void reload() {
        this.blockedCommands = getConfig().getStringList("COMBAT_TIMER.BLOCKED_COMMANDS");
    }

    @Override // Combat timer is usually formatted differently.
    public String getRemainingStringBoard(Player player) {
        Long paused = pausedCache.get(player.getUniqueId());

        if (paused != null) {
            return Formatter.getRemaining(paused, false);
        }

        long rem = timerCache.get(player.getUniqueId()) - System.currentTimeMillis();
        return Formatter.getRemaining(rem, false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = Utils.getDamager(e.getDamager());
        Player damaged = (Player) e.getEntity();

        if (damager == null) return;
        if (damager == damaged) return;

        if (!hasTimer(damager))
            damager.sendMessage(getLanguageConfig().getString("COMBAT_TIMER.TAGGED")
                    .replace("%seconds%", String.valueOf(seconds))
            );

        if (!hasTimer(damaged))
            damaged.sendMessage(getLanguageConfig().getString("COMBAT_TIMER.TAGGED")
                    .replace("%seconds%", String.valueOf(seconds))
            );

        applyTimer(damager);
        applyTimer(damaged);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();

        if (hasTimer(player) && !player.hasPermission("azurite.combatblock.bypass")) {
            if (message.contains(" ")) {
                String[] split = message.split(" ");

                for (String blockedCommand : blockedCommands) {
                    if (!split[0].equalsIgnoreCase(blockedCommand)) continue;
                    e.setCancelled(true);
                    player.sendMessage(getLanguageConfig().getString("COMBAT_TIMER.BLOCKED_COMMAND"));
                    break;
                }

            } else {
                for (String blockedCommand : blockedCommands) {
                    if (!e.getMessage().equalsIgnoreCase(blockedCommand)) continue;
                    e.setCancelled(true);
                    player.sendMessage(getLanguageConfig().getString("COMBAT_TIMER.BLOCKED_COMMAND"));
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onDie(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (hasTimer(player)) {
            Tasks.executeLater(getManager(), 10L, () -> removeTimer(player));
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        Player player = e.getPlayer();

        if (e.getTo().getWorld().getEnvironment() != World.Environment.THE_END) return;

        if (hasTimer(player) && !getConfig().getBoolean("COMBAT_TIMER.END_ENTRY")) {
            e.setCancelled(true);

            if (portalCooldown.hasCooldown(player)) return;

            player.sendMessage(getLanguageConfig().getString("COMBAT_TIMER.DENIED_END_ENTRY"));
            portalCooldown.applyCooldown(player, 3);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player player = e.getPlayer();

        if (hasTimer(player)) {
            Team to = getInstance().getTeamManager().getClaimManager().getTeam(e.getTo());

            if (to instanceof SafezoneTeam) {
                e.setTo(e.getFrom());
                player.sendMessage(getLanguageConfig().getString("COMBAT_TIMER.CANNOT_ENTER"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) return;
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        Player player = e.getPlayer();

        if (hasTimer(player)) {
            Team to = getInstance().getTeamManager().getClaimManager().getTeam(e.getTo());

            if (to instanceof SafezoneTeam) {
                e.setCancelled(true);
                getManager().getEnderpearlTimer().removeTimer(player);
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                player.sendMessage(getLanguageConfig().getString("COMBAT_TIMER.CANNOT_TELEPORT"));
            }
        }
    }
}