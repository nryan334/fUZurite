package me.keano.azurite.modules.nametags.task;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.nametags.NametagManager;
import me.keano.azurite.modules.versions.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ConcurrentModificationException;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("ALL")
public class NametagTask extends Module<NametagManager> implements Runnable {

    public NametagTask(NametagManager manager) {
        super(manager);
    }

    @Override
    public void run() {
        try {

            Version version = getInstance().getVersionManager().getVersion();

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                // Tracked players doesn't contain the viewer
                getManager().handleUpdate(viewer, viewer);

                // When in staff tracked players are empty because you are hidden
                for (Player staff : viewer.spigot().getHiddenPlayers()) {
                    if (staff == viewer) continue;
                    getManager().handleUpdate(staff, viewer);
                }

                for (Player target : version.getTrackedPlayers(viewer)) {
                    if (viewer == target) continue;
                    getManager().handleUpdate(viewer, target);
                }
            }

        } catch (Exception e) {
            if (e instanceof ConcurrentModificationException) return;
            e.printStackTrace();
        }
    }
}