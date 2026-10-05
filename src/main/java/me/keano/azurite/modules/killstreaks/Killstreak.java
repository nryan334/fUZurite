package me.keano.azurite.modules.killstreaks;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class Killstreak extends Module<KillstreakManager> {

    private final String name;
    private final List<String> commands;
    private final List<PotionEffect> effects;
    private final List<ItemStack> items;

    public Killstreak(KillstreakManager manager, String name, List<String> commands, List<PotionEffect> effects, List<ItemStack> items) {
        super(manager);
        this.name = name;
        this.commands = commands;
        this.effects = effects;
        this.items = items;
    }

    public void handle(Player player) {
        if (commands != null) {
            for (String command : commands)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                        .replace("%player%", player.getName())
                );
        }

        if (effects != null) {
            for (PotionEffect effect : effects) getInstance().getClassManager().addEffect(player, effect);
        }

        if (items != null) {
            for (ItemStack item : items) ItemUtils.giveItem(player, item, player.getLocation());
        }
    }
}