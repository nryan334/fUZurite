package me.keano.azurite.modules.framework;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.extra.Configs;
import me.keano.azurite.modules.nametags.Nametag;
import me.keano.azurite.modules.tablist.Tablist;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.users.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public abstract class Module<T extends Manager> extends Configs implements Listener {

    private final HCF instance;
    private final T manager;

    public Module(T manager) {
        this.instance = manager.getInstance();
        this.manager = manager;
        this.checkListener();
    }

    private void checkListener() {
        // Improve load times
        if (this instanceof User) return;
        if (this instanceof Team) return;
        if (this instanceof Tablist) return;
        if (this instanceof Nametag) return;

        for (Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                manager.registerListener(this);
                break; // Break the loop, we already know it's a listener now.
            }
        }
    }
}