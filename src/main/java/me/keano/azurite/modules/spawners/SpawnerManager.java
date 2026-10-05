package me.keano.azurite.modules.spawners;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.spawners.listener.SpawnerListener;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class SpawnerManager extends Manager {

    private final Map<EntityType, Spawner> spawners;

    public SpawnerManager(HCF instance) {
        super(instance);

        this.spawners = new HashMap<>();
        this.load();

        new SpawnerListener(this);
    }

    @Override
    public void reload() {
        spawners.clear();
        this.load();
    }

    private void load() {
        for (String s : getConfig().getStringList("SPAWNERS_CONFIG.TYPES")) {
            String[] split = s.split(", ");
            spawners.put(EntityType.valueOf(split[0]), new Spawner(this, EntityType.valueOf(split[0]), split[1]));
        }
    }

    public Spawner getByName(String string) {
        if (string.equalsIgnoreCase("SKELE") || string.equalsIgnoreCase("SKELLY")) {
            return spawners.get(EntityType.SKELETON);

        } else if (string.equalsIgnoreCase("CSPIDER")) {
            return spawners.get(EntityType.CAVE_SPIDER);

        } else if (string.equalsIgnoreCase("ENDER")) {
            return spawners.get(EntityType.ENDERMAN);
        }

        try {

            return spawners.get(EntityType.valueOf(string.toUpperCase()));

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Spawner getByItem(ItemStack itemStack) {
        for (Spawner value : spawners.values()) {
            if (!value.getItemStack().isSimilar(itemStack)) continue;
            return value;
        }

        return null;
    }
}