package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TpLocCommand extends Command {

    public TpLocCommand(CommandManager manager) {
        super(
                manager,
                "tploc"
        );
        this.setPermissible("azurite.tploc");
    }


    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "tppos"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("TELEPORT_COMMAND.TPLOC_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        Integer x = getInt(args[0]);
        Integer y = getInt(args[1]);
        Integer z = getInt(args[2]);

        if (x == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[0])
            );
            return;
        }

        if (y == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[1])
            );
            return;
        }

        if (z == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[2])
            );
            return;
        }

        player.teleport(new Location(player.getWorld(), x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch()));

        sendMessage(sender, getLanguageConfig().getString("TELEPORT_COMMAND.TPLOC_COMMAND.TELEPORTED")
                .replace("%x%", String.valueOf(x))
                .replace("%y%", String.valueOf(y))
                .replace("%z%", String.valueOf(z))
        );
    }
}