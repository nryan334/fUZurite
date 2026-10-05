package me.keano.azurite.modules.events.king;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.king.listener.KingListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class KingManager extends Manager {

    private Player king;
    private String reward;
    private long startedTime;

    public KingManager(HCF instance) {
        super(instance);

        this.king = null;
        this.reward = null;
        this.startedTime = 0L;

        new KingListener(this);
    }

    public boolean isActive() {
        return king != null;
    }

    public void startKing(Player player, String reward) {
        this.king = player;
        this.reward = reward;
        this.startedTime = System.currentTimeMillis();

        getInstance().getKitManager().getKit("ktk").equip(player);

        for (String s : getLanguageConfig().getStringList("KING_EVENTS.BROADCAST_START")) {
            Bukkit.broadcastMessage(s
                    .replace("%player%", king.getName())
                    .replace("%reward%", reward)
            );
        }
    }

    public void stopKing(boolean forced) {
        String killer = (king.getKiller() == null ? "Unknown" : king.getKiller().getName());
        long death = System.currentTimeMillis() - startedTime;

        for (String s : getLanguageConfig().getStringList("KING_EVENTS.BROADCAST_" + (forced ? "END" : "KILL"))) {
            Bukkit.broadcastMessage(s
                    .replace("%player%", king.getName())
                    .replace("%reward%", reward)
                    .replace("%killer%", killer)
                    .replace("%time%", Formatter.formatDetailed(death))
            );
        }

        this.king = null;
        this.reward = null;
        this.startedTime = 0L;
    }
}