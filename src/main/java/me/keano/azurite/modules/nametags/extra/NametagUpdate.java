package me.keano.azurite.modules.nametags.extra;

import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class NametagUpdate {

    private final Player viewer;
    private final Player target;

    public NametagUpdate(Player viewer, Player target) {
        this.viewer = viewer;
        this.target = target;
    }
}