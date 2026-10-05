package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.waypoints.WaypointAzurite;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SetSpawnCommand extends Command {

    public SetSpawnCommand(CommandManager manager) {
        super(
                manager,
                "setspawn"
        );
        this.setPermissible("azurite.setspawn");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "setworldspawn",
                "setspawnpoint"
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

        Player player = (Player) sender;
        Location location = player.getLocation();

        switch (player.getWorld().getEnvironment()) {
            case NORMAL:
                handleWaypoint(location);
                getInstance().getWaypointManager().setWorldSpawn(location);
                getMiscConfig().set("OVERWORLD_SPAWN", Serializer.serializeLoc(location));
                getMiscConfig().save();
                sendMessage(sender, getLanguageConfig().getString("SETSPAWN_COMMAND.SET_SPAWN")
                        .replace("%world%", Utils.getWorldName(location.getWorld()))
                        .replace("%loc%", Utils.formatLocation(location))
                );
                return;

            case THE_END:
                handleWaypoint(location);
                getInstance().getWaypointManager().setEndSpawn(location);
                getMiscConfig().set("END_SPAWN", Serializer.serializeLoc(location));
                getMiscConfig().save();
                sendMessage(sender, getLanguageConfig().getString("SETSPAWN_COMMAND.SET_SPAWN")
                        .replace("%world%", Utils.getWorldName(location.getWorld()))
                        .replace("%loc%", Utils.formatLocation(location))
                );
                return;

            case NETHER:
                handleWaypoint(location);
                getInstance().getWaypointManager().setNetherSpawn(location);
                getMiscConfig().set("NETHER_SPAWN", Serializer.serializeLoc(location));
                getMiscConfig().save();
                sendMessage(sender, getLanguageConfig().getString("SETSPAWN_COMMAND.SET_SPAWN")
                        .replace("%world%", Utils.getWorldName(location.getWorld()))
                        .replace("%loc%", Utils.formatLocation(location))
                );
        }
    }

    private void handleWaypoint(Location location) {
        Location old = Serializer.fetchLocation(getMiscConfig().getString(Utils.getWorldName(location.getWorld()).toUpperCase() + "_SPAWN"));
        WaypointAzurite spawn = getInstance().getWaypointManager().getSpawnWaypoint();

        for (Player player : location.getWorld().getPlayers()) {
            spawn.remove(player, old, UnaryOperator.identity());
            spawn.send(player, location, UnaryOperator.identity());
        }
    }
}