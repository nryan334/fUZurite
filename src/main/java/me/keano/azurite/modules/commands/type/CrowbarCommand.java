package me.keano.azurite.modules.commands.type;

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
public class CrowbarCommand extends Command {

    public CrowbarCommand(CommandManager manager) {
        super(manager,
                "crowbar"
        );
        this.setPermissible("azurite.crowbar");
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
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, Config.PLAYER_ONLY);
                return;
            }

            Player player = (Player) sender;
            player.getInventory().addItem(Config.CROWBAR.clone());
            player.updateInventory();
            sendMessage(sender, getLanguageConfig().getString("CROWBAR_COMMAND.RECEIVED"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        target.getInventory().addItem(Config.CROWBAR.clone());
        target.updateInventory();
        sendMessage(target, getLanguageConfig().getString("CROWBAR_COMMAND.RECEIVED"));
    }
}