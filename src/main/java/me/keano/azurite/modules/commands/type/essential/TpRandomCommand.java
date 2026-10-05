package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TpRandomCommand extends Command {

    public TpRandomCommand(CommandManager manager) {
        super(
                manager,
                "tprandom"
        );
        this.setPermissible("azurite.tprandom");
        this.completions.add(new TabCompletion(Collections.singletonList("miner"), 0));
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "randomtp",
                "randtp"
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

        if (args.length == 1 && args[0].equalsIgnoreCase("miner")) {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers())
                    .stream()
                    .filter(player -> player.getLocation().getBlockY() <= getConfig().getInt("STAFF_MODE.RANDOM_TP_MINER_Y"))
                    .filter(player -> sender != player)
                    .collect(Collectors.toList());

            if (players.isEmpty()) {
                sendMessage(sender, getLanguageConfig().getString("TELEPORT_COMMAND.TPRANDOM_COMMAND.INSUFFICIENT_PLAYERS"));
                return;
            }

            Player target = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            ((Player) sender).teleport(target);
            sendMessage(sender, getLanguageConfig().getString("TELEPORT_COMMAND.TPRANDOM_COMMAND.TELEPORTED")
                    .replace("%player%", target.getName())
            );
            return;
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (players.size() == 1) {
            sendMessage(sender, getLanguageConfig().getString("TELEPORT_COMMAND.TPRANDOM_COMMAND.INSUFFICIENT_PLAYERS"));
            return;
        }

        Player player = (Player) sender;
        Player target = players.get(ThreadLocalRandom.current().nextInt(players.size()));

        while (player == target) {
            target = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        }

        player.teleport(target);
        sendMessage(sender, getLanguageConfig().getString("TELEPORT_COMMAND.TPRANDOM_COMMAND.TELEPORTED")
                .replace("%player%", target.getName())
        );
    }
}