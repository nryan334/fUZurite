package me.keano.azurite.modules.timers.listeners.playertimers;

import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AppleTimer extends PlayerTimer {

    private final Map<World.Environment, Integer> limits;
    private final Map<UUID, Integer> playerAmount;

    public AppleTimer(TimerManager manager) {
        super(
                manager,
                ActionBarConfig.APPLE,
                false,
                "Apple",
                "PLAYER_TIMERS.APPLE",
                "TIMERS_COOLDOWN.APPLE"
        );
        this.limits = new HashMap<>();
        this.playerAmount = new HashMap<>();
        this.load();
    }

    @Override
    public void reload() {
        limits.clear();
        this.load();
    }

    private void load() {
        for (String key : getConfig().getConfigurationSection("GOLDEN_APPLE_LIMIT").getKeys(false)) {
            try {

                limits.put(World.Environment.valueOf(key), getConfig().getInt("GOLDEN_APPLE_LIMIT." + key));

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Your environment for golden apple limit is wrong");
            }
        }
    }

    public boolean isLimited(World.Environment environment) {
        return limits.containsKey(environment) && limits.get(environment) != -1;
    }

    public int getLimit(Player player) {
        Integer limit = playerAmount.get(player.getUniqueId());
        return (limit != null ? limit : 0);
    }

    public int getMaxLimit(World.Environment environment) {
        return limits.get(environment);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item.getType() == Material.GOLDEN_APPLE && !getManager().isGapple(item)) {
            World.Environment environment = player.getWorld().getEnvironment();

            if (hasTimer(player)) {
                e.setCancelled(true);
                player.sendMessage(getLanguageConfig().getString("APPLE_TIMER.COOLDOWN")
                        .replace("%seconds%", getRemainingString(player))
                );
                return;
            }

            if (isLimited(environment)) {
                int limit = getMaxLimit(environment);
                playerAmount.putIfAbsent(player.getUniqueId(), 0);
                Integer currentAmount = playerAmount.get(player.getUniqueId());

                if (currentAmount >= limit) {
                    e.setCancelled(true);
                    player.sendMessage(getLanguageConfig().getString("APPLE_LISTENER.CARRY_TOO_MANY")
                            .replace("%limit%", String.valueOf(limit))
                    );
                    return;
                }

                playerAmount.put(player.getUniqueId(), currentAmount + 1);
            }

            if (seconds != 0) {
                applyTimer(player);
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        playerAmount.remove(player.getUniqueId());
    }
}