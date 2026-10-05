package me.keano.azurite.modules.pvpclass.type.ghost;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class GhostData extends Module<PvPClassManager> {

    private String mode;
    private List<BukkitTask> tasks;
    private int counter;

    public GhostData(PvPClassManager manager) {
        super(manager);
        this.mode = getClassesConfig().getString("GHOST_CLASS.DEFAULT_MODE");
        this.tasks = new ArrayList<>();
        this.counter = 0;
    }
}