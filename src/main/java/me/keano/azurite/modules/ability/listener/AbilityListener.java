package me.keano.azurite.modules.ability.listener;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.ability.task.CooldownBarTask;
import me.keano.azurite.modules.ability.type.AntiTrapBeaconAbility;
import me.keano.azurite.modules.ability.type.PocketBardAbility;
import me.keano.azurite.modules.framework.Module;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AbilityListener extends Module<AbilityManager> {

    public AbilityListener(AbilityManager manager) {
        super(manager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();
        ItemStack hand = getManager().getItemInHand(damager);

        if (hand == null || !hand.hasItemMeta()) return;
        if (!hand.getItemMeta().hasLore() || !hand.getItemMeta().hasDisplayName()) return;

        for (Ability ability : getManager().getAbilities().values()) {
            if (!ability.hasAbilityInHand(damager)) continue;

            if (ability.getUseType() == AbilityUseType.HIT_PLAYER || ability.getUseType() == AbilityUseType.BOTH) {
                ability.onHit(damager, damaged);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null || !hand.hasItemMeta()) return;
        if (!hand.getItemMeta().hasLore() || !hand.getItemMeta().hasDisplayName()) return;

        for (Ability ability : getManager().getAbilities().values()) {
            if (!ability.hasAbilityInHand(player)) continue;
            if (ability instanceof AntiTrapBeaconAbility) continue;
            e.setCancelled(true);
            break;
        }
    }

    @EventHandler
    public void onSwitch(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack hand = player.getInventory().getItem(e.getNewSlot());

        if (hand == null || !hand.hasItemMeta()) return;
        if (!hand.getItemMeta().hasLore() || !hand.getItemMeta().hasDisplayName()) return;

        for (Ability ability : getManager().getAbilities().values()) {
            if (!ability.getAbilityCooldown().hasTimer(player)) continue;

            if (ability.shouldSendCooldown(hand)) {
                new CooldownBarTask(player, ability);
                break;
            }
        }
    }

    @EventHandler
    public void onCooldown(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack hand = player.getItemInHand();

        if (hand == null || !hand.hasItemMeta()) return;
        if (!hand.getItemMeta().hasLore() || !hand.getItemMeta().hasDisplayName()) return;

        if (e.getAction().name().contains("RIGHT")) {
            for (Ability ability : getManager().getAbilities().values()) {
                if (!ability.hasAbilityInHand(player)) continue;

                if (ability.getUseType() == AbilityUseType.INTERACT || ability.getUseType() == AbilityUseType.BOTH) {
                    e.setCancelled(true);
                    ability.onClick(player);
                    break;
                }
            }

        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            // This is for cooldowns
            for (Ability ability : getManager().getAbilities().values()) {
                // Pocket bards have different items;
                if (ability instanceof PocketBardAbility) {
                    PocketBardAbility pocketBard = (PocketBardAbility) ability;

                    if (pocketBard.getPocketBardInHand(player) != null && pocketBard.getAbilityCooldown().hasTimer(player)) {
                        player.sendMessage(getLanguageConfig().getString("ABILITIES.COOLDOWN")
                                .replace("%ability%", ability.getDisplayName())
                                .replace("%time%", ability.getAbilityCooldown().getRemainingString(player))
                        );
                        break;
                    }
                }

                if (ability.hasAbilityInHand(player) && ability.getAbilityCooldown().hasTimer(player)) {
                    player.sendMessage(getLanguageConfig().getString("ABILITIES.COOLDOWN")
                            .replace("%ability%", ability.getDisplayName())
                            .replace("%time%", ability.getAbilityCooldown().getRemainingString(player))
                    );
                    break;
                }
            }
        }
    }
}