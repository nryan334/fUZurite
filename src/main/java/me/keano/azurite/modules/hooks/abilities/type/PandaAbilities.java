package me.keano.azurite.modules.hooks.abilities.type;

import dev.panda.ability.PandaAbilityAPI;
import dev.panda.ability.abilities.Ability;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.hooks.abilities.Abilities;
import me.keano.azurite.modules.hooks.abilities.AbilitiesHook;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PandaAbilities extends Module<AbilitiesHook> implements Abilities {

    private final PandaAbilityAPI api;

    public PandaAbilities(AbilitiesHook manager) {
        super(manager);
        this.api = new PandaAbilityAPI();
    }

    @Override
    public List<String> getScoreboardLines(Player player) {
        String name = getScoreboardConfig().getString("PLAYER_TIMERS.ABILITIES");
        if (name.isEmpty()) return Collections.emptyList();

        List<String> list = new ArrayList<>();

        for (Ability ability : api.getActiveAbility(player)) {
            list.add(name.replace("%ability%", ability.getName()) + ability.getCooldown(player));
        }

        return list;
    }

    @Override
    public String getRemainingGlobal(Player player) {
        return api.getGlobalCooldown().getGlobalCooldown(player);
    }

    @Override
    public boolean hasGlobalCooldown(Player player) {
        return api.getGlobalCooldown().hasGlobalCooldown(player);
    }
}