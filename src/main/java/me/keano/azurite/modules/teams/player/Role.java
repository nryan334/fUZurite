package me.keano.azurite.modules.teams.player;

import lombok.Getter;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public enum Role {

    MEMBER("Member"),
    CAPTAIN("Captain"),
    CO_LEADER("Co-Leader"),
    LEADER("Leader");

    private final String name;

    Role(String name) {
        this.name = name;
    }
}