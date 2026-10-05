package me.keano.azurite.modules.hooks.tags.type;

import com.broustudio.MizuAPI.MizuAPI;
import me.keano.azurite.modules.hooks.tags.Tag;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MizuTag implements Tag {

    @Override
    public String getTag(Player player) {
        String tag = MizuAPI.getAPI().getTagDisplay(MizuAPI.getAPI().getTag(player.getUniqueId()));
        return (tag != null ? tag : "");
    }
}