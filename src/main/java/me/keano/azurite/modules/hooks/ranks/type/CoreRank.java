package me.keano.azurite.modules.hooks.ranks.type;

import com.broustudio.CoreAPI.CoreAPI;
import me.keano.azurite.modules.hooks.ranks.Rank;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CoreRank implements Rank {

    @Override
    public String getRankName(Player player) {
        return CoreAPI.plugin.rankManager.getRank(player.getUniqueId());
    }

    @Override
    public String getRankPrefix(Player player) {
        return CoreAPI.plugin.rankManager.getRankPrefix(player.getUniqueId());
    }

    @Override
    public String getRankSuffix(Player player) {
        return "";
    }

    @Override
    public String getRankColor(Player player) {
        return CoreAPI.plugin.rankManager.getRankColor(player.getUniqueId());
    }
}