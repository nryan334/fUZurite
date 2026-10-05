package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.eotw.EOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Member;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamJoinArg extends Argument {

    public TeamJoinArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "accept",
                        "join"
                )
        );
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_JOIN.USAGE");
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
        PlayerTeam target = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);
        EOTWManager eotwManager = getInstance().getEotwManager();

        if (getInstance().getTeamManager().getByPlayer(player.getUniqueId()) != null) {
            sendMessage(sender, Config.ALREADY_IN_TEAM);
            return;
        }

        if (target == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        Member roster = target.getRoster(player.getUniqueId());

        if (!target.getInvitedPlayers().contains(player.getUniqueId()) && roster == null && !target.isOpen()) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_JOIN.NOT_INVITED")
                    .replace("%team%", args[0])
            );
            return;
        }

        if (target.getPlayers().size() >= getTeamConfig().getInt("TEAMS.TEAM_SIZE")) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_JOIN.TEAM_FULL"));
            return;
        }

        if (target.hasRegen() && !eotwManager.isActive() && !getTeamConfig().getBoolean("TEAMS.JOIN_WHILE_REGEN")) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_JOIN.CANNOT_JOIN_FREEZE"));
            return;
        }

        if (getInstance().getTimerManager().getCombatTimer().hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_JOIN.CANNOT_JOIN_COMBAT"));
            return;
        }

        target.joinMember(player, (roster == null ? Role.MEMBER : roster.getRole()));
        target.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_JOIN.BROADCAST_JOIN")
                .replace("%player%", player.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getStringTeams().keySet()
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}