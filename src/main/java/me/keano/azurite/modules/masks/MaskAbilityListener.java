package me.keano.azurite.modules.masks;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.CC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MaskAbilityListener extends Module<MaskManager> {

    private final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    // Spiderman
    private static final long SPIDERMAN_DOUBLE_JUMP_COOLDOWN = 8 * 60 * 1000L;
    // Flash
    private static final long FLASH_DASH_COOLDOWN = 5 * 60 * 1000L;
    // Thor
    private static final long THOR_SPEED_COOLDOWN = 5 * 60 * 1000L;

    // Spiderman jump state
    private final Map<UUID, Boolean> spiderJumpReady = new HashMap<>();
    private final Map<UUID, Long> spiderJumpCooldown = new HashMap<>();

    public MaskAbilityListener(MaskManager manager) {
        super(manager);
    }

    private boolean hasMask(Player player, String maskId) {
        if (player == null) return false;
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType() == Material.AIR) return false;
        String id = getManager().getMaskIdFromItem(helmet);
        return id != null && id.equalsIgnoreCase(maskId);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player damaged = (Player) e.getEntity();

        // Venom: 10% chance to survive a lethal hit
        if (hasMask(damaged, "venom")) {
            if (damaged.getHealth() - e.getFinalDamage() <= 0) {
                if (random.nextInt(100) < 10) {
                    e.setCancelled(true);
                    damaged.setHealth(damaged.getMaxHealth());
                    damaged.sendMessage(CC.t("&a&lVENOM MASK &7- You survived a lethal hit!"));
                    damaged.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                }
            }
        }

        if (!(e.getDamager() instanceof Player)) return;
        Player damager = (Player) e.getDamager();

        // Spiderman: 6% chance to deal +10% extra damage
        if (hasMask(damager, "spiderman")) {
            if (random.nextInt(100) < 6) {
                double extraDamage = e.getDamage() * 0.10;
                e.setDamage(e.getDamage() + extraDamage);
                damager.sendMessage(CC.t("&c&lSPIDERMAN MASK &7- You dealt a critical hit!"));
            }
        }

        // Venom: 3% chance to receive Strength I for 5 seconds
        if (hasMask(damager, "venom")) {
            if (random.nextInt(100) < 3) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0));
                damager.sendMessage(CC.t("&c&lVENOM MASK &7- You received Strength I for 5 seconds!"));
            }
        }

        // Hulk: 3% chance to strike lightning and Strength II for 5 seconds
        if (hasMask(damager, "hulk")) {
            if (random.nextInt(100) < 3) {
                Location loc = damaged.getLocation();
                loc.getWorld().strikeLightning(loc);
                damager.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1));
                damager.sendMessage(CC.t("&c&lHULK MASK &7- You struck lightning and gained Strength II!"));
            }
        }

        // Thor: 5% chance to archer tag the enemy
        if (hasMask(damager, "thor")) {
            if (random.nextInt(100) < 5) {
                if (getInstance().getTimerManager().getArcherTagTimer() != null) {
                    getInstance().getTimerManager().getArcherTagTimer().applyTimer(damaged);
                    damager.sendMessage(CC.t("&c&lTHOR MASK &7- You archer tagged your enemy!"));
                }
            }
        }

        // Ironman: 5% chance to receive Speed III on a hit (1.7 style)
        if (hasMask(damager, "ironman")) {
            if (random.nextInt(100) < 5) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2));
                damager.sendMessage(CC.t("&c&lIRONMAN MASK &7- You received Speed III!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        // Spiderman: Double jump (shift + right-click in air)
        if (hasMask(player, "spiderman")) {
            if (e.getAction().name().contains("RIGHT") && player.isSneaking() && !player.isOnGround()) {
                if (!isOnCooldown(player, "spiderman_doublejump", SPIDERMAN_DOUBLE_JUMP_COOLDOWN)) {
                    player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1.0));
                    player.sendMessage(CC.t("&c&lSPIDERMAN MASK &7- Double jump activated!"));
                    setCooldown(player, "spiderman_doublejump");
                }
            }
        }

        // Flash: Dash (right-click with empty hand while sneaking)
        if (hasMask(player, "flash")) {
            if (e.getAction().name().contains("RIGHT") && player.isSneaking()) {
                if (!isOnCooldown(player, "flash_dash", FLASH_DASH_COOLDOWN)) {
                    player.setVelocity(player.getLocation().getDirection().multiply(3.0).setY(0.5));
                    player.sendMessage(CC.t("&c&lFLASH MASK &7- Dash activated!"));
                    setCooldown(player, "flash_dash");
                }
            }
        }

        // Thor: Speed boost (right-click while sneaking)
        if (hasMask(player, "thor")) {
            if (e.getAction().name().contains("RIGHT") && player.isSneaking()) {
                if (!isOnCooldown(player, "thor_speed", THOR_SPEED_COOLDOWN)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                    player.sendMessage(CC.t("&c&lTHOR MASK &7- Speed boost activated!"));
                    setCooldown(player, "thor_speed");
                }
            }
        }
    }

    // Flash: 8% chance to dodge Ninja Star (EnderPearl projectile hit)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPearlHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player damaged = (Player) e.getEntity();

        if (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            if (hasMask(damaged, "flash")) {
                // Check if the projectile is from a Ninja ability (Snowball/EnderPearl)
                Entity damager = e.getDamager();
                if (damager != null) {
                    if (random.nextInt(100) < 8) {
                        e.setCancelled(true);
                        damaged.sendMessage(CC.t("&c&lFLASH MASK &7- You dodged a projectile!"));
                    }
                }
            }
        }
    }

    // Ironman: Switcher range +5 blocks
    public int getSwitcherRangeBonus(Player player) {
        if (hasMask(player, "ironman")) {
            return 5;
        }
        return 0;
    }

    // Ironman: 10% chance to bypass partner item cooldown
    public boolean bypassAbilityCooldown(Player player) {
        if (hasMask(player, "ironman")) {
            return random.nextInt(100) < 10;
        }
        return false;
    }

    // Venom: 15% ender pearl cooldown reduction
    public double getEnderPearlCooldownMultiplier(Player player) {
        if (hasMask(player, "venom")) {
            return 0.85;
        }
        return 1.0;
    }

    private boolean isOnCooldown(Player player, String ability, long cooldownMillis) {
        long lastUse = getCooldownTime(player, ability);
        return System.currentTimeMillis() - lastUse < cooldownMillis;
    }

    private long getCooldownTime(Player player, String ability) {
        long time = 0L;
        Long stored = cooldowns.get(player.getUniqueId());
        if (stored != null) {
            time = stored;
        }
        return time;
    }

    private void setCooldown(Player player, String ability) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
