package me.keano.azurite.modules.hooks.tags.type;

import com.broustudio.CoreAPI.CoreAPI;
import me.keano.azurite.modules.hooks.tags.Tag;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CoreTag implements Tag {

    @Override
    public String getTag(Player player) {
        String tag = CoreAPI.plugin.tagManager.getTagDisplay(player.getUniqueId());
        return (tag != null ? tag : "");
    }
}