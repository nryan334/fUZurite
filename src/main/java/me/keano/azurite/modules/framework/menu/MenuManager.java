package me.keano.azurite.modules.framework.menu;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.menu.listener.MenuListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class MenuManager extends Manager {

    private final Map<UUID, Menu> menus;

    public MenuManager(HCF instance) {
        super(instance);
        this.menus = new HashMap<>();
        new MenuListener(this);
    }
}