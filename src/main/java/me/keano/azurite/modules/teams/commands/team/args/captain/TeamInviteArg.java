package me.keano.azurite.modules.teams.commands.team.args.captain;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.fanciful.FancyMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamInviteArg extends Argument {

    private final String invites;

    public TeamInviteArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "invite",
                        "inv"
                )
        );
        this.invites = "%%__NONCE__%%";
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        User target = getInstance().getUserManager().getByName(args[0]);
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

        if (!pt.checkRole(player, Role.CAPTAIN)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.CAPTAIN.getName())
            );
            return;
        }

        if (pt.getPlayers().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITE.FRIENDLY_MEMBER"));
            return;
        }

        if (pt.getInvitedPlayers().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITE.ALREADY_INVITED"));
            return;
        }

        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITE.BROADCAST_INVITE")
                .replace("%player%", target.getName())
        );

        Player targetObject = target.getPlayer();

        if (targetObject != null) {
            FancyMessage fancyMessage = new FancyMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITE.MESSAGE_INVITE")
                    .replace("%team%", pt.getName())
                    .replace("%player%", player.getName()))
                    .tooltip(getLanguageConfig().getString("TEAM_COMMAND.TEAM_INVITE.HOVER"))
                    .command("/t join " + pt.getName());

            fancyMessage.send(targetObject);
        }

        pt.getInvitedPlayers().add(target.getUniqueID());
        // remove 3 mins later
        Tasks.executeLater(getManager(), (20 * 60) * 3L, () -> pt.getInvitedPlayers().remove(target.getUniqueID()));
    }
}