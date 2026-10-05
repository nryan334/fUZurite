package me.keano.azurite.modules.framework.extra;

import lombok.Getter;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class FancyMessageData {

    private final String inside;
    private final String hover;
    private final String command;

    public FancyMessageData(String inside, String hover, String command) {
        this.inside = inside;
        this.hover = hover;
        this.command = command;
    }
}