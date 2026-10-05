package me.keano.azurite.modules.hooks.ranks.type;

import me.andyreckt.holiday.api.HolidayAPI;
import me.keano.azurite.modules.hooks.ranks.Rank;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class HolidayRank implements Rank {

    @Override
    public String getRankName(Player player) {
        return HolidayAPI.getInstance().getRank(player.getUniqueId()).getName();
    }

    @Override
    public String getRankPrefix(Player player) {
        return HolidayAPI.getInstance().getRank(player.getUniqueId()).getPrefix();
    }

    @Override
    public String getRankSuffix(Player player) {
        return HolidayAPI.getInstance().getRank(player.getUniqueId()).getSuffix();
    }

    @Override
    public String getRankColor(Player player) {
        return HolidayAPI.getInstance().getRank(player.getUniqueId()).getColor();
    }
}