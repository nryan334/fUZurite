package me.keano.azurite.modules.reclaims;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Reclaim {

    private final String name;
    private final String permission;
    private final List<String> commands;
    private final int priority;

    public Reclaim(String name, List<String> commands, int priority, boolean daily) {
        this.name = name;
        this.permission = "azurite." + (daily ? "daily." : "reclaim.") + name.toLowerCase();
        this.commands = commands;
        this.priority = priority;
    }
}