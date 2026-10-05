package me.keano.azurite.modules.hooks.placeholder.type;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.hooks.placeholder.Placeholder;
import me.keano.azurite.modules.hooks.placeholder.PlaceholderHook;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class NonePlaceholderHook extends Module<PlaceholderHook> implements Placeholder {

    public NonePlaceholderHook(PlaceholderHook manager) {
        super(manager);
    }

    @Override
    public String replace(Player player, String string) {
        return string;
    }

    @Override
    public List<String> replace(Player player, List<String> list) {
        return list;
    }
}