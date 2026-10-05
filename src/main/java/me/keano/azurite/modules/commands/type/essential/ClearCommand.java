package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ClearCommand extends Command {

    public ClearCommand(CommandManager manager) {
        super(
                manager,
                "clear"
        );
        this.setPermissible("azurite.clear");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "clearinventory",
                "clearinven",
                "ci"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("CLEAR_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.updateInventory();
                sendMessage(sender, getLanguageConfig().getString("CLEAR_COMMAND.CLEARED"));
                return;
            }

            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        target.getInventory().clear();
        target.getInventory().setArmorContents(null);
        target.updateInventory();

        sendMessage(target, getLanguageConfig().getString("CLEAR_COMMAND.CLEARED"));
        sendMessage(sender, getLanguageConfig().getString("CLEAR_COMMAND.CLEARED_TARGET")
                .replace("%player%", target.getName())
        );
    }
}