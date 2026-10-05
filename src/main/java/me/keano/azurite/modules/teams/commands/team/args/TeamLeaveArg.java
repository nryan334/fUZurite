package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.eotw.EOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamLeaveArg extends Argument {

    public TeamLeaveArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "leave"
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
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        EOTWManager eotwManager = getInstance().getEotwManager();

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (pt.checkRole(player, Role.LEADER)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEAVE.CANNOT_LEAVE_LEADER"));
            return;
        }

        if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) == pt) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEAVE.CANNOT_LEAVE_IN_CLAIM"));
            return;
        }

        if (getInstance().getTimerManager().getCombatTimer().hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEAVE.CANNOT_LEAVE_COMBAT"));
            return;
        }

        if (pt.hasRegen() && !eotwManager.isActive() && !getTeamConfig().getBoolean("TEAMS.LEAVE_WHILE_REGEN")) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEAVE.CANNOT_LEAVE_FREEZE"));
            return;
        }

        pt.removeMember(getInstance().getUserManager().getByUUID(player.getUniqueId()));
        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEAVE.BROADCAST_TEAM")
                .replace("%player%", player.getName())
        );
        sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LEAVE.LEFT_MESSAGE"));
    }
}