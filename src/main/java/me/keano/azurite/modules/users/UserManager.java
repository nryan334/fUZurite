package me.keano.azurite.modules.users;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.users.extra.StartingBalance;
import me.keano.azurite.modules.users.listener.UserListener;
import me.keano.azurite.modules.users.task.DonorTask;
import me.keano.azurite.modules.users.task.LeaderboardsTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class UserManager extends Manager {

    private final Map<UUID, User> users;
    private final Map<String, UUID> uuidCache;
    private final Map<Integer, StartingBalance> balances;

    private final List<User> topKills;
    private final List<User> topDeaths;
    private final List<User> topKDR;
    private final List<User> topKillStreaks;

    private DonorTask donorTask;

    public UserManager(HCF instance) {
        super(instance);

        this.users = new ConcurrentHashMap<>();
        this.uuidCache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.balances = new HashMap<>();

        this.topKills = new ArrayList<>();
        this.topDeaths = new ArrayList<>();
        this.topKDR = new ArrayList<>();
        this.topKillStreaks = new ArrayList<>();

        this.load();

        new UserListener(this);
        new LeaderboardsTask(this);
    }

    @Override
    public void reload() {
        balances.clear();
        donorTask.cancel();
        this.load();
    }

    private void load() {
        for (String key : getConfig().getConfigurationSection("STARTING_BALANCES.PER_RANK").getKeys(false)) {
            String string = getConfig().getString("STARTING_BALANCES.PER_RANK." + key);
            String[] split = string.split(", ");
            int priority = Integer.parseInt(split[0]);
            balances.put(priority, new StartingBalance(key, Integer.parseInt(split[1]), priority));
        }

        this.donorTask = new DonorTask(this);
    }

    public String getPrefix(Player player) {
        if (!Config.LUNAR_PREFIXES_ENABLED) return null;

        User user = getByUUID(player.getUniqueId());
        int index = topKills.indexOf(user);

        if (index != -1) {
            if (index == 0) {
                return Config.LUNAR_PREFIXES_ONE;

            } else if (index == 1) {
                return Config.LUNAR_PREFIXES_TWO;

            } else if (index == 2) {
                return Config.LUNAR_PREFIXES_THREE;
            }
        }

        return null;
    }

    public int getStartingBalance(Player player) {
        int highestPriority = -1;

        // This will get the highest priority reclaim.
        for (StartingBalance balance : balances.values()) {
            String perm = "azurite.balance." + balance.getName().toLowerCase();

            if (player.hasPermission(perm) && balance.getPriority() > highestPriority) {
                highestPriority = balance.getPriority();
            }
        }

        StartingBalance bal = balances.get(highestPriority);
        return (bal != null ? bal.getAmount() : Config.DEFAULT_BAL);
    }

    public User getByUUID(UUID uuid) {
        return users.get(uuid);
    }

    public User getByName(String name) {
        Player player = Bukkit.getPlayer(name);

        if (player != null) {
            return getByUUID(player.getUniqueId()); // Support disguise systems
        }

        UUID uuid = uuidCache.get(name);
        return (uuid != null ? users.get(uuid) : null);
    }
}