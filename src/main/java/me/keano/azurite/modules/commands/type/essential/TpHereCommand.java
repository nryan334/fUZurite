package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TpHereCommand extends Command {

    public TpHereCommand(CommandManager manager) {
        super(
                manager,
                "tphere"
        );
        this.setPermissible("azurite.teleporthere");
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "s"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("TELEPORT_COMMAND.TPHERE_COMMAND.USAGE");
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
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        target.teleport(player);
        player.sendMessage(getLanguageConfig().getString("TELEPORT_COMMAND.TPHERE_COMMAND.TELEPORTED")
                .replace("%player%", target.getName())
        );
    }
}