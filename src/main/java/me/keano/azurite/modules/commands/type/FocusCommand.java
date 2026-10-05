package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class FocusCommand extends Command {

    public FocusCommand(CommandManager manager) {
        super(
                manager,
                "focus"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("FOCUS_COMMAND.USAGE");
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
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (pt.getSingularFocus().contains(target.getUniqueId())) {
            sendMessage(sender, getLanguageConfig().getString("FOCUS_COMMAND.ALREADY_FOCUSED"));
            return;
        }

        if (pt.getPlayers().contains(target.getUniqueId())) {
            sendMessage(sender, getLanguageConfig().getString("FOCUS_COMMAND.CANNOT_FOCUS_TEAMMATE"));
            return;
        }

        if (pt.isAlly(target)) {
            sendMessage(sender, getLanguageConfig().getString("FOCUS_COMMAND.CANNOT_FOCUS_ALLY"));
            return;
        }

        pt.getSingularFocus().add(target.getUniqueId());
        pt.broadcast(getLanguageConfig().getString("FOCUS_COMMAND.FOCUSED")
                .replace("%player%", player.getName())
                .replace("%target%", target.getName())
        );
    }
}