package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class NetherPlayersCommand extends Command {

    public NetherPlayersCommand(CommandManager manager) {
        super(
                manager,
                "netherplayers"
        );
        this.setPermissible("azurite.netherplayers");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int netherPlayers = Bukkit.getWorld("world_nether").getPlayers().size();

        for (String s : getLanguageConfig().getStringList("NETHERPLAYERS_COMMAND.FORMAT")) {
            sendMessage(sender, s
                    .replace("%netherplayers%", String.valueOf(netherPlayers))
            );
        }
    }
}