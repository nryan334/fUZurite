package me.keano.azurite.modules.teams.commands.team;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.teams.commands.team.args.*;
import me.keano.azurite.modules.teams.commands.team.args.captain.TeamInviteArg;
import me.keano.azurite.modules.teams.commands.team.args.captain.TeamKickArg;
import me.keano.azurite.modules.teams.commands.team.args.captain.TeamUninviteArg;
import me.keano.azurite.modules.teams.commands.team.args.captain.TeamWithdrawArg;
import me.keano.azurite.modules.teams.commands.team.args.co_leader.*;
import me.keano.azurite.modules.teams.commands.team.args.leader.TeamDisbandArg;
import me.keano.azurite.modules.teams.commands.team.args.leader.TeamLeaderArg;
import me.keano.azurite.modules.teams.commands.team.args.leader.TeamRenameArg;
import me.keano.azurite.modules.teams.commands.team.args.staff.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamCommand extends Command {

    public TeamCommand(CommandManager manager) {
        super(
                manager,
                "team"
        );

        // create a new arraylist, so we can modify the arguments before registered
        List<Argument> arguments = new ArrayList<>(Arrays.asList(
                // Staff
                new TeamSetBalArg(manager),
                new TeamSetPointsArg(manager),
                new TeamSetCapsArg(manager),
                new TeamSetDtrArg(manager),
                new TeamTeleportArg(manager),
                new TeamSetRegenArg(manager),
                new TeamForceLeaderArg(manager),
                new TeamForceJoinArg(manager),
                new TeamForceKickArg(manager),
                new TeamForceDisbandArg(manager),
                new TeamForcePromoteArg(manager),
                new TeamForceDemoteArg(manager),
                new TeamTeleportHereArg(manager),
                new TeamAddDtrArg(manager),
                new TeamForceRenameArg(manager),
                new TeamSetRaidablePointsArg(manager),

                // Normal
                new TeamCreateArg(manager),
                new TeamInviteArg(manager),
                new TeamDisbandArg(manager),
                new TeamClaimArg(manager),
                new TeamInfoArg(manager),
                new TeamMapArg(manager),
                new TeamKickArg(manager),
                new TeamLeaveArg(manager),
                new TeamListArg(manager),
                new TeamAllyArg(manager),
                new TeamSetHQArg(manager),
                new TeamSortArg(manager),
                new TeamJoinArg(manager),
                new TeamTopArg(manager),
                new TeamStuckArg(manager),
                new TeamHQArg(manager),
                new TeamFocusArg(manager),
                new TeamUnfocusArg(manager),
                new TeamRallyArg(manager),
                new TeamUnrallyArg(manager),
                new TeamDepositArg(manager),
                new TeamWithdrawArg(manager),
                new TeamRenameArg(manager),
                new TeamChatArg(manager),
                new TeamPromoteArg(manager),
                new TeamLeaderArg(manager),
                new TeamUninviteArg(manager),
                new TeamUnclaimArg(manager),
                new TeamDemoteArg(manager),
                new TeamLockClaimArg(manager),
                new TeamUnallyArg(manager),
                new TeamFalltrapCommand(manager),
                new TeamBaseCommand(manager),
                new TeamRosterArg(manager),
                new TeamFriendlyFire(manager),
                new TeamCampArg(manager),
                new TeamOpenArg(manager),
                new TeamInvitesArg(manager),
                new TeamAnnouncementArg(manager),
                new TeamEditorArg(manager)
        ));

        this.checkArguments(arguments); // Check before registering
        this.handleArguments(arguments);

        arguments.clear(); // No use for it anymore
    }

    // Used to check all the disabled arguments and don't register them
    private void checkArguments(List<Argument> arguments) {
        List<String> disabled = getConfig().getStringList("DISABLED_COMMANDS.TEAM_SUBCOMMANDS");
        Iterator<Argument> iterator = arguments.iterator();

        while (iterator.hasNext()) {
            Argument argument = iterator.next();

            for (String name : argument.getNames()) {
                if (!disabled.contains(name.toLowerCase())) continue;
                iterator.remove();
            }
        }
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "t",
                "f",
                "faction"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("TEAM_COMMAND.USAGE");
    }
}