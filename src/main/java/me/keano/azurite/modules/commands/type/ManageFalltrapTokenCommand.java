package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ManageFalltrapTokenCommand extends Command {

    public ManageFalltrapTokenCommand(CommandManager manager) {
        super(
                manager,
                "managefalltraptoken"
        );
        this.completions.add(new TabCompletion(Arrays.asList("set", "add", "plus", "remove", "take"), 0));
        this.setPermissible("azurite.managefalltraptoken");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "adminfalltrap",
                "adminftokens"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("FALLTRAP_TOKEN_MANAGE_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }

        User targetUser = getInstance().getUserManager().getByName(args[1]);
        Integer amount = getInt(args[2]);

        if (targetUser == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[1])
            );
            return;
        }

        if (amount == null || amount < 0) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[2])
            );
            return;
        }

        switch (args[0].toLowerCase()) {
            case "take":
            case "remove":
                targetUser.setFalltrapTokens(targetUser.getFalltrapTokens() - amount);
                sendMessage(sender, getLanguageConfig().getString("FALLTRAP_TOKEN_MANAGE_COMMAND.REMOVED_BAL")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", targetUser.getName())
                );
                return;

            case "plus":
            case "add":
                targetUser.setFalltrapTokens(targetUser.getFalltrapTokens() + amount);
                sendMessage(sender, getLanguageConfig().getString("FALLTRAP_TOKEN_MANAGE_COMMAND.ADDED_BAL")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", targetUser.getName())
                );
                return;

            case "set":
                targetUser.setFalltrapTokens(amount);
                sendMessage(sender, getLanguageConfig().getString("FALLTRAP_TOKEN_MANAGE_COMMAND.SET_BAL")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", targetUser.getName())
                );
                return;
        }

        sendUsage(sender);
    }
}