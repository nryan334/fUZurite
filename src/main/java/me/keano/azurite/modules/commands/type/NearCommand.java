package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.StaffManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class NearCommand extends Command {

    public NearCommand(CommandManager manager) {
        super(
                manager,
                "near"
        );
        this.setPermissible("azurite.near");
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("nearplayers");
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        StaffManager staffManager = getInstance().getStaffManager();
        List<String> nearby = new ArrayList<>();
        int radius = getLanguageConfig().getInt("NEAR_COMMAND.RADIUS");

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Player)) continue;

            Player nearbyPlayer = (Player) entity;

            if (staffManager.isVanished(nearbyPlayer)) continue;
            if (nearbyPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
            if (Bukkit.getPlayer(nearbyPlayer.getUniqueId()) == null) continue; // NPC

            nearby.add(nearbyPlayer.getName());
        }

        if (nearby.isEmpty()) {
            sendMessage(sender, getLanguageConfig().getString("NEAR_COMMAND.EMPTY"));
            return;
        }

        String formatted = String.join("§7, ", nearby.toArray(new String[0]));

        for (String s : getLanguageConfig().getStringList("NEAR_COMMAND.FORMAT")) {
            sendMessage(sender, s
                    .replace("%nearamount%", String.valueOf(nearby.size()))
                    .replace("%near%", formatted)
            );
        }
    }
}