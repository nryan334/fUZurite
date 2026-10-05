package me.keano.azurite.modules.killtag;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class Killtag extends Module<KilltagManager> {

    private final String name;
    private final String format;
    private final String displayName;
    private final Material material;
    private final List<String> lore;
    private final int slot;

    public Killtag(KilltagManager manager, String name) {
        super(manager);
        this.name = name;
        this.format = manager.getConfig().getString("KILL_TAGS.TYPES." + name + ".FORMAT");
        this.displayName = manager.getConfig().getString("KILL_TAGS.TYPES." + name + ".NAME");
        this.slot = manager.getConfig().getInt("KILL_TAGS.TYPES." + name + ".SLOT");
        this.material = ItemUtils.getMat(manager.getConfig().getString("KILL_TAGS.TYPES." + name + ".MATERIAL"));
        this.lore = manager.getConfig().getStringList("KILL_TAGS.TYPES." + name + ".LORE");
    }

    public boolean hasPerm(Player player) {
        return player.hasPermission("azurite.killtag." + name.toLowerCase());
    }
}