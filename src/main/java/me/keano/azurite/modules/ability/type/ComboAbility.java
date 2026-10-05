package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ComboAbility extends Ability {

    private final Set<UUID> combo;
    private final Map<UUID, Integer> hits;
    private final int maxHits;
    private final int amountPerHit;
    private final int seconds;

    public ComboAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Combo Ability"
        );
        this.combo = new HashSet<>();
        this.hits = new HashMap<>();
        this.maxHits = getAbilitiesConfig().getInt("COMBO_ABILITY.MAX_HITS");
        this.amountPerHit = getAbilitiesConfig().getInt("COMBO_ABILITY.AMOUNT_PER_HIT");
        this.seconds = getAbilitiesConfig().getInt("COMBO_ABILITY.SECONDS");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        combo.add(player.getUniqueId());

        takeItem(player);
        applyCooldown(player);

        Tasks.executeLater(getManager(), 20L * seconds, () -> handleEffect(player));

        for (String s : getLanguageConfig().getStringList("ABILITIES.COMBO_ABILITY.USED")) {
            player.sendMessage(s);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        UUID damagerUUID = damager.getUniqueId();

        if (combo.contains(damagerUUID)) {
            hits.putIfAbsent(damagerUUID, 0);
            int current = hits.get(damagerUUID);

            if (current < maxHits) {
                hits.put(damagerUUID, current + amountPerHit);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        hits.remove(player.getUniqueId());
        combo.remove(player.getUniqueId());
    }

    private void handleEffect(Player player) {
        Integer amount = hits.remove(player.getUniqueId());
        combo.remove(player.getUniqueId());

        if (amount != null) {
            String[] split = getAbilitiesConfig().getString("COMBO_ABILITY.EFFECT").split(", ");
            PotionEffectType type = PotionEffectType.getByName(split[0]);
            int amplifier = Integer.parseInt(split[1]) - 1;
            getInstance().getClassManager().addEffect(player, new PotionEffect(type, 20 * amount, amplifier));

            for (String s : getLanguageConfig().getStringList("ABILITIES.COMBO_ABILITY.GAINED_EFFECT")) {
                player.sendMessage(s
                        .replace("%amount%", String.valueOf(amount))
                );
            }
        }
    }
}