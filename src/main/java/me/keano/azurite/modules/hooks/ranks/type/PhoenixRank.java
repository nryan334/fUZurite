package me.keano.azurite.modules.hooks.ranks.type;

import me.keano.azurite.modules.hooks.ranks.Rank;
import org.bukkit.entity.Player;
import xyz.refinedev.phoenix.SharedAPI;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PhoenixRank implements Rank {

    @Override
    public String getRankName(Player player) {
        xyz.refinedev.phoenix.rank.Rank rank = SharedAPI.getInstance().getProfileHandler().getProfile(player.getUniqueId()).getHighestRank();
        return (rank != null ? rank.getName() : "");
    }

    @Override
    public String getRankPrefix(Player player) {
        xyz.refinedev.phoenix.rank.Rank rank = SharedAPI.getInstance().getProfileHandler().getProfile(player.getUniqueId()).getHighestRank();
        return (rank != null ? rank.getPrefix() : "");
    }

    @Override
    public String getRankSuffix(Player player) {
        xyz.refinedev.phoenix.rank.Rank rank = SharedAPI.getInstance().getProfileHandler().getProfile(player.getUniqueId()).getHighestRank();
        return (rank != null ? rank.getSuffix() : "");
    }

    @Override
    public String getRankColor(Player player) {
        xyz.refinedev.phoenix.rank.Rank rank = SharedAPI.getInstance().getProfileHandler().getProfile(player.getUniqueId()).getHighestRank();
        return (rank != null ? rank.getColor() : "");
    }
}