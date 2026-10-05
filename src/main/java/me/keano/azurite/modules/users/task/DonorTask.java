package me.keano.azurite.modules.users.task;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.users.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class DonorTask extends BukkitRunnable {

    private final UserManager manager;

    public DonorTask(UserManager manager) {
        this.manager = manager;
        this.runTaskTimerAsynchronously(manager.getInstance(), 20L * Config.DONOR_INTERVAL, 20L * Config.DONOR_INTERVAL);
    }

    @Override
    public void run() {
        List<String> currentRanks = new ArrayList<>();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("azurite.donor")) continue;
            if (onlinePlayer.isOp()) continue; // Don't count opped players.

            currentRanks.add(onlinePlayer.getName());
        }

        List<String> toSend = new ArrayList<>(Config.DONOR_MESSAGE);
        String members = (currentRanks.isEmpty() ? "None" : String.join(", ", currentRanks));
        toSend.replaceAll(s -> s.replace("%members%", members));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            for (String s : toSend) {
                onlinePlayer.sendMessage(s);
            }

            if (!Config.DONOR_SOUND.isEmpty()) {
                manager.playSound(onlinePlayer, Config.DONOR_SOUND, false);
            }
        }
    }
}