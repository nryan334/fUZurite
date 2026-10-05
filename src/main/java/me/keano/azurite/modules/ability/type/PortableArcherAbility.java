package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.pvpclass.type.archer.ArcherClass;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PortableArcherAbility extends Ability {

    private final Map<UUID, UUID> usedPortableArcher; // player -> arrow

    public PortableArcherAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Portable Archer"
        );
        this.usedPortableArcher = new HashMap<>();
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        ItemStack bow = e.getBow();

        if (hasAbilityInHand(player)) {
            usedPortableArcher.put(player.getUniqueId(), e.getProjectile().getUniqueId());
            getManager().setData(bow, getManager().getData(bow) + 1);

            if (getManager().getData(bow) == bow.getType().getMaxDurability()) {
                getManager().takeItemInHand(player, 1);
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();

        if (!hasAbilityInHand(player)) return;

        if (cannotUse(player) || hasCooldown(player)) {
            e.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        ItemStack damage = e.getItem();

        if (!damage.hasItemMeta()) return;
        if (!damage.getItemMeta().hasDisplayName()) return;

        ItemMeta itemMeta = damage.getItemMeta();
        ItemMeta abilityMeta = item.getItemMeta();

        if (itemMeta.getDisplayName().equals(abilityMeta.getDisplayName()) && itemMeta.getLore().equals(abilityMeta.getLore())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Arrow)) return;

        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;

        UUID arrow = usedPortableArcher.remove(damager.getUniqueId());

        if (arrow != null && e.getDamager().getUniqueId().equals(arrow)) {
            ArcherClass archerClass = getInstance().getClassManager().getArcherClass();
            if (archerClass != null) archerClass.archerTag(e);
            applyCooldown(damager);
        }
    }
}