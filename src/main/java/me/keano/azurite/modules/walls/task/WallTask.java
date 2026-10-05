package me.keano.azurite.modules.walls.task;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.walls.Wall;
import me.keano.azurite.modules.walls.WallManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class WallTask extends Module<WallManager> implements Runnable {

    public WallTask(WallManager manager) {
        super(manager);
    }

    @Override
    public void run() {
        try {

            for (Player player : Bukkit.getOnlinePlayers()) {
                Wall wall = getManager().getWalls().get(player.getUniqueId());
                if (wall != null) wall.tick(player);
            }

        } catch (Exception e) {
            getInstance().getLogger().info("Error updating walls: " + e);
        }
    }
}