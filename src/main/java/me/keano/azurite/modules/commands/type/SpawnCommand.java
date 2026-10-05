package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.timers.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SpawnCommand extends Command {

    public SpawnCommand(CommandManager manager) {
        super(
                manager,
                "spawn"
        );
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
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        Location spawn = getInstance().getWaypointManager().getWorldSpawn();
        TimerManager manager = getInstance().getTimerManager();

        if (sender.hasPermission("azurite.spawn.admin")) {
            if (args.length == 0) {
                player.teleport(spawn);
                sendMessage(sender, getLanguageConfig().getString("SPAWN_COMMAND.SPAWNED"));
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[0])
                );
                return;
            }

            target.teleport(spawn);
            sendMessage(sender, getLanguageConfig().getString("SPAWN_COMMAND.SPAWNED_TARGET")
                    .replace("%player%", target.getName())
            );
            return;
        }

        if (getInstance().getSotwManager().isActive() && !getInstance().getSotwManager().isEnabled(player)
                && getConfig().getBoolean("SPAWN_TIMER.INSTANT_TP_SOTW")) {
            manager.getSpawnTimer().applyTimer(player, 1L);
            return;
        }

        if (!getConfig().getBoolean("SPAWN_TIMER.ENABLED")) {
            sendMessage(sender, getLanguageConfig().getString("SPAWN_COMMAND.SPAWN_TIMER_DISABLED"));
            return;
        }

        if (manager.getCombatTimer().hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("SPAWN_COMMAND.COMBAT_TAGGED"));
            return;
        }

        if (manager.getSpawnTimer().hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("SPAWN_COMMAND.ALREADY_TELEPORTING"));
            return;
        }

        manager.getSpawnTimer().applyTimer(player);
        sendMessage(sender, getLanguageConfig().getString("SPAWN_COMMAND.WARPING")
                .replace("%seconds%", String.valueOf(manager.getSpawnTimer().getSeconds()))
        );
    }
}