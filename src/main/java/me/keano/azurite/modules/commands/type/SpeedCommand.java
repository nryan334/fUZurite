package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.Serializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SpeedCommand extends Command {

    public SpeedCommand(CommandManager manager) {
        super(
                manager,
                "speed"
        );
        this.setPermissible("azurite.speed");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
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

        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.removePotionEffect(PotionEffectType.SPEED);
            sendMessage(sender, getLanguageConfig().getString("SPEED_COMMAND.TOGGLED_OFF"));
            return;
        }

        getInstance().getClassManager().addEffect(player, Serializer.getEffect("SPEED, MAX_VALUE, 2"));
        sendMessage(sender, getLanguageConfig().getString("SPEED_COMMAND.TOGGLED_ON"));
    }
}