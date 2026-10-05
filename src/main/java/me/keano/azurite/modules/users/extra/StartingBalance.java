package me.keano.azurite.modules.users.extra;

import lombok.Getter;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class StartingBalance {

    private final String name;
    private final int amount;
    private final int priority;

    public StartingBalance(String name, int amount, int priority) {
        this.name = name;
        this.amount = amount;
        this.priority = priority;
    }
}