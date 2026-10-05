package me.keano.azurite.modules.nametags;

import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public interface NametagAdapter {

    String getAndUpdate(Player player, Player target);

}