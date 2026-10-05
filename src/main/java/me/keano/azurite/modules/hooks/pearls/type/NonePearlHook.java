package me.keano.azurite.modules.hooks.pearls.type;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.hooks.pearls.Pearl;
import me.keano.azurite.modules.hooks.pearls.PearlHook;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class NonePearlHook extends Module<PearlHook> implements Pearl {

    public NonePearlHook(PearlHook manager) {
        super(manager);
    }

    @Override
    public void loadHook() {
        // Empty
    }
}