package me.keano.azurite.modules.hooks.tags.type;

import me.keano.azurite.modules.hooks.tags.Tag;
import me.quartz.hestia.HestiaAPI;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class HestiaTag implements Tag {

    @Override
    public String getTag(Player player) {
        return HestiaAPI.instance.getTagPrefix(player.getUniqueId());
    }
}