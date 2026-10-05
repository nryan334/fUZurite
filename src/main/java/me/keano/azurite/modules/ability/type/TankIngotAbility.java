package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TankIngotAbility extends Ability {

    private final int radius;
    private final int maxDuration;
    private final int durationPerPlayer;
    private final List<String> effects;

    public TankIngotAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Tank Ingot"
        );
        this.radius = getAbilitiesConfig().getInt("TANK_INGOT.RADIUS");
        this.maxDuration = getAbilitiesConfig().getInt("TANK_INGOT.MAX_DURATION");
        this.durationPerPlayer = getAbilitiesConfig().getInt("TANK_INGOT.DURATION_PER_PLAYER");
        this.effects = getAbilitiesConfig().getStringList("TANK_INGOT.EFFECTS");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        int enemies = countEnemies(player);
        applyEffects(player, enemies);

        for (String s : getLanguageConfig().getStringList("ABILITIES.TANK_INGOT.USED")) {
            player.sendMessage(s
                    .replace("%nearby%", String.valueOf(enemies))
                    .replace("%time%", String.valueOf(durationPerPlayer * enemies))
            );
        }
    }

    private int countEnemies(Player player) {
        int i = 0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (durationPerPlayer * i >= maxDuration) break; // Limit loop
            if (!(entity instanceof Player)) continue;

            Player nearby = (Player) entity;

            if (getInstance().getTeamManager().canHit(player, nearby, false)) {
                i++;
            }
        }

        return i;
    }

    private void applyEffects(Player player, int nearby) {
        if (nearby == 0) return;

        for (String effect : effects) {
            String[] split = effect.split(", ");
            PotionEffect potionEffect = new PotionEffect(
                    PotionEffectType.getByName(split[0]),
                    Math.min(durationPerPlayer * nearby, maxDuration) * 20,
                    Integer.parseInt(split[2]) - 1);
            getInstance().getClassManager().addEffect(player, potionEffect);
        }
    }
}