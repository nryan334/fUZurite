package me.keano.azurite.modules.hooks.ranks.type;

import com.broustudio.AtomAPI.AtomAPI;
import me.keano.azurite.modules.hooks.ranks.Rank;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AtomRank implements Rank {

    @Override
    public String getRankName(Player player) {
        return AtomAPI.getInstance().rankManager.getRank(player.getUniqueId());
    }

    @Override
    public String getRankPrefix(Player player) {
        return AtomAPI.getInstance().rankManager.getRankPrefix(player.getUniqueId());
    }

    @Override
    public String getRankSuffix(Player player) {
        return AtomAPI.getInstance().rankManager.getRankSuffix(player.getUniqueId());
    }

    @Override
    public String getRankColor(Player player) {
        return AtomAPI.getInstance().rankManager.getRankColor(player.getUniqueId());
    }
}