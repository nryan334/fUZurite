package me.keano.azurite.modules.events.koth;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.EventType;
import me.keano.azurite.modules.events.koth.listener.KothListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.teams.claims.Coordinate3D;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@SuppressWarnings("unchecked")
public class KothManager extends Manager {

    private final Map<String, Koth> koths;
    private final Triple<String, Coordinate3D, Koth> noAntiClean;
    private final Triple<String, Coordinate3D, Koth> captureZones;

    public KothManager(HCF instance) {
        super(instance);

        this.koths = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.noAntiClean = new Triple<>();
        this.captureZones = new Triple<>();

        this.load();
        new KothListener(this);
    }

    @Override
    public void disable() {
        for (Koth koth : koths.values()) {
            koth.save();
        }
    }

    private void load() {
        for (Object object : getEventsData().getValues().values()) {
            Map<String, Object> map = (Map<String, Object>) object;

            // it's not a koth rather a different event.
            if (EventType.valueOf((String) map.get("type")) != EventType.KOTH) continue;

            Koth koth = new Koth(this, map);
            koth.save(); // we need to save the capture zone to the map
        }
    }

    public Koth getKoth(String name) {
        return koths.get(name);
    }

    public boolean isAntiCleanRadius(Location location) {
        String name = location.getWorld().getName();
        return noAntiClean.contains(name, new Coordinate3D(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    public Koth getZone(Location location) {
        String name = location.getWorld().getName();
        return captureZones.get(name, new Coordinate3D(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    public List<Koth> getActiveKoths() {
        List<Koth> koths = new ArrayList<>();

        for (Koth koth : getKoths().values()) {
            if (koth.isActive()) koths.add(koth);
        }

        return koths;
    }
}