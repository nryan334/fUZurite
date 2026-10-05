package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
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
public class TeamDepositArg extends Argument {

    public TeamDepositArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "deposit",
                        "d"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_DEPOSIT.USAGE");
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
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (args[0].equalsIgnoreCase("all")) {
            if (user.getBalance() <= 0) {
                sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_DEPOSIT.DEPOSIT_ZERO"));
                return;
            }

            int balance = user.getBalance();
            pt.setBalance(pt.getBalance() + balance);
            pt.save();

            user.setBalance(0);
            user.save();

            pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_DEPOSIT.DEPOSITED")
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(balance))
            );
            return;
        }

        Integer deposit = getInt(args[0]);

        if (deposit == null || deposit < 0) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[0])
            );
            return;
        }

        if (user.getBalance() < deposit) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_DEPOSIT.INSUFFICIENT_BAL")
                    .replace("%amount%", String.valueOf(deposit))
                    .replace("%balance%", String.valueOf(user.getBalance()))
            );
            return;
        }

        user.setBalance(user.getBalance() - deposit);
        user.save();

        pt.setBalance(pt.getBalance() + deposit);
        pt.save();

        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_DEPOSIT.DEPOSITED")
                .replace("%player%", player.getName())
                .replace("%amount%", String.valueOf(deposit))
        );
    }
}