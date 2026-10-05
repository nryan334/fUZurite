package me.keano.azurite.modules.nametags.packet;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.nametags.NametagManager;
import me.keano.azurite.modules.nametags.extra.NameVisibility;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public abstract class NametagPacket extends Module<NametagManager> {

    protected final Player player;

    public NametagPacket(NametagManager manager, Player player) {
        super(manager);
        this.player = player;
    }

    public abstract void create(String name, String color, String prefix, String suffix, boolean friendlyInvis, NameVisibility visibility);

    public abstract void addToTeam(String target, String team);

    public abstract void delete();
}