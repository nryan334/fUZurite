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
public class EcoManageCommand extends Command {

    public EcoManageCommand(CommandManager manager) {
        super(
                manager,
                "ecomanage"
        );
        this.completions.add(new TabCompletion(Arrays.asList("set", "add", "give", "plus", "remove", "take"), 0));
        this.setPermissible("azurite.ecomanage");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "ecomanager",
                "balmanager"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("ECOMANAGE_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }

        User target = getInstance().getUserManager().getByName(args[1]);
        Integer amount = getInt(args[2]);

        if (target == null) {
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
                target.setBalance(target.getBalance() - amount);
                sendMessage(sender, getLanguageConfig().getString("ECOMANAGE_COMMAND.REMOVED_BAL")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", target.getName())
                );
                return;

            case "give":
            case "plus":
            case "add":
                target.setBalance(target.getBalance() + amount);
                target.save();
                sendMessage(sender, getLanguageConfig().getString("ECOMANAGE_COMMAND.ADDED_BAL")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", target.getName())
                );
                return;

            case "set":
                target.setBalance(amount);
                target.save();
                sendMessage(sender, getLanguageConfig().getString("ECOMANAGE_COMMAND.SET_BAL")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", target.getName())
                );
                return;
        }

        sendUsage(sender);
    }
}