package me.keano.azurite.modules.hooks.abilities;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public interface Abilities {

    List<String> getScoreboardLines(Player player);

    String getRemainingGlobal(Player player);

    boolean hasGlobalCooldown(Player player);

}