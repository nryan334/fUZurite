package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.users.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LivesManageCommand extends Command {

    public LivesManageCommand(CommandManager manager) {
        super(
                manager,
                "livesmanage"
        );
        this.setPermissible("azurite.livesmanage");
        this.completions.add(new TabCompletion(Arrays.asList("set", "add", "plus", "remove", "take"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "livemanage"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("LIVESMANAGE_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        Integer amount = getInt(args[2]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[1])
            );
            return;
        }

        if (amount == null || amount <= 0) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[2])
            );
            return;
        }

        User user = getInstance().getUserManager().getByUUID(target.getUniqueId());

        switch (args[0].toLowerCase()) {
            case "take":
            case "remove":
                user.setLives(user.getLives() - amount);
                sendMessage(sender, getLanguageConfig().getString("LIVESMANAGE_COMMAND.REMOVED_LIVES")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", target.getName())
                );
                return;

            case "plus":
            case "give":
            case "add":
                user.setLives(user.getLives() + amount);
                sendMessage(sender, getLanguageConfig().getString("LIVESMANAGE_COMMAND.ADDED_LIVES")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", target.getName())
                );
                return;

            case "set":
                user.setLives(amount);
                sendMessage(sender, getLanguageConfig().getString("LIVESMANAGE_COMMAND.SET_LIVES")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%target%", target.getName())
                );
                return;
        }

        sendUsage(sender);
    }
}