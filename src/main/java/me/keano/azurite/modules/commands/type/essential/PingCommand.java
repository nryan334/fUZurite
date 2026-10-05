package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PingCommand extends Command {

    public PingCommand(CommandManager manager) {
        super(
                manager,
                "ping"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "ms"
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

        if (args.length == 0) {
            Player player = (Player) sender;
            int selfPing = getInstance().getVersionManager().getVersion().getPing(player);
            sendMessage(sender, getLanguageConfig().getString("PING_COMMAND.SELF_PING")
                    .replace("%color%", CC.t(getColor(selfPing)))
                    .replace("%ping%", selfPing + "ms")
            );
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        int otherPing = getInstance().getVersionManager().getVersion().getPing(target);
        sendMessage(sender, getLanguageConfig().getString("PING_COMMAND.OTHER_PING")
                .replace("%color%", CC.t(getColor(otherPing)))
                .replace("%player%", target.getName())
                .replace("%ping%", otherPing + "ms")
        );
    }

    public String getColor(int ping) {
        if (ping > 100 && ping < 160) {
            return "&e";

        } else if (ping > 160) {
            return "&c";

        } else {
            return "&a";
        }
    }
}