package me.keano.azurite.modules.tablist.extra;

import lombok.Getter;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class TablistSkin {

    public static final Map<UUID, TablistSkin> SKIN_CACHE = new ConcurrentHashMap<>();

    private final String value;
    private final String signature;

    public TablistSkin(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TablistSkin skin = (TablistSkin) o;
        return Objects.equals(value, skin.value) && Objects.equals(signature, skin.signature);
    }

    public static TablistSkin getPlayerSkin(Manager manager, Player player) {
        TablistSkin cache = SKIN_CACHE.get(player.getUniqueId());

        if (cache != null) {
            return cache;
        }

        TablistSkin skin = manager.getInstance().getVersionManager().getVersion().getSkinData(player);

        if (skin != null) {
            SKIN_CACHE.put(player.getUniqueId(), skin);
            return skin;
        }

        return null;
    }
}