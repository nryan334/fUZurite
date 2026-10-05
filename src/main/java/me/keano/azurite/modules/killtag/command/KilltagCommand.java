package me.keano.azurite.modules.killtag.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.killtag.menu.KilltagMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KilltagCommand extends Command {

    public KilltagCommand(CommandManager manager) {
        super(
                manager,
                "killtag"
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "killtags",
                "killname",
                "killmessage"
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
        new KilltagMenu(getInstance().getMenuManager(), player).open();
    }
}