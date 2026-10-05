package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BaseTokenCommand extends Command {

    public BaseTokenCommand(CommandManager manager) {
        super(
                manager,
                "basetoken"
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "btoken",
                "base"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("BASE_TOKEN_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
                sendMessage(sender, getLanguageConfig().getString("BASE_TOKEN_COMMAND.SELF_CHECK")
                        .replace("%balance%", String.valueOf(user.getBaseTokens()))
                );
                return;
            }

            sendUsage(sender);
            return;
        }

        User user = getInstance().getUserManager().getByName(args[0]);

        if (user == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        int targetBalance = user.getBaseTokens();
        sendMessage(sender, getLanguageConfig().getString("BASE_TOKEN_COMMAND.TARGET_CHECK")
                .replace("%target%", user.getName())
                .replace("%balance%", String.valueOf(targetBalance))
        );
    }
}