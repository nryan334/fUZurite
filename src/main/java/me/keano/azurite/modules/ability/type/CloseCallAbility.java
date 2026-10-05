package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Serializer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CloseCallAbility extends Ability {

    private final Set<PotionEffect> effects;
    private final String usageMessage;
    private final double healthRequired;

    public CloseCallAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Close Call"
        );
        this.effects = getAbilitiesConfig().getStringList("CLOSE_CALL.EFFECTS").stream().map(Serializer::getEffect).collect(Collectors.toSet());
        this.usageMessage = "%%__USER__%%";
        this.healthRequired = getAbilitiesConfig().getDouble("CLOSE_CALL.HEALTH_REQUIRED");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        if ((player.getHealth() / 2.0) <= healthRequired) {
            for (PotionEffect effect : effects) {
                getInstance().getClassManager().addEffect(player, effect);
            }

            for (String s : getLanguageConfig().getStringList("ABILITIES.CLOSE_CALL.USED_SUCCESSFULLY")) {
                player.sendMessage(s);
            }

        } else {
            for (String s : getLanguageConfig().getStringList("ABILITIES.CLOSE_CALL.USED_UNSUCCESSFULLY")) {
                player.sendMessage(s);
            }
        }
    }
}