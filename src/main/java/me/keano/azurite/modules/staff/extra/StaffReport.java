package me.keano.azurite.modules.staff.extra;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.staff.StaffManager;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class StaffReport extends Module<StaffManager> {

    private final UUID player;
    private final UUID reported;
    private final String reason;

    public StaffReport(StaffManager manager, UUID player, UUID reported, String reason) {
        super(manager);
        this.player = player;
        this.reported = reported;
        this.reason = reason;
    }
}