package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class StrengthListener extends Module<ListenerManager> {

    public StrengthListener(ListenerManager manager) {
        super(manager);
    }

    // Focus Mode = LOW, Archer Class = NORMAL, Strength = HIGH
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;

        Player player = (Player) e.getDamager();

        if (Config.STRENGTH_NERF && player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            int level = 0;

            for (PotionEffect activePotionEffect : player.getActivePotionEffects()) {
                if (!activePotionEffect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) continue;
                level = activePotionEffect.getAmplifier() + 1;
                break;
            }

            switch (level) {
                case 1:
                    e.setDamage(e.getDamage() * Config.STRENGTH_NERF_LEVEL1);
                    break;

                case 2:
                    e.setDamage(e.getDamage() * Config.STRENGTH_NERF_LEVEL2);
                    break;

                default:
                    e.setDamage(e.getDamage() * Config.STRENGTH_NERF_LEVEL3PLUS);
            }
        }
    }
}