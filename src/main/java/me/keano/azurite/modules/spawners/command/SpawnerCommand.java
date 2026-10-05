package me.keano.azurite.modules.spawners.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.spawners.Spawner;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SpawnerCommand extends Command {

    public SpawnerCommand(CommandManager manager) {
        super(
                manager,
                "spawner"
        );
        this.setPermissible("azurite.spawner");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("SPAWNER_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(permissible)) {
            sendMessage(sender, Config.INSUFFICIENT_PERM);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        EntityType type;

        try {

            type = EntityType.valueOf(args[0].toUpperCase());

        } catch (IllegalArgumentException e) {
            type = null;
        }

        if (type == null || getInstance().getSpawnerManager().getSpawners().get(type) == null) {
            sendMessage(sender, getLanguageConfig().getString("SPAWNER_COMMAND.SPAWNER_NOT_FOUND")
                    .replace("%type%", args[0])
            );
            return;
        }

        Spawner spawner = getInstance().getSpawnerManager().getSpawners().get(type);

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, Config.PLAYER_ONLY);
                return;
            }

            Player player = (Player) sender;
            player.getInventory().addItem(spawner.getItemStack());
            player.updateInventory();
            player.sendMessage(getLanguageConfig().getString("SPAWNER_COMMAND.SPAWNER_GAINED")
                    .replace("%type%", spawner.getName())
            );
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[1])
            );
            return;
        }

        target.getInventory().addItem(spawner.getItemStack());
        target.updateInventory();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getSpawnerManager().getSpawners().keySet()
                    .stream()
                    .map(EntityType::toString)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}