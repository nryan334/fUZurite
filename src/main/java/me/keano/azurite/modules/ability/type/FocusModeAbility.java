package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class FocusModeAbility extends Ability {

    private final Map<UUID, UUID> focusMode;
    private final Map<UUID, Pair<UUID, Long>> lastDamage;
    private final double multiplier;
    private final int seconds;
    private final int hitsValid;

    public FocusModeAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Focus Mode"
        );
        this.focusMode = new HashMap<>();
        this.lastDamage = new HashMap<>();
        this.multiplier = getAbilitiesConfig().getDouble("FOCUS_MODE.DAMAGE_MULTIPLIER");
        this.seconds = getAbilitiesConfig().getInt("FOCUS_MODE.SECONDS");
        this.hitsValid = getAbilitiesConfig().getInt("FOCUS_MODE.HITS_VALID");
        getManager().getTasks().add(Bukkit.getScheduler().runTaskTimer(getInstance(), this::cleanDamageStore, 0L, (20 * 60) * 5L));
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        Player damager = getDamager(player, hitsValid);

        if (damager == null) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.FOCUS_MODE.NO_LAST_HIT"));
            return;
        }

        focusMode.put(player.getUniqueId(), damager.getUniqueId());

        takeItem(player);
        applyCooldown(player);

        Tasks.executeLater(getManager(), 20L * seconds, () -> {
            focusMode.remove(player.getUniqueId());

            for (String s : getLanguageConfig().getStringList("ABILITIES.FOCUS_MODE.EXPIRED")) {
                player.sendMessage(s);
            }
        });

        for (String s : getLanguageConfig().getStringList("ABILITIES.FOCUS_MODE.USED"))
            player.sendMessage(s
                    .replace("%player%", damager.getName())
            );

        for (String s : getLanguageConfig().getStringList("ABILITIES.FOCUS_MODE.BEEN_HIT"))
            damager.sendMessage(s
                    .replace("%player%", player.getName())
            );
    }

    // Focus Mode = LOW, Archer Class = NORMAL, Strength = HIGH
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();

        if (!focusMode.containsKey(damager.getUniqueId())) return;
        if (!focusMode.get(damager.getUniqueId()).equals(damaged.getUniqueId())) return;

        e.setDamage(e.getDamage() * multiplier);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        focusMode.remove(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageStore(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();
        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;
        if (damager == damaged) return;

        lastDamage.put(damaged.getUniqueId(), new Pair<>(damager.getUniqueId(), System.currentTimeMillis()));
    }

    private void cleanDamageStore() {
        Iterator<Map.Entry<UUID, Pair<UUID, Long>>> iterator = lastDamage.entrySet().iterator();

        while (iterator.hasNext()) {
            Pair<UUID, Long> pair = iterator.next().getValue();
            boolean valid = (System.currentTimeMillis() - pair.getValue()) <= (60 * 1000L); // 60s
            if (!valid) iterator.remove();
        }
    }

    public Player getDamager(Player player, int secondsValid) {
        Pair<UUID, Long> pair = lastDamage.get(player.getUniqueId());

        // This will get the last damage depending on the seconds it is valid till.
        if (pair != null) {
            Player damager = Bukkit.getPlayer(pair.getKey());
            boolean valid = (System.currentTimeMillis() - pair.getValue()) <= secondsValid * 1000L;

            // Damager can be null if they logged out.
            if (damager != null && valid) {
                return damager;
            }
        }

        return null;
    }
}