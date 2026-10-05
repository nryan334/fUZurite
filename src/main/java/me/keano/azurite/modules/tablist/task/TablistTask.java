package me.keano.azurite.modules.tablist.task;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.tablist.Tablist;
import me.keano.azurite.modules.tablist.TablistManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TablistTask extends Module<TablistManager> implements Runnable {

    public TablistTask(TablistManager manager) {
        super(manager);
    }

    @Override
    public void run() {
        try {

            getManager().getTitle().tick();

            for (Player player : Bukkit.getOnlinePlayers()) {
                Tablist tablist = getManager().getTablists().get(player.getUniqueId());

                if (tablist != null) {
                    tablist.update();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}