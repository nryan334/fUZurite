package me.keano.azurite.modules.hooks.tags.type;

import me.activated.core.plugin.AquaCoreAPI;
import me.keano.azurite.modules.hooks.tags.Tag;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AquaCoreTag implements Tag {

    @Override
    public String getTag(Player player) {
        return AquaCoreAPI.INSTANCE.getTagFormat(player);
    }
}