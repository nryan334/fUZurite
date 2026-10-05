package me.keano.azurite.modules.teams.extra;

import lombok.Getter;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class ManageTeamData {

    private final ManageTeamType manageTeamType;
    private final UUID team;

    public ManageTeamData(ManageTeamType manageTeamType, UUID team) {
        this.manageTeamType = manageTeamType;
        this.team = team;
    }

    public enum ManageTeamType {

        REGEN,
        DTR,
        BALANCE,
        POINTS,
        RENAME

    }
}