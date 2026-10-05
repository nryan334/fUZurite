package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TopCommand extends Command {

    public TopCommand(CommandManager manager) {
        super(
                manager,
                "top"
        );
        this.setPermissible("azurite.top");
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "highestpoint"
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
        Location teleport = getHighestSafe(player.getLocation());

        if (teleport == null) {
            sendMessage(sender, getLanguageConfig().getString("TOP_COMMAND.NOT_SAFE"));
            return;
        }

        player.teleport(teleport.add(0.5, 1.0, 0.5));
        sendMessage(sender, getLanguageConfig().getString("TOP_COMMAND.TELEPORTED"));
    }

    private Location getHighestSafe(Location location) {
        Location cloned = location.clone();
        int x = cloned.getBlockX();
        int y = location.getWorld().getMaxHeight();
        int z = cloned.getBlockZ();

        while (y > location.getBlockY()) {
            Block block = location.getWorld().getBlockAt(x, --y, z);

            if (!block.isEmpty()) {
                Location next = block.getLocation();
                next.setPitch(location.getPitch());
                next.setYaw(location.getYaw());
                return next;
            }
        }

        return null;
    }
}