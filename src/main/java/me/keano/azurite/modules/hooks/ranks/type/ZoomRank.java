package me.keano.azurite.modules.hooks.ranks.type;

import club.frozed.core.ZoomAPI;
import me.keano.azurite.modules.hooks.ranks.Rank;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ZoomRank implements Rank {

    @Override
    public String getRankName(Player player) {
        return ZoomAPI.getRankName(player);
    }

    @Override
    public String getRankPrefix(Player player) {
        return ZoomAPI.getRankPrefix(player);
    }

    @Override
    public String getRankSuffix(Player player) {
        return ZoomAPI.getRankSuffix(player);
    }

    @Override
    public String getRankColor(Player player) {
        return ZoomAPI.getRankColor(player).toString();
    }
}