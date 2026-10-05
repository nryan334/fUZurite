package me.keano.azurite.modules.users.settings;

import lombok.Getter;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public enum TeamListSetting {

    ONLINE_LOW("TEAM_COMMAND.TEAM_SORT.ONLINE_LOW"),
    ONLINE_HIGH("TEAM_COMMAND.TEAM_SORT.ONLINE_HIGH"),
    LOWEST_DTR("TEAM_COMMAND.TEAM_SORT.LOWEST_DTR"),
    HIGHEST_DTR("TEAM_COMMAND.TEAM_SORT.HIGHEST_DTR");

    private final String configPath;

    TeamListSetting(String configPath) {
        this.configPath = configPath;
    }
}