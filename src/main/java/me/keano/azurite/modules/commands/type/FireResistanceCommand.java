package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.Serializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class FireResistanceCommand extends Command {

    public FireResistanceCommand(CommandManager manager) {
        super(
                manager,
                "fireresistance"
        );
        this.setPermissible("azurite.fireres");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "fres",
                "fireres"
        );
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

        if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            sendMessage(sender, getLanguageConfig().getString("FIRE_RESISTANCE_COMMAND.TOGGLED_OFF"));
            return;
        }

        getInstance().getClassManager().addEffect(player, Serializer.getEffect("FIRE_RESISTANCE, MAX_VALUE, 1"));
        sendMessage(sender, getLanguageConfig().getString("FIRE_RESISTANCE_COMMAND.TOGGLED_ON"));
    }
}