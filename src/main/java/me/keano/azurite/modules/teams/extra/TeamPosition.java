package me.keano.azurite.modules.teams.extra;

import lombok.Getter;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class TeamPosition {

    private final String color;
    private final String prefix;

    public TeamPosition(String color, String prefix) {
        this.color = color;
        this.prefix = prefix;
    }
}