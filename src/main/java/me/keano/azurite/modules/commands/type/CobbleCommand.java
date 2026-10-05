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
public class CobbleCommand extends Command {

    public CobbleCommand(CommandManager manager) {
        super(
                manager,
                "cobble"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "cobblestone"
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

        if (user.isCobblePickup()) {
            user.setCobblePickup(false);
            user.save();
            sendMessage(sender, getLanguageConfig().getString("COBBLE_COMMAND.TOGGLED_OFF"));

        } else {
            user.setCobblePickup(true);
            user.save();
            sendMessage(sender, getLanguageConfig().getString("COBBLE_COMMAND.TOGGLED_ON"));
        }
    }
}