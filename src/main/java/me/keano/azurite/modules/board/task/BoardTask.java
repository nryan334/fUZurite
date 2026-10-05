package me.keano.azurite.modules.board.task;

import me.keano.azurite.modules.board.Board;
import me.keano.azurite.modules.board.BoardManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BoardTask extends Module<BoardManager> implements Runnable {

    public BoardTask(BoardManager manager) {
        super(manager);
    }

    @Override
    public void run() {
        try {

            // tick timers before ticking the board, this will allow it to be sync with expiring.
            TimerManager timerManager = getInstance().getTimerManager();

            for (PlayerTimer timer : timerManager.getPlayerTimers().values()) {
                timer.tick();
            }

            for (CustomTimer timer : timerManager.getCustomTimers().values()) {
                timer.tick();
            }

            // Tick sotw
            getInstance().getSotwManager().tick();

            if (Config.SCOREBOARD_ENABLED) {
                // Tick title and footer change
                getManager().getTitle().tick();
                getManager().getFooter().tick();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Board board = getManager().getBoards().get(player.getUniqueId());

                    // not sure if this would happen but just in case.
                    if (board != null) {
                        board.update();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}