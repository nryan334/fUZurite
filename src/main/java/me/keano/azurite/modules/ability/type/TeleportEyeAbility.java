package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.ability.task.TeleportTask;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeleportEyeAbility extends Ability {

    private final int hitsValid;
    private final int seconds;

    public TeleportEyeAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Teleport Eye"
        );
        this.hitsValid = getAbilitiesConfig().getInt("TELEPORT_EYE.HITS_VALID");
        this.seconds = getAbilitiesConfig().getInt("TELEPORT_EYE.SECONDS");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        Block block = player.getLocation().getBlock();
        Player damager = ((FocusModeAbility) getManager().getAbility("FocusMode")).getDamager(player, hitsValid);

        if (!block.isLiquid()) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.TELEPORT_EYE.NOT_IN_LIQUID"));
            return;
        }

        if (damager == null) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.NINJA_ABILITY.NO_LAST_HIT"));
            return;
        }

        takeItem(player);
        applyCooldown(player);

        new TeleportTask(this, () -> {
            player.teleport(damager);

            for (String s : getLanguageConfig().getStringList("ABILITIES.TELEPORT_EYE.TELEPORTED_SUCCESSFULLY")) {
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                );
            }
        }, (i) -> {
            for (String s : getLanguageConfig().getStringList("ABILITIES.TELEPORT_EYE.TELEPORTING"))
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.TELEPORT_EYE.TELEPORTING_ATTACKER"))
                damager.sendMessage(s
                        .replace("%player%", player.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );
        }, seconds);
    }
}