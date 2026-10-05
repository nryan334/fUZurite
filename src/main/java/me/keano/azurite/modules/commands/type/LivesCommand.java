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
public class LivesCommand extends Command {

    public LivesCommand(CommandManager manager) {
        super(
                manager,
                "lives"
        );
        this.completions.add(new TabCompletion(Arrays.asList("revive", "send", "give", "check"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "live"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("LIVES_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (args.length == 0) {
            sendMessage(sender, getLanguageConfig().getString("LIVES_COMMAND.SELF_CHECK")
                    .replace("%lives%", String.valueOf(user.getLives()))
            );
            return;
        }

        switch (args[0].toLowerCase()) {
            case "revive":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Player toRevive = Bukkit.getPlayer(args[1]);

                if (toRevive == null) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                if (!getInstance().getDeathbanManager().isDeathbanned(toRevive)) {
                    sendMessage(sender, getLanguageConfig().getString("LIVES_COMMAND.REVIVE_NOT_DEATHBANNED"));
                    return;
                }

                if (user.getLives() <= 0) {
                    sendMessage(sender, getLanguageConfig().getString("LIVES_COMMAND.REVIVE_NO_LIVES"));
                    return;
                }

                user.setLives(user.getLives() - 1); // decrement player lives
                user.save();

                getInstance().getDeathbanManager().removeDeathban(toRevive);
                sendMessage(sender, getLanguageConfig().getString("LIVES_COMMAND.REVIVED")
                        .replace("%player%", toRevive.getName())
                );
                return;

            case "check":
                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Player toCheck = Bukkit.getPlayer(args[1]);

                if (toCheck == null) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                User toCheckUser = getInstance().getUserManager().getByUUID(toCheck.getUniqueId());

                sendMessage(sender, getLanguageConfig().getString("LIVES_COMMAND.OTHER_CHECK")
                        .replace("%player%", toCheck.getName())
                        .replace("%lives%", String.valueOf(toCheckUser.getLives()))
                );
                return;

            case "send":
            case "give":
                if (args.length < 3) {
                    sendUsage(sender);
                    return;
                }

                Player toGive = Bukkit.getPlayer(args[1]);
                Integer amount = getInt(args[2]);

                if (toGive == null) {
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

                if (user.getLives() < amount) {
                    sendMessage(sender, getLanguageConfig().getString("LIVES_COMMAND.INSUFFICIENT_LIVES")
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%lives%", String.valueOf(user.getLives()))
                    );
                    return;
                }

                User toGiveUser = getInstance().getUserManager().getByUUID(toGive.getUniqueId());

                user.setLives(user.getLives() - amount);
                user.save();

                toGiveUser.setLives(toGiveUser.getLives() + amount);
                toGiveUser.save();

                sendMessage(sender, getLanguageConfig().getString("LIVES_COMMAND.GAVE_LIVES")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%player%", toGive.getName())
                );

                sendMessage(toGive, getLanguageConfig().getString("LIVES_COMMAND.RECEIVED_LIVES")
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%player%", player.getName())
                );
                return;
        }

        sendUsage(sender);
    }
}