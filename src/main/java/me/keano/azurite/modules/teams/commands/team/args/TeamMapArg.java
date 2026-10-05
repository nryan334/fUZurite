package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamMapArg extends Argument {

    public TeamMapArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "map",
                        "claims"
                )
        );
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (user.isClaimsShown()) {
            user.setClaimsShown(false);
            getInstance().getWallManager().clearTeamMap(player);
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_MAP.MAP_HIDDEN"));
            return;
        }

        user.setClaimsShown(true);
        getInstance().getWallManager().sendTeamMap(player);
    }
}