package me.keano.azurite.modules.kits;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;

import java.util.Map;
import java.util.TreeMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@SuppressWarnings("unchecked")
public class KitManager extends Manager {

    private final Map<String, Kit> kits;

    public KitManager(HCF instance) {
        super(instance);
        this.kits = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.load();
    }

    @Override
    public void disable() {
        for (Kit kit : kits.values()) {
            getKitsData().getValues().put(kit.getName(), kit.serialize());
        }

        getKitsData().save();
    }

    private void load() {
        Map<String, Object> values = getKitsData().getValues();

        for (String s : values.keySet()) {
            Map<String, Object> innerMap = (Map<String, Object>) values.get(s);
            Kit kit = new Kit(this, innerMap);
            kits.put(kit.getName(), kit);
        }

        // Default Kits
        if (getKit("deathban") == null) new Kit(this, "deathban", true).save();
        if (getKit("ktk") == null) new Kit(this, "ktk", true).save();
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }
}