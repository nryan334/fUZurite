package me.keano.azurite.modules.tablist.listener;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.tablist.Tablist;
import me.keano.azurite.modules.tablist.TablistManager;
import me.keano.azurite.modules.tablist.extra.TablistSkin;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TablistListener extends Module<TablistManager> {

    public TablistListener(TablistManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // call last
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Tasks.executeLaterAsync(getManager(), 20L, () -> new Tablist(getManager(), player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        // Remove them from the map to help ram usage.
        getManager().getTablists().remove(player.getUniqueId());
        TablistSkin.SKIN_CACHE.remove(player.getUniqueId());
    }
}