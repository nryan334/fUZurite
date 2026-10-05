package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class DropListener extends Module<ListenerManager> {

    private final boolean durabilityFix;
    private final int durabilityChance;

    public DropListener(ListenerManager manager) {
        super(manager);
        this.durabilityFix = getConfig().getBoolean("DURABILITY_FIX.ENABLED");
        this.durabilityChance = getConfig().getInt("DURABILITY_FIX.PERCENT");
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(PlayerItemDamageEvent e) {
        if (!durabilityFix) return;

        int random = ThreadLocalRandom.current().nextInt(100 + 1);

        if (random <= durabilityChance) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // call last
    public void onDieEXP(EntityDeathEvent e) {
        if (!Config.GIVE_EXP_KILL) return;
        if (e.getEntity() instanceof Player) return;

        Player killer = e.getEntity().getKiller();

        if (killer != null) {
            killer.giveExp(e.getDroppedExp());
            e.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // call last
    public void onDieDrop(EntityDeathEvent e) {
        if (!Config.GIVE_ITEM_ON_KILL) return;
        if (e.getEntity() instanceof Player) return; // Not for players!
        if (e.getEntity() instanceof Villager) return; // Not for loggers!

        Player killer = e.getEntity().getKiller();

        if (killer != null) {
            List<ItemStack> drops = e.getDrops();

            if (drops == null) return;

            for (ItemStack drop : drops) {
                if (drop == null) continue;
                ItemUtils.giveItem(killer, drop, e.getEntity().getLocation());
            }

            drops.clear(); // clear so nothing drops
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreakDrop(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        ItemStack trySmelt = smelt(block);

        // Handle exp
        if (Config.GIVE_EXP_MINE && e.getExpToDrop() > 0) {
            getManager().playSound(player, "ORB_PICKUP", false);
            player.giveExp(e.getExpToDrop());
            e.setExpToDrop(0);
        }

        // Check if we can auto-smelt it first
        if (Config.GIVE_SMELT_ON_MINE && player.hasPermission("azurite.autosmelt") && trySmelt != null) {
            ItemStack hand = getManager().getItemInHand(player);

            if (hand == null) return;
            if (hand.hasItemMeta() && hand.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) return;

            e.setCancelled(true);
            block.setType(Material.AIR); // So nothing drops
            ItemUtils.giveItem(player, trySmelt, block.getLocation());
            return;
        }

        if (Config.GIVE_BLOCKS_ON_MINE) {
            if (player.getGameMode() == GameMode.CREATIVE) return;

            ItemStack hand = getManager().getItemInHand(player);
            List<ItemStack> drop = getInstance().getVersionManager().getVersion().getBlockDrops(player, block, hand);

            block.setType(Material.AIR);
            e.setCancelled(true);
            getInstance().getVersionManager().getVersion().damageItemDefault(player, hand);

            if (!drop.isEmpty()) {
                User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

                if (!user.isCobblePickup()) {
                    drop.removeIf(item -> item.getType() == Material.COBBLESTONE);
                }

                for (ItemStack itemStack : drop) {
                    if (itemStack.getAmount() <= 0) continue;
                    ItemUtils.giveItem(player, itemStack, block.getLocation());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (e.getItem().getItemStack().getType() != Material.COBBLESTONE) return;

        if (!user.isCobblePickup()) {
            e.setCancelled(true);
        }
    }

    private ItemStack smelt(Block block) {
        Material type = block.getType();

        if (type == Material.IRON_ORE) {
            return new ItemStack(Material.IRON_INGOT);

        } else if (type == Material.GOLD_ORE) {
            return new ItemStack(Material.GOLD_INGOT);
        }

        return null;
    }
}