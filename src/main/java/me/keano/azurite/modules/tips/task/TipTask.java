package me.keano.azurite.modules.tips.task;

import me.keano.azurite.modules.tips.TipManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TipTask extends BukkitRunnable {

    private final TipManager manager;

    public TipTask(TipManager manager) {
        this.manager = manager;
        this.runTaskTimerAsynchronously(manager.getInstance(), 20 * 30L, manager.getSchedulesConfig().getInt("TIPS.SECONDS") * 20L);
    }

    @Override
    public void run() {
        List<String> tip = manager.getNextTip();

        if (tip != null) {
            for (String s : tip) {
                Bukkit.getConsoleSender().sendMessage(s.replace("%player%", "Console"));
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                for (String s : tip) {
                    player.sendMessage(s
                            .replace("%player%", player.getName())
                    );
                }

                if (!manager.getSound().isEmpty()) {
                    manager.playSound(player, manager.getSound(), false);
                }
            }
        }
    }
}