package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class InvseeCommand extends Command {

    private final Map<UUID, UUID> openMenus;

    public InvseeCommand(CommandManager manager) {
        super(
                manager,
                "invsee"
        );
        this.setPermissible("azurite.invsee");
        this.openMenus = new HashMap<>();
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("INVSEE_COMMAND.USAGE");
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
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        player.openInventory(target.getInventory());
        openMenus.put(player.getUniqueId(), target.getUniqueId());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        openMenus.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        Iterator<Map.Entry<UUID, UUID>> iterator = openMenus.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();

            if (entry.getValue().equals(player.getUniqueId())) {
                Player opened = Bukkit.getPlayer(entry.getKey());
                if (opened != null) opened.closeInventory();
                iterator.remove();
                continue;
            }

            if (entry.getKey().equals(player.getUniqueId())) {
                iterator.remove();
            }
        }
    }
}