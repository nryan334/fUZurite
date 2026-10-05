package me.keano.azurite.modules.teams.player;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Member {

    private UUID uniqueID;
    private Role role;

    public Member(UUID uniqueID, Role role) {
        this.uniqueID = uniqueID;
        this.role = role;
    }

    // We can't use a variable otherwise it won't change everytime we update the role.
    public String getAsterisk() {
        return (role == Role.LEADER ? "***" : role == Role.CO_LEADER ? "**" : role == Role.CAPTAIN ? "*" : "");
    }
}