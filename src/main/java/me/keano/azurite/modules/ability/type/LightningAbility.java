package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LightningAbility extends Ability {

    private final List<UUID> lightnings;
    private final int chance;
    private final int seconds;
    private final double damage;

    public LightningAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Lightning"
        );
        this.lightnings = new ArrayList<>();
        this.chance = getAbilitiesConfig().getInt("LIGHTNING.CHANCE");
        this.seconds = getAbilitiesConfig().getInt("LIGHTNING.SECONDS");
        this.damage = getAbilitiesConfig().getDouble("LIGHTNING.DAMAGE") * 2;
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        lightnings.add(player.getUniqueId());

        takeItem(player);
        applyCooldown(player);

        Tasks.executeLater(getManager(), 20L * seconds, () -> {
            lightnings.remove(player.getUniqueId());

            for (String s : getLanguageConfig().getStringList("ABILITIES.LIGHTNING.EXPIRED")) {
                player.sendMessage(s);
            }
        });

        for (String s : getLanguageConfig().getStringList("ABILITIES.LIGHTNING.USED")) {
            player.sendMessage(s);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();

        if (lightnings.contains(damager.getUniqueId()) && ThreadLocalRandom.current().nextInt(100 + 1) <= chance) {
            damaged.getWorld().strikeLightningEffect(damaged.getLocation());
            damaged.setHealth(Math.max(damaged.getHealth() - damage, 0D));
            e.setDamage(0D); // We only do the lightning damage

            // Fix double death bug
            if (damaged.isDead()) {
                e.setCancelled(true);
            }

            for (String s : getLanguageConfig().getStringList("ABILITIES.LIGHTNING.STRUCK_LIGHTNING"))
                damager.sendMessage(s
                        .replace("%player%", damaged.getName())
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.LIGHTNING.STRUCK_BY_LIGHTNING"))
                damaged.sendMessage(s
                        .replace("%player%", damager.getName())
                );
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        lightnings.remove(player.getUniqueId());
    }
}