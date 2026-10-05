package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.timers.listeners.playertimers.GappleTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class GappleCommand extends Command {

    public GappleCommand(CommandManager manager) {
        super(
                manager,
                "gapple"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "gopple"
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
        GappleTimer timer = getInstance().getTimerManager().getGappleTimer();

        if (!timer.hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("GAPPLE_COMMAND.NO_TIMER"));
            return;
        }

        sendMessage(sender, getLanguageConfig().getString("GAPPLE_COMMAND.FORMAT")
                .replace("%remaining%", timer.getRemainingString(player))
        );
    }
}