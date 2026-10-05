package me.keano.azurite.modules.commands.type.essential;

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
public class IgnoreCommand extends Command {

    public IgnoreCommand(CommandManager manager) {
        super(
                manager,
                "ignore"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("IGNORE_COMMAND.USAGE");
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
        User target = getInstance().getUserManager().getByName(args[0]);
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (user.getIgnoring().remove(target.getUniqueID())) {
            user.save();
            sendMessage(sender, getLanguageConfig().getString("IGNORE_COMMAND.IGNORE_REMOVE")
                    .replace("%player%", target.getName())
            );
            return;
        }

        user.getIgnoring().add(target.getUniqueID());
        user.save();
        sendMessage(sender, getLanguageConfig().getString("IGNORE_COMMAND.IGNORE_ADD")
                .replace("%player%", target.getName())
        );
    }
}