package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class WorldCommand extends Command {

    public WorldCommand(CommandManager manager) {
        super(
                manager,
                "world"
        );
        this.setPermissible("azurite.world");
        this.completions.add(new TabCompletion(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()), 0));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("WORLD_COMMAND.USAGE");
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
        World world = Bukkit.getWorld(args[0]);

        if (world == null) {
            sendMessage(sender, getLanguageConfig().getString("WORLD_COMMAND.WORLD_NOT_FOUND")
                    .replace("%name%", args[0])
            );
            return;
        }

        World oldWorld = player.getLocation().clone().getWorld();

        switch (world.getEnvironment()) {
            case NORMAL:
                player.teleport(getInstance().getWaypointManager().getWorldSpawn());
                break;

            case NETHER:
                player.teleport(getInstance().getWaypointManager().getNetherSpawn());
                break;

            case THE_END:
                player.teleport(getInstance().getWaypointManager().getEndSpawn());
                break;
        }

        // We need to call this event.
        Bukkit.getPluginManager().callEvent(new PlayerChangedWorldEvent(player, oldWorld));

        sendMessage(sender, getLanguageConfig().getString("WORLD_COMMAND.WORLD_CHANGED")
                .replace("%name%", args[0])
        );
    }
}