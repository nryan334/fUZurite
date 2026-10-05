package me.keano.azurite.modules.teams.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.enums.TeamType;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class WildernessTeam extends Team {

    public WildernessTeam(TeamManager manager) {
        super(
                manager,
                "Wilderness",
                UUID.randomUUID(),
                true,
                TeamType.WILDERNESS
        );
    }

    @Override
    public String getDisplayName(Player player) {
        return Config.DISPLAY_NAME_WILDERNESS.replace("%team%", super.getDisplayName(player));
    }
}