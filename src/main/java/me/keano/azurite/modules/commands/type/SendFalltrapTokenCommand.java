package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
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
public class SendFalltrapTokenCommand extends Command {

    public SendFalltrapTokenCommand(CommandManager manager) {
        super(
                manager,
                "sendfalltraptoken"
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "sendfttoken",
                "sendtraptoken"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("SEND_FALLTRAP_TOKEN_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        Integer paying = getInt(args[1]);
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (paying == null || paying <= 0) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[1])
            );
            return;
        }

        if (user.getFalltrapTokens() < paying) {
            sendMessage(sender, getLanguageConfig().getString("SEND_FALLTRAP_TOKEN_COMMAND.INSUFFICIENT_BAL")
                    .replace("%amount%", String.valueOf(paying))
            );
            return;
        }

        User userTarget = getInstance().getUserManager().getByUUID(target.getUniqueId());

        user.setFalltrapTokens(user.getFalltrapTokens() - paying);
        user.save();

        userTarget.setFalltrapTokens(userTarget.getFalltrapTokens() + paying);
        userTarget.save();

        sendMessage(target, getLanguageConfig().getString("SEND_FALLTRAP_TOKEN_COMMAND.RECEIVED")
                .replace("%player%", player.getName())
                .replace("%amount%", String.valueOf(paying))
        );

        sendMessage(sender, getLanguageConfig().getString("SEND_FALLTRAP_TOKEN_COMMAND.PAID")
                .replace("%player%", target.getName())
                .replace("%amount%", String.valueOf(paying))
        );
    }
}