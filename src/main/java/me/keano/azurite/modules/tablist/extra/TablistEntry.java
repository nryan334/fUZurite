package me.keano.azurite.modules.tablist.extra;

import lombok.Getter;
import lombok.Setter;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class TablistEntry {

    private String text;
    private TablistSkin skin;
    private int ping;

    public TablistEntry(String text, TablistSkin skin, int ping) {
        this.text = text;
        this.skin = skin;
        this.ping = ping;
    }
}