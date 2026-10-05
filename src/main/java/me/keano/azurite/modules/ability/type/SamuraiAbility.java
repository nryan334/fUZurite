package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.ability.task.TeleportTask;
import me.keano.azurite.modules.timers.listeners.playertimers.EnderpearlTimer;
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
public class SamuraiAbility extends Ability {

    private final Set<PotionEffect> effects;
    private final int hitsValid;
    private final int seconds;
    private final int antiBuildTime;
    private final int enderPearlTime;

    public SamuraiAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Samurai"
        );
        this.effects = getAbilitiesConfig().getStringList("SAMURAI.EFFECTS_ON_TELEPORT").stream().map(Serializer::getEffect).collect(Collectors.toSet());
        this.hitsValid = getAbilitiesConfig().getInt("SAMURAI.HITS_VALID");
        this.seconds = getAbilitiesConfig().getInt("SAMURAI.SECONDS");
        this.antiBuildTime = getAbilitiesConfig().getInt("SAMURAI.ANTI_BUILD_TIME");
        this.enderPearlTime = getAbilitiesConfig().getInt("SAMURAI.ENDER_PEARL_TIME");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        // I already coded a way to get last damager for focus mode, so just use that.
        Player damager = ((FocusModeAbility) getManager().getAbility("FocusMode")).getDamager(player, hitsValid);

        if (damager == null) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.SAMURAI_ABILITY.NO_LAST_HIT"));
            return;
        }

        AntiBuildAbility antiBuildAbility = (AntiBuildAbility) getManager().getAbility("AntiBuild");
        EnderpearlTimer enderpearlTimer = getInstance().getTimerManager().getEnderpearlTimer();

        takeItem(player);
        applyCooldown(player);

        antiBuildAbility.getAntiBuild().applyCooldown(damager, antiBuildTime);
        enderpearlTimer.applyTimer(damager, enderPearlTime * 1000L);

        new TeleportTask(this, () -> {
            player.teleport(damager);

            for (PotionEffect effect : effects) {
                getInstance().getClassManager().addEffect(player, effect);
            }

            for (String s : getLanguageConfig().getStringList("ABILITIES.SAMURAI_ABILITY.TELEPORTED_SUCCESSFULLY")) {
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                );
            }
        }, (i) -> {
            for (String s : getLanguageConfig().getStringList("ABILITIES.SAMURAI_ABILITY.TELEPORTING"))
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.SAMURAI_ABILITY.TELEPORTING_ATTACKER"))
                damager.sendMessage(s
                        .replace("%player%", player.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );
        }, seconds);
    }
}