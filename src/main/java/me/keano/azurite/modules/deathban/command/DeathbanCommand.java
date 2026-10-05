package me.keano.azurite.modules.deathban.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.deathban.Deathban;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.Utils;
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
public class DeathbanCommand extends Command {

    public DeathbanCommand(CommandManager manager) {
        super(
                manager,
                "deathban"
        );
        this.setPermissible("azurite.deathban");
        this.completions.add(new TabCompletion(Arrays.asList("setarenaspawn", "info", "remove"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("DEATHBAN_COMMAND.USAGE");
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

        switch (args[0].toLowerCase()) {
            case "setarenaspawn":
                getInstance().getDeathbanManager().setArenaSpawn(player.getLocation());
                getInstance().getDeathbanManager().save();
                sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.SET_ARENA_SPAWN"));
                return;

            case "info":
                if (args.length < 2) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.DEATHBAN_INFO.USAGE"));
                    return;
                }

                Player infoAbout = Bukkit.getPlayer(args[1]);

                if (infoAbout == null) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                Deathban deathban = getInstance().getDeathbanManager().getDeathban(infoAbout);

                if (deathban == null) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.NOT_DEATHBANNED")
                            .replace("%player%", infoAbout.getName())
                    );
                    return;
                }

                List<String> format = getLanguageConfig().getStringList("DEATHBAN_COMMAND.DEATHBAN_INFO.FORMAT");

                format.replaceAll(s1 -> s1
                        .replace("%player%", infoAbout.getName())
                        .replace("%date%", deathban.getDateFormatted())
                        .replace("%reason%", deathban.getReason())
                        .replace("%location%", Utils.formatLocation(deathban.getLocation()))
                );

                for (String form : format) {
                    sendMessage(sender, form);
                }
                return;

            case "remove":
                if (args.length < 2) {
                    sendMessage(sender, getLanguageConfig().getString(""));
                    return;
                }

                Player toRemove = Bukkit.getPlayer(args[1]);

                if (toRemove == null) {
                    sendMessage(sender, Config.PLAYER_NOT_FOUND
                            .replace("%player%", args[1])
                    );
                    return;
                }

                if (!getInstance().getDeathbanManager().isDeathbanned(toRemove)) {
                    sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.NOT_DEATHBANNED")
                            .replace("%player%", toRemove.getName())
                    );
                    return;
                }

                getInstance().getDeathbanManager().removeDeathban(toRemove);
                sendMessage(sender, getLanguageConfig().getString("DEATHBAN_COMMAND.REMOVED_DEATHBAN")
                        .replace("%player%", toRemove.getName())
                );
                return;
        }

        sendUsage(sender);
    }
}