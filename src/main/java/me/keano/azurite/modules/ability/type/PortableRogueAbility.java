package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.pvpclass.type.rogue.RogueBackstabEvent;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PortableRogueAbility extends Ability {

    private final List<PotionEffect> backstabEffects;

    public PortableRogueAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Portable Rogue"
        );
        this.backstabEffects = new ArrayList<>();
        this.load();
    }

    private void load() {
        this.backstabEffects.addAll(getAbilitiesConfig().getStringList("PORTABLE_ROGUE.BACKSTAB_EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();

        if (!hasAbilityInHand(damager)) return;
        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;

        // Used to calculate the direction to make sure they are behind the player.
        Vector damagerVector = damager.getLocation().getDirection().setY(0);
        Vector damagedVector = damaged.getLocation().getDirection().setY(0);
        CustomCooldown backstabCooldown = getInstance().getClassManager().getRogueClass().getBackstabCooldown();

        double degree = damagerVector.angle(damagedVector);
        double backstabDamage = getAbilitiesConfig().getDouble("PORTABLE_ROGUE.BACKSTAB_DAMAGE") * 2.0;

        if (Math.abs(degree) < 1.4) {
            if (backstabCooldown.hasCooldown(damager)) {
                damager.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ROGUE_CLASS.BACKSTAB_COOLDOWN")
                        .replace("%seconds%", backstabCooldown.getRemaining(damager))
                );
                return;
            }

            backstabCooldown.applyCooldown(damager, getAbilitiesConfig().getInt("PORTABLE_ROGUE.BACKSTAB_COOLDOWN"));
            getManager().playSound(damaged, getAbilitiesConfig().getString("PORTABLE_ROGUE.BACKSTAB_SOUND"), true);

            takeItem(damager);
            applyCooldown(damager);

            getInstance().getVersionManager().getVersion().playEffect(
                    damaged.getLocation().add(0, 1, 0), // add 1
                    Effect.STEP_SOUND.name(),
                    ItemUtils.getMat(getAbilitiesConfig().getString("PORTABLE_ROGUE.BACKSTAB_EFFECT"))
            );

            damaged.setLastDamageCause(new RogueBackstabEvent(damaged, damager, EntityDamageEvent.DamageCause.CUSTOM, backstabDamage));
            damaged.setHealth(Math.max(damaged.getHealth() - backstabDamage, 0));

            e.setDamage(0D);

            if (damaged.isDead()) {
                e.setCancelled(true); // we cancel the event so PlayerDeathEvent doesn't get called twice.
            }

            // add the slowness and all that
            for (PotionEffect backstabEffect : backstabEffects) {
                damager.addPotionEffect(backstabEffect);
            }

        } else {
            damager.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ROGUE_CLASS.BACKSTAB_FAILED"));
        }
    }
}