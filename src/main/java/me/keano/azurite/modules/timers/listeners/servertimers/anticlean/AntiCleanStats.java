package me.keano.azurite.modules.timers.listeners.servertimers.anticlean;

import lombok.Getter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.extra.Pair;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class AntiCleanStats {

    private final Map<UUID, Integer> hits;
    private final Map<UUID, Integer> archerTags;
    private final Map<UUID, Integer> goldenApples;
    private final Map<UUID, Integer> potsUsed;
    private final Map<UUID, Double> damageDealt;

    public AntiCleanStats() {
        this.hits = new HashMap<>();
        this.archerTags = new HashMap<>();
        this.goldenApples = new HashMap<>();
        this.potsUsed = new HashMap<>();
        this.damageDealt = new HashMap<>();
    }

    public int getStatInt(Player player, Map<UUID, Integer> map) {
        return map.getOrDefault(player.getUniqueId(), 0);
    }

    public double getStatDouble(Player player, Map<UUID, Double> map) {
        return map.getOrDefault(player.getUniqueId(), 0.0D);
    }

    public void incrementInt(Player player, Map<UUID, Integer> map) {
        int current = map.getOrDefault(player.getUniqueId(), 0);
        map.put(player.getUniqueId(), current + 1);
    }

    public void incrementDouble(Player player, Map<UUID, Double> map, double amount) {
        double current = map.getOrDefault(player.getUniqueId(), 0.0D);
        map.put(player.getUniqueId(), current + amount);
    }

    public int getTotalInt(Map<UUID, Integer> map) {
        int total = 0;

        for (Integer value : map.values()) {
            total += value;
        }

        return total;
    }

    public double getTotalDouble(Map<UUID, Double> map) {
        double damage = 0.0D;

        for (Double value : map.values()) {
            damage += value;
        }

        return damage;
    }

    public List<Pair<UUID, Integer>> getTopHits() {
        List<Pair<UUID, Integer>> topHits = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : hits.entrySet()) {
            topHits.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        topHits.sort(Comparator.comparingInt(Pair::getValue));
        Collections.reverse(topHits);
        return topHits;
    }

    public String getTopHitsMessage(Manager manager, int number, List<Pair<UUID, Integer>> list) {
        if (number > list.size()) {
            return Config.ANTICLEAN_STATS_EMPTY;
        }

        Pair<UUID, Integer> pair = list.get(number - 1);
        User user = manager.getInstance().getUserManager().getByUUID(pair.getKey());
        return Config.ANTICLEAN_STATS_HITS_MESSAGE
                .replace("%player%", user.getName())
                .replace("%hits%", String.valueOf(pair.getValue()));
    }
}