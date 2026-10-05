package me.keano.azurite.modules.nametags.extra;

import lombok.Getter;

import java.util.Objects;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class NameInfo {

    private final String name;
    private final String color;
    private final String prefix;
    private final String suffix;
    private final NameVisibility visibility;
    private final boolean friendlyInvis;

    public NameInfo(String name, String color, String prefix, String suffix, NameVisibility visibility, boolean friendlyInvis) {
        this.name = name;
        this.color = color;
        this.prefix = prefix;
        this.suffix = suffix;
        this.visibility = visibility;
        this.friendlyInvis = friendlyInvis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NameInfo)) return false;
        NameInfo that = (NameInfo) o;
        return friendlyInvis == that.friendlyInvis && Objects.equals(name, that.name) && Objects.equals(color, that.color) && Objects.equals(prefix, that.prefix) && Objects.equals(suffix, that.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color, prefix, suffix, friendlyInvis);
    }
}