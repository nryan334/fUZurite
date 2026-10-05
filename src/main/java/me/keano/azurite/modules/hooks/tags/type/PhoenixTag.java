package me.keano.azurite.modules.hooks.tags.type;

import me.keano.azurite.modules.hooks.tags.Tag;
import org.bukkit.entity.Player;
import xyz.refinedev.phoenix.SharedAPI;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PhoenixTag implements Tag {

    @Override
    public String getTag(Player player) {
        xyz.refinedev.phoenix.profile.tag.Tag tag = SharedAPI.getInstance().getProfileHandler().getProfile(player.getUniqueId()).getTag();
        return (tag != null ? tag.getPrefix() : "");
    }
}