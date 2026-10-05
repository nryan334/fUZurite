package me.keano.azurite.modules.events.conquest.extra;

import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public enum ConquestType {

    RED("Red", ChatColor.RED),
    BLUE("Blue", ChatColor.BLUE),
    GREEN("Green", ChatColor.GREEN),
    YELLOW("Yellow", ChatColor.YELLOW);

    private final String name;
    private final ChatColor color;

    ConquestType(String name, ChatColor color) {
        this.name = name;
        this.color = color;
    }
}