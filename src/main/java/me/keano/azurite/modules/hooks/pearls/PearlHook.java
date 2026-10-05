package me.keano.azurite.modules.hooks.pearls;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.hooks.pearls.type.NonePearlHook;
import me.keano.azurite.modules.hooks.pearls.type.VortexPearlHook;
import me.keano.azurite.utils.Utils;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PearlHook extends Manager implements Pearl {

    private Pearl pearl;

    public PearlHook(HCF instance) {
        super(instance);
        this.load();
        this.loadHook();
    }

    private void load() {
        if (Utils.verifyPlugin("VortexPearls", getInstance())) {
            pearl = new VortexPearlHook(this);

        } else {
            pearl = new NonePearlHook(this);
        }
    }

    @Override
    public void loadHook() {
        pearl.loadHook();
    }
}