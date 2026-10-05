package me.keano.azurite.modules.walls;

import lombok.Getter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.awt.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public enum WallType {

    PVP_TIMER(Config.PVP_TIMER_WALL_MATERIAL, Config.PVP_TIMER_WALL_COLOR, Config.PVP_TIMER_WALL_DATA),
    COMBAT_TAG(Config.COMBAT_TAG_WALL_MATERIAL, Config.COMBAT_TAG_WALL_COLOR, Config.COMBAT_TAG_WALL_DATA),
    INVINCIBILITY(Config.INVINCIBILITY_WALL_MATERIAL, Config.INVINCIBILITY_WALL_COLOR, Config.INVINCIBILITY_WALL_DATA),
    EVENT_DENIED(Config.EVENT_DENIED_WALL_MATERIAL, Config.EVENT_DENIED_WALL_COLOR, Config.EVENT_DENIED_WALL_DATA),
    CONQUEST_DENIED(Config.CONQUEST_DENIED_WALL_MATERIAL, Config.CONQUEST_DENIED_WALL_COLOR, Config.CONQUEST_DENIED_WALL_DATA),
    CITADEL_DENIED(Config.CITADEL_DENIED_WALL_MATERIAL, Config.CITADEL_DENIED_WALL_COLOR, Config.CITADEL_DENIED_WALL_DATA),
    LOCKED_CLAIM(Config.LOCKED_CLAIM_WALL_MATERIAL, Config.LOCKED_CLAIM_WALL_COLOR, Config.LOCKED_CLAIM_WALL_DATA);

    private final Material material;
    private final Color lunarColor;
    private final byte data;

    WallType(Material material, Color lunarColor, byte data) {
        this.material = material;
        this.lunarColor = lunarColor;
        this.data = data;
    }

    public boolean shouldLimit(Player player, Team team) {
        TeamType type = null;

        switch (this) {
            case EVENT_DENIED:
                type = TeamType.EVENT;
                break;

            case CONQUEST_DENIED:
                type = TeamType.CONQUEST;
                break;

            case CITADEL_DENIED:
                type = TeamType.CITADEL;
                break;
        }

        if (type != null) {
            if (getEntryLimit() <= 0 || player.hasPermission(getEntryPermission()) || team.getType() != type) {
                return false;
            }

            PlayerTeam pt = team.getManager().getByPlayer(player.getUniqueId());
            return pt == null || pt.getOnlinePlayersSize(false) < getEntryLimit();
        }

        return false;
    }

    public boolean isEntryLimited() {
        return this == WallType.EVENT_DENIED || this == WallType.CITADEL_DENIED || this == WallType.CONQUEST_DENIED;
    }

    public int getEntryLimit() {
        switch (this) {
            case EVENT_DENIED:
                return Config.TEAM_EVENT_ENTER_LIMIT;

            case CONQUEST_DENIED:
                return Config.TEAM_CONQUEST_ENTER_LIMIT;

            case CITADEL_DENIED:
                return Config.TEAM_CITADEL_ENTER_LIMIT;

            default:
                return 0;
        }
    }

    public String getEntryPermission() {
        switch (this) {
            case EVENT_DENIED:
                return "azurite.event.entry";

            case CITADEL_DENIED:
                return "azurite.citadel.entry";

            case CONQUEST_DENIED:
                return "azurite.conquest.entry";

            default:
                return null;
        }
    }

    public String getConfigPath() {
        switch (this) {
            case EVENT_DENIED:
                return "EVENT";

            case CONQUEST_DENIED:
                return "CONQUEST";

            case CITADEL_DENIED:
                return "CITADEL";

            default:
                return null;
        }
    }
}