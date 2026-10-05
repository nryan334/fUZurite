package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ResetRedeemCommand extends Command {

    public ResetRedeemCommand(CommandManager manager) {
        super(
                manager,
                "resetredeem"
        );
        this.setPermissible("azurite.resetredeem");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("RESET_REDEEM_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("ALL")) {
            int i = 0;

            for (User user : getInstance().getUserManager().getUsers().values()) {
                user.setRedeemed(false);
                i++;
            }

            getInstance().getStorageManager().getStorage().saveUsers();
            sendMessage(sender, getLanguageConfig().getString("RESET_REDEEM_COMMAND.RESET_ALL")
                    .replace("%users%", String.valueOf(i))
            );
            return;
        }

        User user = getInstance().getUserManager().getByName(args[0]);

        if (user == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (!user.isRedeemed()) {
            sendMessage(sender, getLanguageConfig().getString("RESET_REDEEM_COMMAND.NOT_REDEEMED"));
            return;
        }

        user.setRedeemed(false);
        user.save();
        sendMessage(sender, getLanguageConfig().getString("RESET_REDEEM_COMMAND.RESET_REDEEM")
                .replace("%player%", user.getName())
        );
    }
}