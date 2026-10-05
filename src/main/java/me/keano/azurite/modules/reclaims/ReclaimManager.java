package me.keano.azurite.modules.reclaims;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ReclaimManager extends Manager {

    private final List<Reclaim> reclaims;
    private final List<Reclaim> daily;

    public ReclaimManager(HCF instance) {
        super(instance);
        this.reclaims = new ArrayList<>();
        this.daily = new ArrayList<>();
        this.load();
    }

    @Override
    public void reload() {
        reclaims.clear();
        daily.clear();
        this.load();
    }

    private void load() {
        for (String key : getReclaimsConfig().getConfigurationSection("RECLAIMS").getKeys(false)) {
            Reclaim reclaim = new Reclaim(
                    key,
                    getReclaimsConfig().getStringList("RECLAIMS." + key + ".COMMANDS"),
                    getReclaimsConfig().getInt("RECLAIMS." + key + ".PRIORITY"), false
            );

            reclaims.add(reclaim);
        }

        for (String key : getReclaimsConfig().getConfigurationSection("DAILY").getKeys(false)) {
            Reclaim reclaim = new Reclaim(
                    key,
                    getReclaimsConfig().getStringList("DAILY." + key + ".COMMANDS"),
                    getReclaimsConfig().getInt("DAILY." + key + ".PRIORITY"), true
            );

            daily.add(reclaim);
        }
    }

    public Reclaim getReclaim(Player player) {
        Reclaim highestPriority = null;

        for (Reclaim reclaim : reclaims) {
            if (player.hasPermission(reclaim.getPermission())) {
                if (highestPriority == null || reclaim.getPriority() > highestPriority.getPriority()) {
                    highestPriority = reclaim;
                }
            }
        }

        return highestPriority;
    }

    public Reclaim getDaily(Player player) {
        Reclaim highestPriority = null;

        for (Reclaim reclaim : daily) {
            if (player.hasPermission(reclaim.getPermission())) {
                if (highestPriority == null || reclaim.getPriority() > highestPriority.getPriority()) {
                    highestPriority = reclaim;
                }
            }
        }

        return highestPriority;
    }
}