package me.keano.azurite.modules.events.conquest;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.conquest.extra.Capzone;
import me.keano.azurite.modules.events.conquest.listener.ConquestListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.teams.claims.Coordinate3D;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Location;

import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@SuppressWarnings("unchecked")
public class ConquestManager extends Manager {

    private final Conquest conquest;
    private final Triple<String, Coordinate3D, Capzone> captureZones;

    public ConquestManager(HCF instance) {
        super(instance);

        this.captureZones = new Triple<>();
        this.conquest = new Conquest(this, (Map<String, Object>) getEventsData().getValues().get("Conquest"));

        new ConquestListener(this);
        Tasks.executeScheduled(this, 20, this::tick);
    }

    @Override
    public void disable() {
        conquest.save();
    }

    public void tick() {
        for (Capzone capzone : conquest.getCapzones().values()) {
            capzone.tick();
        }
    }

    public Capzone getZone(Location location) {
        String name = location.getWorld().getName();
        return captureZones.get(name, new Coordinate3D(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}