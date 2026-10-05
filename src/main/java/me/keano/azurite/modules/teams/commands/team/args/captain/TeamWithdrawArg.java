package me.keano.azurite.modules.teams.commands.team.args.captain;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamWithdrawArg extends Argument {

    public TeamWithdrawArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "withdraw",
                        "w"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_WITHDRAW.USAGE");
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
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (!pt.checkRole(player, Role.CAPTAIN)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.CAPTAIN.getName())
            );
            return;
        }

        Integer withdraw = getInt(args[0]);

        if (args[0].equalsIgnoreCase("all")) {
            withdraw = pt.getBalance();
        }

        if (withdraw == null || withdraw < 0) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[0])
            );
            return;
        }

        if (pt.getBalance() < withdraw) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_WITHDRAW.INSUFFICIENT_BAL"));
            return;
        }

        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        user.setBalance(user.getBalance() + withdraw);
        user.save();

        pt.setBalance(pt.getBalance() - withdraw);
        pt.save();
        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_WITHDRAW.WITHDREW")
                .replace("%player%", player.getName())
                .replace("%amount%", String.valueOf(withdraw))
        );
    }
}