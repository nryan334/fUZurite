package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RageBallAbility extends Ability {

    private final Map<UUID, UUID> ballsThrown;
    private final List<PotionEffect> friendlyEffects;
    private final List<PotionEffect> enemyEffects;
    private final String rageBallEffect;
    private final boolean wholeTeamCooldown;
    private final int radius;

    public RageBallAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Rage Ball"
        );
        this.ballsThrown = new HashMap<>();
        this.friendlyEffects = new ArrayList<>();
        this.enemyEffects = new ArrayList<>();
        this.rageBallEffect = getAbilitiesConfig().getString("RAGE_BALL.EFFECT");
        this.wholeTeamCooldown = getAbilitiesConfig().getBoolean("RAGE_BALL.COOLDOWN_WHOLE_TEAM");
        this.radius = getAbilitiesConfig().getInt("RAGE_BALL.RADIUS");
        this.load();
    }

    private void load() {
        for (String s : getAbilitiesConfig().getStringList("RAGE_BALL.FRIENDLY_EFFECTS")) {
            friendlyEffects.add(Serializer.getEffect(s));
        }

        for (String s : getAbilitiesConfig().getStringList("RAGE_BALL.ENEMY_EFFECTS")) {
            enemyEffects.add(Serializer.getEffect(s));
        }
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

        for (String s : getLanguageConfig().getStringList("ABILITIES.RAGE_BALL.USED")) {
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

            PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
            PvPClassManager classManager = getInstance().getClassManager();

            // The effect
            getInstance().getVersionManager().getVersion().playEffect(projectile.getLocation(), rageBallEffect, null);

            for (Entity entity : projectile.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof Player)) continue;

                Player other = (Player) entity;

                if (other == player) {
                    for (PotionEffect friendlyEffect : friendlyEffects) classManager.addEffect(player, friendlyEffect);
                    continue;
                }

                if (getInstance().getTeamManager().canHit(player, other, false)) {
                    for (PotionEffect enemyEffect : enemyEffects) classManager.addEffect(other, enemyEffect);
                }

                if (pt != null && pt.getPlayers().contains(other.getUniqueId())) {
                    if (wholeTeamCooldown) abilityCooldown.applyTimer(other);
                    for (PotionEffect friendlyEffect : friendlyEffects) classManager.addEffect(other, friendlyEffect);
                }
            }
        }
    }
}