package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CopyInvCommand extends Command {

    public CopyInvCommand(CommandManager manager) {
        super(
                manager,
                "copyinv"
        );
        this.setPermissible("azurite.copyinv");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("COPYINV_COMMAND.USAGE");
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

        PlayerInventory playerInventory = player.getInventory();
        PlayerInventory targetInventory = target.getInventory();
        playerInventory.setContents(targetInventory.getContents());
        playerInventory.setArmorContents(targetInventory.getArmorContents());
        sendMessage(sender, getLanguageConfig().getString("COPYINV_COMMAND.COPIED")
                .replace("%player%", target.getName())
        );
    }
}