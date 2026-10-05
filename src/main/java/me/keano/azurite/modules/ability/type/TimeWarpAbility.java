package me.keano.azurite.modules.ability.type;

import lombok.Getter;
import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TimeWarpAbility extends Ability {

    private final Map<UUID, TimeWarpData> timeWarps;
    private final int seconds;
    private final int delay;

    public TimeWarpAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Time Warp"
        );
        this.timeWarps = new HashMap<>();
        this.seconds = getAbilitiesConfig().getInt("TIME_WARP.PEARL_DELAY");
        this.delay = getAbilitiesConfig().getInt("TIME_WARP.DELAY");
    }

    @Override
    public void onClick(Player player) {
        if (hasCooldown(player)) return;
        if (cannotUse(player)) return;

        TimeWarpData data = timeWarps.remove(player.getUniqueId());

        if (data == null || data.getValidTill() < System.currentTimeMillis()) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.TIMEWARP.INVALID_PEARL")
                    .replace("%seconds%", String.valueOf(seconds))
            );
            return;
        }

        takeItem(player);
        applyCooldown(player);
        Tasks.executeLater(getManager(), 20L * delay, () -> player.teleport(data.getLocation()));

        for (String s : getLanguageConfig().getStringList("ABILITIES.TIMEWARP.USED"))
            player.sendMessage(s
                    .replace("%seconds%", String.valueOf(delay))
            );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThrow(ProjectileLaunchEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof EnderPearl)) return;

        Player player = Utils.getDamager(e.getEntity());

        if (player != null) {
            timeWarps.put(player.getUniqueId(), new TimeWarpData(player.getLocation(), seconds));
        }
    }

    @Getter
    private static class TimeWarpData {

        private final Location location;
        private final long validTill;

        public TimeWarpData(Location location, int usableSeconds) {
            this.location = location;
            this.validTill = System.currentTimeMillis() + (usableSeconds * 1000L);
        }
    }
}