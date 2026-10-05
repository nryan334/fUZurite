package me.keano.azurite.modules.loggers;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.loggers.task.LoggerRemoveTask;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Logger extends Module<LoggerManager> {

    private Player player;
    private LoggerRemoveTask removeTask;
    private UUID villagerUUID;
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    private float exp;

    public Logger(LoggerManager manager, Player player) {
        super(manager);

        Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
        this.player = player;
        this.villagerUUID = villager.getUniqueId();
        this.contents = player.getInventory().getContents();
        this.armorContents = player.getInventory().getArmorContents();
        this.exp = player.getExp();
        this.removeTask = new LoggerRemoveTask(getManager(), villagerUUID);

        this.checkVillager(villager);
    }

    private void checkVillager(Villager villager) {
        // Name stuff
        villager.setMetadata("combat-logger", new FixedMetadataValue(getInstance(), true));
        villager.setCustomName(getConfig().getString("LOGGERS.COLOR") + player.getName() + "'s Logger");
        villager.setCustomNameVisible(true);

        // Health stuff
        villager.setMaxHealth(calcHealth(player)); // 20 hearts
        villager.setHealth(villager.getMaxHealth());

        // Some checks to assure age
        villager.setAdult();
        villager.setBreed(false);
        villager.setProfession(Villager.Profession.FARMER);

        // in case the player is falling we apply it to the villager aswell
        villager.setFallDistance(player.getFallDistance());
        villager.setVelocity(player.getVelocity());
        villager.setRemoveWhenFarAway(true);

        // This is in case the villager spawns in lava, etc...
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.FIRE_RESISTANCE)) {
                villager.addPotionEffect(effect);
            }
        }

        // So they can't move
        villager.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW,
                Integer.MAX_VALUE,
                100,
                false
        ));
        villager.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP,
                Integer.MAX_VALUE,
                100,
                false
        ));
    }

    public Villager getVillager() {
        Collection<Villager> villagers = player.getWorld().getEntitiesByClass(Villager.class);
        for (Villager villager : villagers) if (villager.getUniqueId().equals(villagerUUID)) return villager;
        return null;
    }

    public void cancel() {
        if (removeTask != null) {
            removeTask.cancel();
            removeTask = null;
        }
    }

    // Credits: HCTeams
    public double calcHealth(Player player) {
        int potions = 0;
        boolean gapple = false;
        ItemStack pot = ItemUtils.tryGetPotion(getManager(), ItemUtils.getMat("SPLASH_POTION"), 16421);

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;

            if (itemStack.isSimilar(pot)) {
                potions++;

            } else if (!gapple && getManager().isGapple(itemStack)) {
                // Only let the player have one gapple count.
                potions += 15;
                gapple = true;
            }
        }

        return ((potions * 3.5D) + player.getHealth());
    }
}