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
public class EChestCommand extends Command {

    public EChestCommand(CommandManager manager) {
        super(
                manager,
                "echest"
        );
        this.setPermissible("azurite.echest");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "ec",
                "openechest"
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

        if (!sender.hasPermission("azurite.echest")) {
            sendMessage(sender, Config.INSUFFICIENT_PERM);
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.openInventory(player.getEnderChest());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (!sender.hasPermission("azurite.echest.other")) {
            sendMessage(sender, Config.INSUFFICIENT_PERM);
            return;
        }

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        player.openInventory(target.getEnderChest());
    }
}