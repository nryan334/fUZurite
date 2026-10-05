package me.keano.azurite.modules.framework.extra;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public enum TitlesConfig {

    WELCOME_BACK,
    JOIN_NO_TEAM,
    JOIN_NO_CLAIMS,
    TEAM_CREATE,
    STAFF_ENABLED,
    STAFF_DISABLED;

    private boolean enabled;
    private String title;
    private String subtitle;

    public void load(Configs configs) {
        String path = "PLAYER_TITLES." + name() + ".";
        this.enabled = configs.getConfig().getBoolean(path + "ENABLED");
        this.title = configs.getConfig().getString(path + "TITLE");
        this.subtitle = configs.getConfig().getString(path + "SUBTITLE");
    }

    public void sendTitle(Player player) {
        this.sendTitle(player, UnaryOperator.identity());
    }

    public void sendTitle(Player player, UnaryOperator<String> replacer) {
        player.sendTitle(replacer.apply(title), replacer.apply(subtitle));
    }
}