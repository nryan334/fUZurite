package me.keano.azurite.modules.pvpclass.cooldown;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class EnergyCooldown {

    private UUID player;
    private double energy;
    private long maxEnergy;

    public EnergyCooldown(UUID player, int maxEnergy) {
        this.player = player;
        this.energy = System.currentTimeMillis();
        this.maxEnergy = maxEnergy;
    }

    public double getEnergy() {
        // convert to seconds
        double remaining = (System.currentTimeMillis() - energy) / 1000;
        return Math.min(maxEnergy, remaining);
    }

    public boolean checkEnergy(int amount) {
        return getEnergy() < amount;
    }

    public void takeEnergy(int amount) {
        double energy = getEnergy();
        double minus = (energy - amount) * 1000L;
        // basically minus normally and convert to long
        this.energy = (System.currentTimeMillis() - minus);
    }
}