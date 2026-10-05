package me.keano.azurite.modules.users.task;

import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LeaderboardsTask extends BukkitRunnable {

    private final UserManager manager;

    public LeaderboardsTask(UserManager manager) {
        this.manager = manager;
        this.start();
    }

    @Override
    public void run() {
        List<User> sorted = new ArrayList<>(manager.getUsers().values());

        sorted.sort(Comparator.comparingInt(User::getKills).reversed());
        this.checkSurpass(sorted);
        manager.getTopKills().clear();
        manager.getTopKills().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.sort(Comparator.comparingInt(User::getDeaths).reversed());
        manager.getTopDeaths().clear();
        manager.getTopDeaths().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.sort(Comparator.comparingInt(User::getKillstreak).reversed());
        manager.getTopKillStreaks().clear();
        manager.getTopKillStreaks().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.sort(Comparator.comparingDouble(User::getKDR).reversed());
        manager.getTopKDR().clear();
        manager.getTopKDR().addAll(sorted.stream().limit(20).collect(Collectors.toList()));

        sorted.clear();
    }

    private void checkSurpass(List<User> newUsers) {
        List<User> oldUsers = manager.getTopKills();
        int size = Math.min(oldUsers.size(), newUsers.size());

        for (int i = 0; i < Math.min(size, 3); i++) {
            User oldUser = oldUsers.get(i);
            User newUser = newUsers.get(i);
            int newPosition = newUsers.indexOf(oldUser);

            if (!oldUser.getUniqueID().equals(newUser.getUniqueID()) && newUser.getKills() > oldUser.getKills() && newPosition > i) {
                Bukkit.broadcastMessage(manager.getLanguageConfig().getString("LEADERBOARDS_SURPASS")
                        .replace("%player%", newUser.getName())
                        .replace("%target%", oldUser.getName())
                        .replace("%pos%", String.valueOf(i + 1))
                );
            }
        }
    }

    public void start() {
        this.runTaskTimerAsynchronously(manager.getInstance(), 0L, 20 * 30); // 30s
    }
}