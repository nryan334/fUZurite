package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SwitchStickAbility extends Ability {

    private final Set<UUID> switchSticks;
    private final int chance;
    private final int degrees;
    private final int time;

    public SwitchStickAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Switch Stick"
        );
        this.switchSticks = new HashSet<>();
        this.chance = getAbilitiesConfig().getInt("SWITCH_STICK.CHANCE");
        this.degrees = getAbilitiesConfig().getInt("SWITCH_STICK.DEGREES");
        this.time = getAbilitiesConfig().getInt("SWITCH_STICK.TIME");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);
        switchSticks.add(player.getUniqueId());
        Tasks.executeLater(getManager(), 20L * time, () -> switchSticks.remove(player.getUniqueId()));

        for (String s : getLanguageConfig().getStringList("ABILITIES.SWITCH_STICK.USED")) {
            player.sendMessage(s);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();

        if (!switchSticks.contains(damager.getUniqueId())) return;

        int random = ThreadLocalRandom.current().nextInt(100 + 1);

        if (random <= chance) {
            // https://www.spigotmc.org/threads/setting-a-players-yaw-pitch.97825/
            Location cloned = damaged.getLocation().clone();
            cloned.setYaw(cloned.getYaw() + degrees);
            damaged.teleport(cloned);

            for (String s : getLanguageConfig().getStringList("ABILITIES.SWITCH_STICK.ACTIVATED")) {
                damager.sendMessage(s);
            }
        }
    }
}