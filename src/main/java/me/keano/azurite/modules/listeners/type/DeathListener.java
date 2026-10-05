package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.ability.type.FocusModeAbility;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.killtag.Killtag;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.pvpclass.type.rogue.RogueBackstabEvent;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class DeathListener extends Module<ListenerManager> {

    private static final SimpleDateFormat STATTRACK_FORMAT = new SimpleDateFormat("dd.MM.yy");
    private static final SimpleDateFormat STATTRACK_FORMAT_TIME = new SimpleDateFormat("HH:mm");

    public DeathListener(ListenerManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.LOW) // call first
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        EntityDamageEvent.DamageCause cause = (player.getLastDamageCause() == null ?
                EntityDamageEvent.DamageCause.SUICIDE :
                player.getLastDamageCause().getCause());

        // Don't do anything if they were deathbanned.
        if (getInstance().getDeathbanManager().isDeathbanned(player)) {
            e.setDeathMessage(null); // we don't want messages from the arena
            e.setDroppedExp(0);
            e.getDrops().clear();
            return;
        }

        String message;
        Player killer;
        boolean handleStats;

        player.getWorld().strikeLightningEffect(player.getLocation()); // make sure we strike lightning

        if (player.getLastDamageCause() instanceof RogueBackstabEvent) {
            RogueBackstabEvent rogueBackstabEvent = (RogueBackstabEvent) player.getLastDamageCause();
            message = formatRogueEvent(player, rogueBackstabEvent);
            killer = rogueBackstabEvent.getBackstabbedBy();
            handleStats = false;

        } else if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            message = formatDamageEvent(player, (EntityDamageByEntityEvent) player.getLastDamageCause(), cause);
            killer = getKiller(player);
            handleStats = true;

        } else {
            message = formatEvent(player, cause);
            killer = getKiller(player);
            handleStats = true;
        }

        getInstance().getTeamManager().handleDeath(player, killer, message);

        if (handleStats) handleStats(killer, player);
        if (!message.isEmpty()) e.setDeathMessage(null);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            User user = getInstance().getUserManager().getByUUID(onlinePlayer.getUniqueId());

            if (!user.isDeathMessages()) {
                continue;
            }

            onlinePlayer.sendMessage(message);
        }
    }

    private String formatRogueEvent(Player player, RogueBackstabEvent e) {
        Player killer = e.getBackstabbedBy();
        return Config.DEATH_BACKSTABBED
                .replace("%player%", format(player))
                .replace("%killer%", formatKiller(killer));
    }

    private String formatEvent(Player player, EntityDamageEvent.DamageCause cause) {
        Player killer = getKiller(player);

        switch (cause) {
            case BLOCK_EXPLOSION:
                return Config.DEATH_EXPLOSION
                        .replace("%player%", format(player));

            case MAGIC:
                return Config.DEATH_MAGIC;

            case WITHER:
                return Config.DEATH_WITHER
                        .replace("%player%", format(player));

            case STARVATION:
                return Config.DEATH_STARVATION
                        .replace("%player%", format(player));

            case DROWNING:
                return Config.DEATH_DROWN
                        .replace("%player%", format(player));

            case SUFFOCATION:
                return Config.DEATH_SUFFOCATION
                        .replace("%player%", format(player));

            case POISON:
                return Config.DEATH_POISON
                        .replace("%player%", format(player));

            case LAVA:
                return Config.DEATH_LAVA
                        .replace("%player%", format(player));

            case FIRE_TICK:
            case FIRE:
                return Config.DEATH_FIRE
                        .replace("%player%", format(player));

            case THORNS:
            case CONTACT:
                return Config.DEATH_CONTACT
                        .replace("%player%", format(player));

            case FALL:
                if (killer != null && killer != player) {
                    return Config.DEATH_FALL_KILLER
                            .replace("%player%", format(player))
                            .replace("%killer%", formatKiller(killer));

                } else {
                    return Config.DEATH_FALL
                            .replace("%player%", format(player));
                }

            case VOID:
                if (killer != null && killer != player) {
                    return Config.DEATH_VOID_KILLER
                            .replace("%player%", format(player))
                            .replace("%killer%", formatKiller(killer));

                } else {
                    return Config.DEATH_VOID
                            .replace("%player%", format(player));
                }

            default:
                return Config.DEATH_DEFAULT
                        .replace("%player%", format(player));
        }
    }

    private String formatDamageEvent(Player player, EntityDamageByEntityEvent e, EntityDamageEvent.DamageCause cause) {
        Player killer = getKiller(player);

        switch (cause) {
            case FALLING_BLOCK:
                return Config.DEATH_SUFFOCATION
                        .replace("%player%", format(player));

            case ENTITY_EXPLOSION:
                return Config.DEATH_EXPLOSION
                        .replace("%player%", format(player));

            case LIGHTNING:
                return Config.DEATH_LIGHTNING
                        .replace("%player%", format(player));

            case ENTITY_ATTACK:
                if (e.getDamager() instanceof Player) {
                    Player damager = (Player) e.getDamager();
                    Killtag killtag = getInstance().getKilltagManager().getKilltag(damager);

                    if (killtag != null) {
                        return killtag.getFormat()
                                .replace("%player%", format(player))
                                .replace("%killer%", formatKiller(damager))
                                .replace("%item%", ItemUtils.getItemName(getManager().getItemInHand(damager)));
                    }

                    return Config.DEATH_KILLER
                            .replace("%player%", format(player))
                            .replace("%killer%", formatKiller(damager))
                            .replace("%item%", ItemUtils.getItemName(getManager().getItemInHand(damager)));

                } else {
                    return Config.DEATH_ENTITY
                            .replace("%player%", format(player))
                            .replace("%entity%", ItemUtils.getEntityName(e.getDamager()));
                }

            case FALL:
                if (killer != null && killer != player) {
                    return Config.DEATH_FALL_KILLER
                            .replace("%player%", format(player))
                            .replace("%killer%", formatKiller(killer));

                } else {
                    return Config.DEATH_FALL
                            .replace("%player%", format(player));
                }

            case PROJECTILE:
                if (killer != null && killer != player) {
                    int distance = (int) killer.getLocation().distance(player.getLocation());
                    return Config.DEATH_PROJECTILE_KILLER
                            .replace("%player%", format(player))
                            .replace("%killer%", formatKiller(killer))
                            .replace("%blocks%", String.valueOf(distance));

                } else {
                    return Config.DEATH_PROJECTILE
                            .replace("%player%", format(player));
                }

            default:
                return Config.DEATH_DEFAULT
                        .replace("%player%", format(player));
        }
    }

    private Player getKiller(Player player) {
        if (player.getKiller() != null) {
            return player.getKiller();
        }

        return ((FocusModeAbility) getInstance().getAbilityManager().getAbility("FocusMode"))
                .getDamager(player, 15);
    }

    private String format(Player player) {
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        return Config.DEATH_FORMAT
                .replace("%player%", player.getName())
                .replace("%kills%", String.valueOf(user.getKills()));
    }

    private String formatKiller(Player player) {
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        return Config.DEATH_FORMAT
                .replace("%player%", player.getName())
                .replace("%kills%", String.valueOf(user.getKills() + 1));
    }

    private void handleStats(Player killer, Player player) {
        if (killer == null) return;
        if (!Config.DEATH_STAT_ENABLED) return;

        ItemStack hand = getManager().getItemInHand(killer);

        if (hand != null) {
            // Only swords
            if (getConfig().getBoolean("STATS_TRACKER.KILLS_ONLY_SWORD") && !hand.getType().name().endsWith("_SWORD")) {
                return;
            }

            if (hand.getType() == Material.AIR) return;
            handleKiller(killer, player, hand);
        }

        // All the armor
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                handleDeath(killer, player, armor);
            }
        }
    }

    private void handleKiller(Player player, Player target, ItemStack item) {
        ItemBuilder builder = new ItemBuilder(item);
        Date date = new Date();
        List<String> currentLore = new ArrayList<>(builder.getLore());

        boolean hasStats = false;
        int header = -1;

        // Check for header and save index
        for (int i = 0; i < currentLore.size(); i++) {
            String s = currentLore.get(i);

            if (s.startsWith(Config.KILL_HEADER)) {
                header = i;
                hasStats = true;
                break;
            }
        }

        // No header, add a new one
        if (!hasStats) {
            if (getConfig().getBoolean("STATS_TRACKER.SPACE_BEFORE_KILLS")) {
                currentLore.add("");
            }

            currentLore.add(Config.KILL_HEADER + 1);
            currentLore.add(Config.KILL_STAT
                    .replace("%player%", player.getName())
                    .replace("%target%", target.getName())
                    .replace("%playerPrefix%", getInstance().getRankHook().getRankPrefix(player))
                    .replace("%targetPrefix%", getInstance().getRankHook().getRankPrefix(target))
                    .replace("%playerColor%", getInstance().getRankHook().getRankColor(player))
                    .replace("%targetColor%", getInstance().getRankHook().getRankColor(target))
                    .replace("%date%", STATTRACK_FORMAT.format(date))
                    .replace("%time%", STATTRACK_FORMAT_TIME.format(date))
            );

            builder.setLore(currentLore);
            return;
        }

        // Replace the header, and then replace all the spaces will return just the number.
        int currKills = Integer.parseInt(ChatColor.stripColor(currentLore.get(header).replace(Config.KILL_HEADER, "").replaceAll(" ", "")));

        currentLore.set(header, Config.KILL_HEADER + (currKills + 1));
        currentLore.add(Config.KILL_STAT
                .replace("%player%", player.getName())
                .replace("%target%", target.getName())
                .replace("%playerPrefix%", getInstance().getRankHook().getRankPrefix(player))
                .replace("%targetPrefix%", getInstance().getRankHook().getRankPrefix(target))
                .replace("%playerColor%", getInstance().getRankHook().getRankColor(player))
                .replace("%targetColor%", getInstance().getRankHook().getRankColor(target))
                .replace("%date%", STATTRACK_FORMAT.format(date))
                .replace("%time%", STATTRACK_FORMAT_TIME.format(date))
        );

        // Remove line below header
        if (Config.MAX_KILL_STAT != -1 && currentLore.size() - header > Config.MAX_KILL_STAT) {
            currentLore.remove(header + 1);
        }

        builder.setLore(currentLore);
    }

    private void handleDeath(Player player, Player target, ItemStack item) {
        ItemBuilder builder = new ItemBuilder(item);
        Date date = new Date();
        List<String> currentLore = new ArrayList<>(builder.getLore());

        boolean hasStats = false;
        int header = -1;

        // Check for header and save index
        for (int i = 0; i < currentLore.size(); i++) {
            String s = currentLore.get(i);

            if (s.startsWith(Config.DEATH_HEADER)) {
                header = i;
                hasStats = true;
                break;
            }
        }

        // No header, add a new one
        if (!hasStats) {
            if (getConfig().getBoolean("STATS_TRACKER.SPACE_BEFORE_DEATHS")) {
                currentLore.add("");
            }

            currentLore.add(Config.DEATH_HEADER);
            currentLore.add(Config.DEATH_STAT
                    .replace("%player%", player.getName())
                    .replace("%target%", target.getName())
                    .replace("%playerPrefix%", getInstance().getRankHook().getRankPrefix(player))
                    .replace("%targetPrefix%", getInstance().getRankHook().getRankPrefix(target))
                    .replace("%playerColor%", getInstance().getRankHook().getRankColor(player))
                    .replace("%targetColor%", getInstance().getRankHook().getRankColor(target))
                    .replace("%date%", STATTRACK_FORMAT.format(date))
                    .replace("%time%", STATTRACK_FORMAT_TIME.format(date))
            );

            item.hasItemMeta(); // errors on no item meta items if this isn't here
            builder.setLore(currentLore);
            return;
        }

        currentLore.add(Config.DEATH_STAT
                .replace("%player%", player.getName())
                .replace("%target%", target.getName())
                .replace("%playerPrefix%", getInstance().getRankHook().getRankPrefix(player))
                .replace("%targetPrefix%", getInstance().getRankHook().getRankPrefix(target))
                .replace("%playerColor%", getInstance().getRankHook().getRankColor(player))
                .replace("%targetColor%", getInstance().getRankHook().getRankColor(target))
                .replace("%date%", STATTRACK_FORMAT.format(date))
                .replace("%time%", STATTRACK_FORMAT_TIME.format(date))
        );

        // Remove line below header
        if (Config.MAX_DEATH_STAT != -1 && currentLore.size() - header > Config.MAX_DEATH_STAT) {
            currentLore.remove(header + 1);
        }

        builder.setLore(currentLore);
    }
}