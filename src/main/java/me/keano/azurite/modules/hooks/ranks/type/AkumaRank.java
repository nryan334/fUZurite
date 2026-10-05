package me.keano.azurite.modules.hooks.ranks.type;

import cc.insidious.akuma.api.AkumaAPI;
import me.keano.azurite.modules.hooks.ranks.Rank;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AkumaRank implements Rank {

    @Override
    public String getRankName(Player player) {
        return AkumaAPI.getInstance().getGrantHandler().getActiveRank(player.getUniqueId()).getName();
    }

    @Override
    public String getRankPrefix(Player player) {
        return AkumaAPI.getInstance().getGrantHandler().getActiveRank(player.getUniqueId()).getPrefix();
    }

    @Override
    public String getRankSuffix(Player player) {
        return AkumaAPI.getInstance().getGrantHandler().getActiveRank(player.getUniqueId()).getSuffix();
    }

    @Override
    public String getRankColor(Player player) {
        return AkumaAPI.getInstance().getGrantHandler().getActiveRank(player.getUniqueId()).getColor().getCode();
    }
}