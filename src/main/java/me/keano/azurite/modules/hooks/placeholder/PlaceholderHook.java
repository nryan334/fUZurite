package me.keano.azurite.modules.hooks.placeholder;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.hooks.placeholder.type.NonePlaceholderHook;
import me.keano.azurite.modules.hooks.placeholder.type.PlaceholderAPIHook;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PlaceholderHook extends Manager implements Placeholder {

    private Placeholder placeholder;

    public PlaceholderHook(HCF instance) {
        super(instance);
        this.load();
    }

    private void load() {
        if (Utils.verifyPlugin("PlaceholderAPI", getInstance())) {
            placeholder = new PlaceholderAPIHook(this);

        } else {
            placeholder = new NonePlaceholderHook(this);
        }
    }

    @Override
    public String replace(Player player, String string) {
        return placeholder.replace(player, string);
    }

    @Override
    public List<String> replace(Player player, List<String> list) {
        return placeholder.replace(player, list);
    }
}