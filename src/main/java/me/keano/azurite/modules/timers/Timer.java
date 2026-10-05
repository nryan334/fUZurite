package me.keano.azurite.modules.timers;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.configs.ConfigYML;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Timer extends Module<TimerManager> {

    protected String name;
    protected String scoreboardPath;
    protected String scoreboard;
    protected String secondsPath;
    protected int seconds;

    public Timer(TimerManager manager, String name, String scoreboardPath, String secondsPath) {
        super(manager);
        this.name = name;
        this.scoreboardPath = scoreboardPath;
        this.secondsPath = secondsPath;
        this.scoreboard = null;
        this.seconds = 0;
        this.fetchData();
    }

    public void fetchData() {
        if (!scoreboardPath.isEmpty()) {
            String fetch = getScoreboardConfig().getString(scoreboardPath);
            this.scoreboard = (fetch.isEmpty() ? null : fetch);
        }

        if (!secondsPath.isEmpty()) {
            Integer fetch = null;

            // Search all the configs
            for (ConfigYML config : getInstance().getConfigs()) {
                try {

                    fetch = config.getInt(secondsPath);

                } catch (Exception e) {
                    // Empty
                }
            }

            if (fetch != null) {
                seconds = fetch;
            }
        }
    }

    public void reload() {
    }
}