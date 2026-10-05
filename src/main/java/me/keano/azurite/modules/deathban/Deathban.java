package me.keano.azurite.modules.deathban;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Deathban extends Module<DeathbanManager> {

    private UUID uniqueID;
    private String reason;
    private Location location;
    private Date date;
    private long time;

    public Deathban(DeathbanManager manager, UUID uniqueID, long time, String reason, Location location) {
        super(manager);

        this.uniqueID = uniqueID;
        this.reason = reason;
        this.location = location;

        this.date = new Date();
        this.time = System.currentTimeMillis() + time;
    }

    public long getTime() {
        long remaining = (time - System.currentTimeMillis());

        // Uses the scoreboard thread to tick this.
        if (remaining < 0L) { // its expired
            Player player = Bukkit.getPlayer(uniqueID);

            if (player != null) { // if the player is null it'll wait till the player isn't!
                getManager().removeDeathban(player);
            }
        }

        return remaining;
    }

    public String getDateFormatted() {
        return Formatter.formatDate(date);
    }

    public boolean isExpired() {
        return (getTime() < 0L);
    }
}