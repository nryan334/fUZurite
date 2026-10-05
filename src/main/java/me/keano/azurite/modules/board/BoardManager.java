package me.keano.azurite.modules.board;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.board.adapter.AzuriteBoard;
import me.keano.azurite.modules.board.extra.ActionBarConfig;
import me.keano.azurite.modules.board.extra.AnimatedString;
import me.keano.azurite.modules.board.listener.BoardListener;
import me.keano.azurite.modules.board.task.BoardTask;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.extra.NameThreadFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class BoardManager extends Manager {

    private final Map<UUID, Board> boards;
    private final ScheduledExecutorService executor;

    private BoardAdapter adapter;
    private AnimatedString title;
    private AnimatedString footer;

    public BoardManager(HCF instance) {
        super(instance);

        this.boards = new ConcurrentHashMap<>();
        this.adapter = new AzuriteBoard(this);

        this.title = new AnimatedString(this, getStringList("TITLE_CONFIG.CHANGES"), getLong("TITLE_CONFIG.CHANGER_TICKS"));
        this.footer = new AnimatedString(this, getStringList("FOOTER_CONFIG.CHANGES"), getLong("FOOTER_CONFIG.CHANGER_TICKS"));

        this.executor = Executors.newScheduledThreadPool(1, new NameThreadFactory("Azurite - BoardThread"));
        this.executor.scheduleAtFixedRate(new BoardTask(this), 0L, 100L, TimeUnit.MILLISECONDS);

        this.cacheActionBarConfig();
        new BoardListener(this);
    }

    @Override
    public void disable() {
        executor.shutdownNow();
    }

    @Override
    public void reload() {
        this.cacheActionBarConfig();
        this.adapter = new AzuriteBoard(this); // re-fetch the values
        this.title = new AnimatedString(this, getStringList("TITLE_CONFIG.CHANGES"), getLong("TITLE_CONFIG.CHANGER_TICKS"));
        this.footer = new AnimatedString(this, getStringList("FOOTER_CONFIG.CHANGES"), getLong("FOOTER_CONFIG.CHANGER_TICKS"));
    }

    private void cacheActionBarConfig() {
        for (ActionBarConfig config : ActionBarConfig.values()) {
            config.cache(this);
        }
    }

    public List<String> getStringList(String path) {
        return getScoreboardConfig().getStringList(path);
    }

    public long getLong(String path) {
        return getScoreboardConfig().getLong(path);
    }
}