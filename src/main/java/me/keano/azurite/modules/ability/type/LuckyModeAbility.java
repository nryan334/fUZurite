package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LuckyModeAbility extends Ability {

    private final Map<UUID, Double> luckyMode;
    private final DecimalFormat formatter;
    private final double minimum;
    private final double maximum;
    private final int seconds;

    public LuckyModeAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Lucky Mode"
        );
        this.luckyMode = new HashMap<>();
        this.formatter = new DecimalFormat("##");
        this.minimum = getAbilitiesConfig().getDouble("LUCKY_MODE.MINIMUM_MULTIPLIER");
        this.maximum = getAbilitiesConfig().getDouble("LUCKY_MODE.MAXIMUM_MULTIPLIER");
        this.seconds = getAbilitiesConfig().getInt("LUCKY_MODE.SECONDS");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        double multiplier = ThreadLocalRandom.current().nextDouble(minimum, maximum + 1);
        luckyMode.put(player.getUniqueId(), multiplier);

        takeItem(player);
        applyCooldown(player);

        Tasks.executeLater(getManager(), 20L * seconds, () -> {
            luckyMode.remove(player.getUniqueId());

            for (String s : getLanguageConfig().getStringList("ABILITIES.LUCKY_MODE.EXPIRED")) {
                player.sendMessage(s);
            }
        });

        for (String s : getLanguageConfig().getStringList("ABILITIES.LUCKY_MODE.USED"))
            player.sendMessage(s
                    .replace("%amount%", formatter.format(multiplier))
            );
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();

        if (!luckyMode.containsKey(damager.getUniqueId())) return;

        double percent = luckyMode.get(damager.getUniqueId());

        // No change
        if (percent == 0) return;

        // This will convert 35% to actual percent (1.35, 1.25, 0.90) etc..
        if (percent > 0) {
            e.setDamage(e.getDamage() * (percent / 100 + 1));

        } else {
            e.setDamage(e.getDamage() * (1 / (Math.abs(percent) / 100 + 1)));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        luckyMode.remove(player.getUniqueId());
    }
}