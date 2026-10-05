package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class StackListener extends Module<ListenerManager> {

    private final List<EntityType> denied;
    private final String color;
    private final boolean enabled;
    private final int radius;
    private final int maxStack;

    public StackListener(ListenerManager manager) {
        super(manager);

        this.denied = getConfig().getStringList("MOB_STACKING.DENIED_ENTITIES").stream().map(EntityType::valueOf).collect(Collectors.toList());
        this.color = getConfig().getString("MOB_STACKING.COLOR");
        this.enabled = getConfig().getBoolean("MOB_STACKING.ENABLED");
        this.radius = getConfig().getInt("MOB_STACKING.RADIUS");
        this.maxStack = getConfig().getInt("MOB_STACKING.MAX_STACK");
        int clearSeconds = getConfig().getInt("MOB_STACKING.DESPAWN_TIME");

        if (clearSeconds > 0) {
            getManager().getTasks().add(Bukkit.getScheduler().runTaskTimer(getInstance(), this::clean, 0L, (20L * clearSeconds)));
        }
    }

    // Used to clean the entities.
    private void clean() {
        for (World world : Bukkit.getWorlds()) {
            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            while (iterator.hasNext()) {
                LivingEntity entity = iterator.next();

                if (getAmount(entity) == -1) continue;

                entity.remove();
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!enabled) return;

        LivingEntity entity = e.getEntity();
        int amount = getAmount(entity);

        if (amount == -1) return;
        if (amount == 1) return; // just let them die

        // Spawn it again
        LivingEntity livingEntity = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());

        if (livingEntity instanceof Ageable) {
            Ageable ageable = (Ageable) livingEntity;

            // Make sure it's an adult
            if (!ageable.isAdult()) {
                ageable.setAdult();
            }
        }

        setAmount(livingEntity, amount - 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        if (!enabled) return;

        LivingEntity entity = e.getEntity();
        boolean newStack = true;

        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        if (denied.contains(entity.getType())) return;

        for (Entity nearbyEntity : entity.getNearbyEntities(radius, radius, radius)) {
            if (!(nearbyEntity instanceof LivingEntity)) continue;
            if (nearbyEntity.getType() != entity.getType()) continue;

            LivingEntity nearby = (LivingEntity) nearbyEntity;
            int amount = getAmount(nearby);

            if (amount == -1) continue;

            if (amount < maxStack) { // limit stack
                e.setCancelled(true); // just add it to the other stack.
                setAmount(nearby, amount + 1);
                newStack = false;
            }
        }

        if (newStack) {
            setAmount(entity, 1); // create a new one if none nearby to stack to.
        }
    }

    private int getAmount(LivingEntity entity) {
        String name = entity.getCustomName();

        if (name == null) return -1;

        try {

            return Integer.parseInt(ChatColor.stripColor(name).replace("x", ""));

        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void setAmount(LivingEntity livingEntity, int amount) {
        livingEntity.setCustomName(color + amount + "x");
        livingEntity.setCustomNameVisible(true);
    }
}