package me.keano.azurite.utils.extra;

import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.Formatter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamCooldown {

    private final Map<UUID, Long> cooldowns;

    public TeamCooldown(Manager manager) {
        this.cooldowns = new ConcurrentHashMap<>();
        manager.getInstance().getTeamCooldowns().add(this);
    }

    public void clean() {
        cooldowns.values().removeIf(next -> next - System.currentTimeMillis() < 0L);
    }

    public void applyCooldown(PlayerTeam pt, int seconds) {
        cooldowns.put(pt.getUniqueID(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public boolean hasCooldown(PlayerTeam pt) {
        return cooldowns.containsKey(pt.getUniqueID()) && (cooldowns.get(pt.getUniqueID()) >= System.currentTimeMillis());
    }

    public void removeCooldown(PlayerTeam pt) {
        cooldowns.remove(pt.getUniqueID());
    }

    public String getRemaining(PlayerTeam pt) {
        long l = this.cooldowns.get(pt.getUniqueID()) - System.currentTimeMillis();
        return Formatter.getRemaining(l, true);
    }
}