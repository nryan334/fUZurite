package me.keano.azurite.modules.hooks.pearls.type;

import eu.vortexgg.vortexpearls.VortexPearls;
import eu.vortexgg.vortexpearls.hook.Hook;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.hooks.pearls.Pearl;
import me.keano.azurite.modules.hooks.pearls.PearlHook;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class VortexPearlHook extends Module<PearlHook> implements Pearl {

    public VortexPearlHook(PearlHook manager) {
        super(manager);
    }

    @Override
    public void loadHook() {
        VortexPearls.getInstance().setHook(new Hook() {
            @Override
            public boolean isOnCooldown(Player p) {
                return getInstance().getTimerManager().getEnderpearlTimer().hasTimer(p);
            }
        });
    }
}