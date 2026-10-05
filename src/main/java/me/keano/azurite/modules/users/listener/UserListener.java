package me.keano.azurite.modules.users.listener;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.timers.listeners.playertimers.InvincibilityTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class UserListener extends Module<UserManager> {

    public UserListener(UserManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (!getInstance().isLoaded()) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(Config.SERVER_NOT_LOADED);
            return;
        }

        User user = getManager().getByUUID(e.getUniqueId());

        if (user == null) {
            try {

                user = new User(getManager(), e.getUniqueId(), e.getName());
                user.save();

            } catch (Exception exception) {
                exception.printStackTrace();
                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(Config.COULD_NOT_LOAD_DATA);
            }
        }

        if (getManager().getUsers().get(e.getUniqueId()) == null) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(Config.COULD_NOT_LOAD_DATA);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        User user = getManager().getByUUID(player.getUniqueId());
        user.setLastLogin(System.currentTimeMillis());

        if (!user.getName().equals(player.getName())) {
            // Update user cache
            getManager().getUuidCache().put(player.getName(), player.getUniqueId());
            user.setName(player.getName());
            user.save();
        }

        if (!player.hasPlayedBefore() && !getInstance().getSotwManager().isActive()) {
            InvincibilityTimer timer = getInstance().getTimerManager().getInvincibilityTimer();
            if (timer.getSeconds() != 0) timer.applyTimer(player);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        handleLeave(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        handleLeave(e.getPlayer());
    }

    private void handleLeave(Player player) {
        User user = getManager().getByUUID(player.getUniqueId());
        user.setActionBar(null);
        user.updatePlaytime();
        user.save();
    }
}