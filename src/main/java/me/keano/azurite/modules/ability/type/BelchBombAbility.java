package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Serializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BelchBombAbility extends Ability {

    private final List<PotionEffect> effects;
    private final int radius;

    public BelchBombAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Belch Bomb"
        );
        this.effects = getAbilitiesConfig().getStringList("BELCH_BOMB.EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList());
        this.radius = getAbilitiesConfig().getInt("BELCH_BOMB.RADIUS");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        int enemies = 0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Player)) continue;
            Player target = (Player) entity;
            if (!getInstance().getTeamManager().canHit(player, target, false)) continue;

            for (PotionEffect effect : effects) {
                getInstance().getClassManager().addEffect(target, effect);
            }
            enemies++;
        }

        for (String s : getLanguageConfig().getStringList("ABILITIES.BELCH_BOMB.USED")) {
            player.sendMessage(s
                    .replace("%enemies%", String.valueOf(enemies))
            );
        }
    }
}