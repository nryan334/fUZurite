package me.keano.azurite.modules.signs.economy;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.signs.CustomSign;
import me.keano.azurite.modules.signs.CustomSignManager;
import me.keano.azurite.modules.spawners.Spawner;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.extra.Pair;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class EconomySign extends CustomSign {

    private final Map<String, ItemStack> cache;
    private final Map<UUID, Pair<Sign, BukkitTask>> taskCache;

    public EconomySign(CustomSignManager manager, List<String> lines) {
        super(manager, lines);
        this.cache = new HashMap<>();
        this.taskCache = new HashMap<>();
    }

    @Override
    public void onClick(Player player, Sign sign) {
    }

    public void sendSignChange(Player player, Sign sign, String[] fakeLines) {
        // if we already have a task running, send the proper lines back for old one and cancel that task
        if (taskCache.containsKey(player.getUniqueId())) {
            Pair<Sign, BukkitTask> pair = taskCache.remove(player.getUniqueId());
            player.sendSignChange(pair.getKey().getLocation(), pair.getKey().getLines());
            pair.getValue().cancel();
        }

        player.sendSignChange(sign.getLocation(), fakeLines);

        taskCache.put(player.getUniqueId(), new Pair<>(sign, new BukkitRunnable() {
            @Override
            public void run() {
                player.sendSignChange(sign.getLocation(), sign.getLines());
                taskCache.remove(player.getUniqueId());
            }
        }.runTaskLater(getInstance(), 40L)));
    }

    public ItemStack getItemStack(String material) {
        if (cache.containsKey(material)) {
            return cache.get(material);
        }

        // Handle the ways of buying spawners
        if (material.contains("Spawner ")) {
            String[] split = material.split(" ");
            Spawner spawner = getInstance().getSpawnerManager().getByName(split[1]);

            if (spawner == null) {
                return null;
            } else return spawner.getItemStack();

        } else if (material.contains(" Spawner")) {
            String[] split = material.split(" ");
            Spawner spawner = getInstance().getSpawnerManager().getByName(split[0]);

            if (spawner == null) {
                return null;
            } else return spawner.getItemStack();
        }

        if (material.equals("Crowbar")) {
            return Config.CROWBAR.clone();
        }

        if (material.contains(":")) {
            String[] split = material.split(":");
            Material mat = tryGetMat(split[0]);
            if (mat == null) return null;
            ItemStack item = new ItemBuilder(mat)
                    .data(getManager(), Short.parseShort(split[1]))
                    .toItemStack();
            cache.put(material, item);
            return item;
        }

        Material mat = tryGetMat(material);
        if (mat == null) return null;
        ItemStack item = new ItemStack(mat);
        cache.put(material, item);
        return item;
    }

    private Material tryGetMat(String string) {
        try {

            return ItemUtils.getMat(string);

        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}