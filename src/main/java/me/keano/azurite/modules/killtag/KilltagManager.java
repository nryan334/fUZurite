package me.keano.azurite.modules.killtag;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class KilltagManager extends Manager {

    private final Map<String, Killtag> killtags;

    public KilltagManager(HCF instance) {
        super(instance);
        this.killtags = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.load();
    }

    @Override
    public void reload() {
        killtags.clear();
        this.load();
    }

    private void load() {
        for (String key : getConfig().getConfigurationSection("KILL_TAGS.TYPES").getKeys(false)) {
            killtags.put(key, new Killtag(this, key));
        }
    }

    public Killtag getKilltag(Player player) {
        String killTag = getInstance().getUserManager().getByUUID(player.getUniqueId()).getKilltag();
        if (killTag == null) return null;
        return killtags.get(killTag);
    }
}