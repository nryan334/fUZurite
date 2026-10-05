package me.keano.azurite.modules.nametags.listener;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.nametags.Nametag;
import me.keano.azurite.modules.nametags.NametagManager;
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
public class NametagListener extends Module<NametagManager> {

    public NametagListener(NametagManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.LOWEST) // call first
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        getManager().getNametags().put(player.getUniqueId(), new Nametag(getManager(), player));
    }

    @EventHandler(priority = EventPriority.HIGHEST) // call last
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Nametag nametag = getManager().getNametags().remove(player.getUniqueId());
        if (nametag != null) nametag.delete();
    }
}