package me.keano.azurite.modules.board.extra;

import lombok.Getter;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.Module;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class AnimatedString extends Module<Manager> {

    private final List<String> changes;
    private final long delay;

    private String current;
    private long ticks;
    private int index;

    public AnimatedString(Manager manager, List<String> changes, long delay) {
        super(manager);
        this.changes = changes;
        this.delay = delay;
        this.current = "";
        this.ticks = System.currentTimeMillis();
        this.index = 0;
    }

    public void tick() {
        if (ticks < System.currentTimeMillis()) {
            ticks = System.currentTimeMillis() + delay;

            if (index == changes.size()) {
                index = 0; // Reset the index
                current = changes.get(0);
                return;
            }

            String nextTitle = changes.get(index);
            index++;
            current = nextTitle;
        }
    }
}