package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.task.TeleportTask;
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
public class TeleportBowAbility extends Ability {

    private final Map<UUID, UUID> usedTeleportBow; // player -> arrow
    private final int seconds;

    public TeleportBowAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Teleport Bow"
        );
        this.usedTeleportBow = new HashMap<>();
        this.seconds = getAbilitiesConfig().getInt("TELEPORT_BOW.SECONDS");
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        ItemStack bow = e.getBow();

        if (hasAbilityInHand(player)) {
            usedTeleportBow.put(player.getUniqueId(), e.getProjectile().getUniqueId());
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

        Player damaged = (Player) e.getEntity();
        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;
        if (damager == damaged) return;

        UUID arrow = usedTeleportBow.remove(damager.getUniqueId());

        if (arrow != null && e.getDamager().getUniqueId().equals(arrow)) {
            applyCooldown(damager);

            new TeleportTask(this, () -> {
                damager.teleport(damaged);

                for (String s : getLanguageConfig().getStringList("ABILITIES.TELEPORT_BOW.TELEPORTED_SUCCESSFULLY")) {
                    damager.sendMessage(s
                            .replace("%player%", damaged.getName())
                    );
                }
            }, (i) -> {
                for (String s : getLanguageConfig().getStringList("ABILITIES.TELEPORT_BOW.TELEPORTING"))
                    damager.sendMessage(s
                            .replace("%player%", damaged.getName())
                            .replace("%seconds%", String.valueOf(seconds - i))
                    );

                for (String s : getLanguageConfig().getStringList("ABILITIES.TELEPORT_BOW.TELEPORTING_ATTACKER"))
                    damaged.sendMessage(s
                            .replace("%player%", damager.getName())
                            .replace("%seconds%", String.valueOf(seconds - i))
                    );
            }, seconds);
        }
    }
}