package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.listeners.playertimers.CampTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamCampArg extends Argument {

    public TeamCampArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "camp"
                )
        );
        this.setPermissible("azurite.team.camp");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_CAMP.USAGE");
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
        Team atPlayer = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
        PlayerTeam target = getInstance().getTeamManager().getByPlayerOrTeam(args[0]);

        if (target == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (target.getHq() == null) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CAMP.NO_HQ"));
            return;
        }

        if (getInstance().getTimerManager().getCombatTimer().hasTimer(player) && !getConfig().getBoolean("CAMP_TIMER.ALLOW_COMBAT")) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CAMP.DENIED_COMBAT"));
            return;
        }

        if (getConfig().getBoolean("CAMP_TIMER.ONLY_SPAWN_AND_OWN_CLAIM") && !(atPlayer instanceof SafezoneTeam) && !(atPlayer instanceof PlayerTeam)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CAMP.DENIED_OTHER"));
            return;
        }

        if (target.getPlayers().contains(player.getUniqueId())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CAMP.DENIED_SELF"));
            return;
        }

        CampTimer campTimer = getInstance().getTimerManager().getCampTimer();

        if (atPlayer instanceof SafezoneTeam && getConfig().getBoolean("CAMP_TIMER.INSTANT_TP_SPAWN")) {
            campTimer.tpSafe(player, target.getUniqueID());
            return;
        }

        campTimer.getTeams().put(player.getUniqueId(), target.getUniqueID());
        campTimer.applyTimer(player);
        sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CAMP.STARTED_CAMP")
                .replace("%team%", target.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getTeams().values()
                    .stream()
                    .filter(team -> team instanceof PlayerTeam)
                    .map(Team::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}