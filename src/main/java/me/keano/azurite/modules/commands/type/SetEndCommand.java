package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.Serializer;
import org.bukkit.Location;
import org.bukkit.World;
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
public class SetEndCommand extends Command {

    public SetEndCommand(CommandManager manager) {
        super(
                manager,
                "setend"
        );
        this.setPermissible("azurite.setend");
        this.completions.add(new TabCompletion(Arrays.asList("exit", "worldexit"), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("SET_END_COMMAND.USAGE");
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
        Location location = player.getLocation().clone();

        switch (args[0].toLowerCase()) {
            case "exit":
                if (location.getWorld().getEnvironment() != World.Environment.THE_END) {
                    sendMessage(sender, getLanguageConfig().getString("SET_END_COMMAND.WRONG_WORLD")
                            .replace("%world%", World.Environment.THE_END.name())
                    );
                    return;
                }

                getInstance().getWaypointManager().setEndExit(location);
                getMiscConfig().set("END_EXIT", Serializer.serializeLoc(location));
                getMiscConfig().save();
                sendMessage(sender, getLanguageConfig().getString("SET_END_COMMAND.UPDATED"));
                return;

            case "worldexit":
                if (location.getWorld().getEnvironment() != World.Environment.NORMAL) {
                    sendMessage(sender, getLanguageConfig().getString("SET_END_COMMAND.WRONG_WORLD")
                            .replace("%world%", World.Environment.NORMAL.name())
                    );
                    return;
                }

                getInstance().getWaypointManager().setEndWorldExit(location);
                getMiscConfig().set("WORLD_EXIT", Serializer.serializeLoc(location));
                getMiscConfig().save();
                sendMessage(sender, getLanguageConfig().getString("SET_END_COMMAND.UPDATED"));
                return;
        }

        sendUsage(sender);
    }
}