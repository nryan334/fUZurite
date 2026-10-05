package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RocketAbility extends Ability {

    private final Set<UUID> fall;
    private final double multiplier;
    private final double y;

    public RocketAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Rocket"
        );
        this.fall = new HashSet<>();
        this.multiplier = getAbilitiesConfig().getDouble("ROCKET.MULTIPLIER");
        this.y = getAbilitiesConfig().getDouble("ROCKET.Y_VALUE");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        Vector vector = player.getEyeLocation().getDirection();
        vector.multiply(multiplier);
        if (y != 0) vector.setY(y);
        player.setVelocity(vector);

        fall.add(player.getUniqueId());
        Tasks.executeLater(getManager(), 20L * 15, () -> fall.remove(player.getUniqueId()));

        for (String s : getLanguageConfig().getStringList("ABILITIES.ROCKET.USED")) {
            player.sendMessage(s);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player player = (Player) e.getEntity();

        if (fall.remove(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }
}