package me.keano.azurite.modules.board.listener;

import me.keano.azurite.modules.board.Board;
import me.keano.azurite.modules.board.BoardManager;
import me.keano.azurite.modules.framework.Module;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BoardListener extends Module<BoardManager> {

    public BoardListener(BoardManager manager) {
        super(manager);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        getManager().getBoards().put(player.getUniqueId(), new Board(getManager(), player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        getManager().getBoards().remove(player.getUniqueId());
    }
}