package me.keano.azurite.modules.events.sotw;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.events.sotw.listener.SOTWListener;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class SOTWManager extends Manager {

    private final List<UUID> enabled;
    private final List<UUID> flying;
    private Long remaining;
    private boolean active;

    public SOTWManager(HCF instance) {
        super(instance);

        this.enabled = new ArrayList<>();
        this.flying = new ArrayList<>();
        this.remaining = 0L;
        this.active = false;

        new SOTWListener(this);
    }

    @Override
    public void disable() {
        for (UUID uuid : flying) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.setFlying(false);
        }
    }

    public String getRemainingString() {
        long rem = remaining - System.currentTimeMillis();
        if (rem < 0L) {
            return "";
        }
        return Formatter.getRemaining(rem, false);
    }

    public void tick() {
        long rem = remaining - System.currentTimeMillis();

        if (rem < 0L && active) {
            this.active = false;
            Tasks.execute(this, this::endSOTW);
        }
    }

    public boolean isEnabled(Player player) {
        return enabled.contains(player.getUniqueId());
    }

    public boolean canFly(Player player, Team at) {
        // Anywhere
        if (getConfig().getBoolean("SOTW_FLY.ALLOW_ANYWHERE") && player.hasPermission("azurite.sotw.fly.anywhere")) {
            return true;
        }

        // Team Check
        if (getConfig().getBoolean("SOTW_FLY.ALLOW_OWN_CLAIM") && at instanceof PlayerTeam) {
            PlayerTeam pt = (PlayerTeam) at;
            return pt.getPlayers().contains(player.getUniqueId());
        }

        // Sotw check
        return getConfig().getBoolean("SOTW_FLY.ALLOW_SPAWN") && at instanceof SafezoneTeam;
    }

    public boolean toggleFly(Player player) {
        if (flying.remove(player.getUniqueId())) {
            player.setAllowFlight(false);
            player.setFlying(false);
            return false;

        } else {
            flying.add(player.getUniqueId());
            player.setAllowFlight(true);
            player.setFlying(true);
            return true;
        }
    }

    public void startSOTW(long time) {
        this.active = true;
        this.remaining = System.currentTimeMillis() + time;

        for (String s : getLanguageConfig().getStringList("SOTW_TIMER.STARTED_SOTW")) {
            Bukkit.broadcastMessage(s);
        }
    }

    public void extendSOTW(long time) {
        this.active = true; // Make sure?
        this.remaining = getRemaining() + time;

        for (String s : getLanguageConfig().getStringList("SOTW_TIMER.EXTENDED_SOTW")) {
            Bukkit.broadcastMessage(s
                    .replace("%time%", Formatter.formatDetailed(time))
            );
        }
    }

    public void endSOTW() {
        this.active = false;
        this.remaining = 0L;

        for (UUID uuid : getFlying()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.setFlying(false);
        }

        for (String s : getLanguageConfig().getStringList("SOTW_TIMER.ENDED_SOTW")) {
            Bukkit.broadcastMessage(s);
        }
    }
}