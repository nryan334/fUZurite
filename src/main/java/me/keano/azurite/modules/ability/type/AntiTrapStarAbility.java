package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.ability.task.TeleportTask;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AntiTrapStarAbility extends Ability {

    private final Map<UUID, Pair<UUID, Long>> lastDamage;
    private final int hitsValid;
    private final int seconds;

    public AntiTrapStarAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Anti Trap Star"
        );
        this.lastDamage = new HashMap<>();
        this.hitsValid = getAbilitiesConfig().getInt("ANTI_TRAP_STAR.HITS_VALID");
        this.seconds = getAbilitiesConfig().getInt("ANTI_TRAP_STAR.SECONDS");
        getManager().getTasks().add(Bukkit.getScheduler().runTaskTimer(getInstance(), this::cleanDamageStore, 0, (20 * 60) * 5L));
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        Player damager = getDamager(player, hitsValid);

        if (damager == null) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_STAR_ABILITY.NO_LAST_HIT"));
            return;
        }

        takeItem(player);
        applyCooldown(player);

        new TeleportTask(this, () -> {
            player.teleport(damager);
            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_STAR_ABILITY.TELEPORTED_SUCCESSFULLY")) {
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                );
            }
        }, (i) -> {
            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_STAR_ABILITY.TELEPORTING"))
                player.sendMessage(s
                        .replace("%player%", damager.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_STAR_ABILITY.TELEPORTING_ATTACKER"))
                damager.sendMessage(s
                        .replace("%player%", player.getName())
                        .replace("%seconds%", String.valueOf(seconds - i))
                );
        }, seconds);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageStore(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();
        Player damager = Utils.getDamagerProjectileOnly(e.getDamager()); // only projectiles for focus mode

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