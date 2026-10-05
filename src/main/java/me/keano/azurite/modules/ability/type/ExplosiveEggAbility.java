package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.fastparticles.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ExplosiveEggAbility extends Ability {

    private final Map<UUID, UUID> ballsThrown;
    private final ParticleType particleType;
    private final double multiplier;
    private final double yMultiplier;
    private final int radius;

    public ExplosiveEggAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Explosive Egg"
        );
        this.ballsThrown = new HashMap<>();
        this.particleType = ParticleType.of(getAbilitiesConfig().getString("EXPLOSIVE_EGG.EFFECT"));
        this.multiplier = getAbilitiesConfig().getDouble("EXPLOSIVE_EGG.MULTIPLIER");
        this.yMultiplier = getAbilitiesConfig().getDouble("EXPLOSIVE_EGG.Y_MULTIPLIER");
        this.radius = getAbilitiesConfig().getInt("EXPLOSIVE_EGG.RADIUS");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();

        if (!hasAbilityInHand(player)) return;

        if (cannotUse(player) || hasCooldown(player)) {
            e.setCancelled(true);
            player.updateInventory(); // Refund item
        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();
        Player player = Utils.getDamager(projectile);

        if (player == null) return;
        if (!hasAbilityInHand(player)) return;

        ballsThrown.put(projectile.getUniqueId(), player.getUniqueId());
        applyCooldown(player);

        for (String s : getLanguageConfig().getStringList("ABILITIES.EXPLOSIVE_EGG.USED")) {
            player.sendMessage(s);
        }
    }

    @EventHandler
    public void onLand(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        UUID uuid = ballsThrown.remove(projectile.getUniqueId());

        if (uuid != null) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) return;

            particleType.spawn(player.getWorld(), projectile.getLocation(), 1);

            for (Entity entity : projectile.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof Player)) continue;

                Player other = (Player) entity;

                if (getInstance().getTeamManager().canHit(player, other, false)) {
                    pullEntityToLocation(other, player.getLocation());
                }
            }
        }
    }

    private void pullEntityToLocation(Entity entity, Location loc) {
        Vector explosion = loc.toVector().subtract(entity.getLocation().toVector());
        explosion.multiply(multiplier);
        explosion.setY(explosion.getY() + yMultiplier);
        entity.setVelocity(explosion);
    }
}