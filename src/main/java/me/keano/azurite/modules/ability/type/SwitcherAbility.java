package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SwitcherAbility extends Ability {

    private final Set<UUID> switchers;
    private final int distance;
    private final boolean switcherTeammate;

    public SwitcherAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Switcher"
        );
        this.switchers = new HashSet<>();
        this.distance = getAbilitiesConfig().getInt("SWITCHER.DISTANCE");
        this.switcherTeammate = getAbilitiesConfig().getBoolean("SWITCHER.ALLOW_TEAMMATE");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();

        if (!hasAbilityInHand(player)) return;

        if (cannotUse(player) || hasCooldown(player)) {
            e.setCancelled(true);
            player.updateInventory();
            return;
        }

        applyCooldown(player);
        switchers.add(player.getUniqueId());
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (getItem().getType() == ItemUtils.getMat("SNOW_BALL") && !(e.getDamager() instanceof Snowball)) return;
        if (getItem().getType() == ItemUtils.getMat("EGG") && !(e.getDamager() instanceof Egg)) return;

        Player damager = Utils.getDamager(e.getDamager());
        Player damaged = (Player) e.getEntity();

        if (damager == null) return;
        if (!switchers.contains(damager.getUniqueId())) return;

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(damager.getUniqueId());
        boolean allow = switcherTeammate && pt != null && pt.getPlayers().contains(damaged.getUniqueId());

        if (e.isCancelled() && !allow) {
            damager.getInventory().addItem(item);
            damager.updateInventory(); // refund it
            return;
        }

        if (damager.getLocation().distance(damaged.getLocation()) > distance) {
            damager.getInventory().addItem(item);
            damager.updateInventory(); // refund it
            damager.sendMessage(getLanguageConfig().getString("ABILITIES.SWITCHER.TOO_FAR"));
            return;
        }

        Location clonedFirst = damaged.getLocation().clone();
        Location clonedSecond = damager.getLocation().clone();

        damaged.teleport(clonedSecond);
        damager.teleport(clonedFirst);

        switchers.remove(damager.getUniqueId());

        for (String s : getLanguageConfig().getStringList("ABILITIES.SWITCHER.USED"))
            damager.sendMessage(s
                    .replace("%player%", damaged.getName())
            );

        for (String s : getLanguageConfig().getStringList("ABILITIES.SWITCHER.BEEN_HIT"))
            damaged.sendMessage(s
                    .replace("%player%", damager.getName())
            );
    }
}