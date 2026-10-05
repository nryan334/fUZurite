package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TpAllCommand extends Command {

    public TpAllCommand(CommandManager manager) {
        super(
                manager,
                "tpall"
        );
        this.setPermissible("azurite.tpall");
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

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (players.size() == 1) {
            sendMessage(sender, getLanguageConfig().getString("TELEPORT_COMMAND.TPALL_COMMAND.INSUFFICIENT_PLAYERS"));
            return;
        }

        Player player = (Player) sender;
        // Keep this outside the for loop
        String message = getLanguageConfig().getString("TELEPORT_COMMAND.TPALL_COMMAND.TELEPORTED")
                .replace("%player%", player.getName());

        for (Player onlinePlayer : players) {
            if (onlinePlayer == player) continue; // We don't want this

            onlinePlayer.teleport(player);
            onlinePlayer.sendMessage(message);
        }
    }
}