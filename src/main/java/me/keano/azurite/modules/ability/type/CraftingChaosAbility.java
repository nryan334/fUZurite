package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CraftingChaosAbility extends Ability {

    private final Map<UUID, Integer> hits;
    private final Map<UUID, UUID> craftingChaos;
    private final int seconds;
    private final int maxHits;
    private final int chance;

    public CraftingChaosAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.HIT_PLAYER,
                "Crafting Chaos"
        );
        this.hits = new HashMap<>();
        this.craftingChaos = new HashMap<>();
        this.seconds = getAbilitiesConfig().getInt("CRAFTING_CHAOS.SECONDS");
        this.maxHits = getAbilitiesConfig().getInt("CRAFTING_CHAOS.HITS_REQUIRED");
        this.chance = getAbilitiesConfig().getInt("CRAFTING_CHAOS.CHANCE");
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        UUID damagerUUID = damager.getUniqueId();

        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;
        if (!hits.containsKey(damagerUUID)) hits.put(damagerUUID, 0);

        int current = hits.get(damagerUUID) + 1;
        hits.put(damagerUUID, current);

        if (current == maxHits) {
            hits.remove(damagerUUID);
            craftingChaos.put(damagerUUID, damaged.getUniqueId());

            takeItem(damager);
            applyCooldown(damager);

            Tasks.executeLater(getManager(), 20L * seconds, () -> {
                craftingChaos.remove(damagerUUID);

                for (String s : getLanguageConfig().getStringList("ABILITIES.CRAFTING_CHAOS.EXPIRED")) {
                    damager.sendMessage(s);
                }
            });

            for (String s : getLanguageConfig().getStringList("ABILITIES.CRAFTING_CHAOS.USED"))
                damager.sendMessage(s
                        .replace("%player%", damaged.getName())
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.CRAFTING_CHAOS.BEEN_HIT"))
                damaged.sendMessage(s
                        .replace("%player%", damager.getName())
                );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();

        if (!craftingChaos.containsKey(damager.getUniqueId())) return;
        if (!craftingChaos.get(damager.getUniqueId()).equals(damaged.getUniqueId())) return;

        if (ThreadLocalRandom.current().nextInt(100 + 1) <= chance) {
            damaged.openWorkbench(null, true);

            for (String s : getLanguageConfig().getStringList("ABILITIES.CRAFTING_CHAOS.MADE_OPEN"))
                damager.sendMessage(s
                        .replace("%player%", damaged.getName())
                );
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        hits.remove(player.getUniqueId());
        craftingChaos.remove(player.getUniqueId());
    }
}