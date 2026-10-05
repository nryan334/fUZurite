package me.keano.azurite.modules.hooks.tags.type;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.hooks.tags.Tag;
import me.keano.core.CoreAPI;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AzuriteTag implements Tag {

    private final CoreAPI coreAPI;

    public AzuriteTag(HCF instance) {
        this.coreAPI = new CoreAPI(instance);
    }

    @Override
    public String getTag(Player player) {
        return coreAPI.getTag(player.getUniqueId());
    }
}