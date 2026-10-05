package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TogglePMCommand extends Command {

    public TogglePMCommand(CommandManager manager) {
        super(
                manager,
                "togglepm"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "tpm"
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

        Player player = (Player) sender;
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        String path = "TOGGLEPM_COMMAND.PM_" + (user.isPrivateMessages() ? "FALSE" : "TRUE");

        user.setPrivateMessages(!user.isPrivateMessages());
        sendMessage(sender, getLanguageConfig().getString(path));
    }
}