package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Serializer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MedKitAbility extends Ability {

    private final List<PotionEffect> effects;

    public MedKitAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Med Kit"
        );
        this.effects = getAbilitiesConfig().getStringList("MED_KIT.EFFECTS").stream().map(Serializer::getEffect).collect(Collectors.toList());
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        for (PotionEffect effect : effects) {
            getInstance().getClassManager().addEffect(player, effect);
        }

        for (String s : getLanguageConfig().getStringList("ABILITIES.MED_KIT.USED")) {
            player.sendMessage(s);
        }
    }
}