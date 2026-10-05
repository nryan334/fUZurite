package me.keano.azurite.modules.pvpclass.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ArmorListener extends Module<PvPClassManager> {

    public ArmorListener(PvPClassManager manager) {
        super(manager);
    }

    @EventHandler
    public void onEffect(EntityPotionEffectEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (e.getAction() == EntityPotionEffectEvent.Action.REMOVED && e.getCause() == EntityPotionEffectEvent.Cause.EXPIRATION) {
            PotionEffect old = e.getOldEffect();

            if (old == null) return;

            PotionEffect restore = getManager().getRestores().remove(player.getUniqueId(), old.getType());
            PvPClass active = getManager().getActiveClass(player);

            if (active != null) {
                Tasks.execute(getManager(), () -> {
                    if (active.hasArmor(player)) active.addEffects(player);
                });
            }

            if (restore != null) {
                Tasks.execute(getManager(), () -> {
                    // Attempted fix for resistance bug (SagePvP)
                    if (active != null && active.getEffects().stream().anyMatch(effect -> effect.getType().equals(restore.getType()))) {
                        if (!active.hasArmor(player)) return;
                    }

                    player.addPotionEffect(restore);
                });
            }
        }
    }

    @EventHandler
    public void onEquip(PlayerArmorChangeEvent e) {
        // There's a bug where this event will get called when your armor takes durability damage
        // if the item's type are equal don't check. Otherwise, the class will be unequipped/equipped again.
        if (e.getNewItem() != null && e.getOldItem() != null &&
                e.getNewItem().getType() == e.getOldItem().getType()) return;

        getManager().checkArmor(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEquipFix(PlayerArmorChangeEvent e) {
        Player player = e.getPlayer();
        ItemStack old = e.getOldItem();

        if (old == null) return;

        // best way I can think of, no reason to remove armor unless you are trying to bug it out.
        getManager().getRestores().removeFirst(player.getUniqueId());
    }
}