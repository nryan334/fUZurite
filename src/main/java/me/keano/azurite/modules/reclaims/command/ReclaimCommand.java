package me.keano.azurite.modules.reclaims.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.reclaims.Reclaim;
import me.keano.azurite.modules.users.User;
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
public class ReclaimCommand extends Command {

    public ReclaimCommand(CommandManager manager) {
        super(
                manager,
                "reclaim"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("claim");
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
        Reclaim reclaim = getInstance().getReclaimManager().getReclaim(player);
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (reclaim == null) {
            sendMessage(sender, getLanguageConfig().getString("RECLAIM_COMMAND.NO_RECLAIM"));
            return;
        }

        if (user.isReclaimed()) {
            sendMessage(sender, getLanguageConfig().getString("RECLAIM_COMMAND.ALREADY_RECLAIMED"));
            return;
        }

        for (String command : reclaim.getCommands()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command
                    .replace("%player%", player.getName())
            );
        }

        user.setReclaimed(true);
        user.save();
    }
}