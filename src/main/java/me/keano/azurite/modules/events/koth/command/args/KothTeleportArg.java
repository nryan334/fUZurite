package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothTeleportArg extends Argument {

    public KothTeleportArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "teleport",
                        "tp",
                        "to"
                )
        );
        this.setPermissible("azurite.koth.teleport");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_TELEPORT.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        Koth koth = getInstance().getKothManager().getKoth(args[0]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        if (koth.getCaptureZone() == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_TELEPORT.NO_CAP_ZONE"));
            return;
        }

        player.teleport(koth.getCaptureZone().getCenter());
        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_TELEPORT.TELEPORTED")
                .replace("%color%", koth.getColor())
                .replace("%koth%", koth.getName())
        );
    }
}