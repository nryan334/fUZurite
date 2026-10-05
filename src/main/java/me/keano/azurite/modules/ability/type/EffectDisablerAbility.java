package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionEffectAddEvent;
import org.bukkit.event.inventory.EquipmentSetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class EffectDisablerAbility extends Ability {

    private final Triple<UUID, PotionEffectType, PotionEffect> restores;
    private final Map<UUID, Integer> hits;
    private final Set<UUID> takenAway;

    private final int maxHits;
    private final int takeawayTime;

    public EffectDisablerAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.HIT_PLAYER,
                "Effect Disabler"
        );
        this.restores = new Triple<>();
        this.hits = new HashMap<>();
        this.takenAway = new HashSet<>();

        this.maxHits = getAbilitiesConfig().getInt("EFFECT_DISABLER.HITS_REQUIRED");
        this.takeawayTime = getAbilitiesConfig().getInt("EFFECT_DISABLER.TIME_DISABLED");
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        UUID damagerUUID = damager.getUniqueId();
        PvPClass active = getInstance().getClassManager().getActiveClass(damaged);

        if (active != null) {
            damager.sendMessage(getLanguageConfig().getString("ABILITIES.EFFECT_DISABLER.IN_CLASS"));
            return;
        }

        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;
        if (!hits.containsKey(damagerUUID)) hits.put(damagerUUID, 0);

        int current = hits.get(damagerUUID) + 1;
        hits.put(damagerUUID, current);

        if (current == maxHits) {
            takenAway.add(damaged.getUniqueId());
            hits.remove(damager.getUniqueId());

            takeItem(damager);
            applyCooldown(damager);

            for (PotionEffect effect : damaged.getActivePotionEffects()) {
                restores.put(damaged.getUniqueId(), effect.getType(), effect);
                damaged.removePotionEffect(effect.getType());
            }

            for (String s : getLanguageConfig().getStringList("ABILITIES.EFFECT_DISABLER.USED"))
                damager.sendMessage(s
                        .replace("%player%", damaged.getName())
                        .replace("%seconds%", String.valueOf(takeawayTime))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.EFFECT_DISABLER.BEEN_HIT"))
                damaged.sendMessage(s
                        .replace("%player%", damager.getName())
                        .replace("%seconds%", String.valueOf(takeawayTime))
                );

            Tasks.executeLater(getManager(), 20L * takeawayTime, () -> {
                takenAway.remove(damaged.getUniqueId());

                for (PotionEffectType type : PotionEffectType.values()) {
                    PotionEffect damagedRestore = restores.remove(damaged.getUniqueId(), type);

                    if (damagedRestore != null) {
                        damaged.removePotionEffect(type);
                        damaged.addPotionEffect(damagedRestore, true);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onEffect(PotionEffectAddEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (takenAway.contains(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEquipFix(EquipmentSetEvent e) {
        Player player = (Player) e.getHumanEntity();
        ItemStack previous = e.getPreviousItem();

        if (previous != null) {
            restores.removeFirst(player.getUniqueId());
        }
    }
}