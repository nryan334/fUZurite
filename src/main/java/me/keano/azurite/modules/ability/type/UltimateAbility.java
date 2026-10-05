package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class UltimateAbility extends Ability {

    private final List<PotionEffect> effects;
    private final int seconds;

    public UltimateAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Ultimate Ability"
        );
        this.effects = getAbilitiesConfig().getStringList("ULTIMATE_ABILITY.EFFECTS").stream().map(Serializer::getEffect).collect(Collectors.toList());
        this.seconds = getAbilitiesConfig().getInt("ULTIMATE_ABILITY.SECONDS");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        Location location = player.getLocation();
        Tasks.executeLater(getManager(), 20L * seconds, () -> player.teleport(location));

        for (String s : getLanguageConfig().getStringList("ABILITIES.ULTIMATE_ABILITY.USED")) {
            player.sendMessage(s);
        }

        for (PotionEffect effect : effects) {
            getInstance().getClassManager().addEffect(player, effect);
        }
    }
}