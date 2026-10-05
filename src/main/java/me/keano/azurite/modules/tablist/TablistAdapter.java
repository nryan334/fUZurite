package me.keano.azurite.modules.tablist;

import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public interface TablistAdapter {

    String[] getHeader(Player player);

    String[] getFooter(Player player);

    Tablist getInfo(Player player);

}