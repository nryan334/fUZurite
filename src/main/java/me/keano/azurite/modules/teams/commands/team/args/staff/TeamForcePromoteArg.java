package me.keano.azurite.modules.teams.commands.team.args.staff;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Member;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamForcePromoteArg extends Argument {

    private final List<Role> roles;

    public TeamForcePromoteArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "forcepromote"
                )
        );
        this.roles = new ArrayList<>(Arrays.asList(Role.values()));
        this.roles.remove(Role.LEADER);
        this.setPermissible("azurite.team.forcepromote");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEPROMOTE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        User target = getInstance().getUserManager().getByName(args[0]);

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[1])
            );
            return;
        }

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(target.getUniqueID());

        if (pt == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (!pt.getPlayers().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEPROMOTE.NOT_IN_TEAM")
                    .replace("%player%", target.getName())
            );
            return;
        }

        Member targetMember = pt.getMember(target.getUniqueID());
        Role toPromote = getRole(targetMember);

        if (targetMember.getRole() == toPromote) {
            sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEPROMOTE.HIGHEST_ROLE"));
            return;
        }

        targetMember.setRole(toPromote);
        pt.save();

        pt.broadcast(getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEPROMOTE.PROMOTED_BROADCAST")
                .replace("%role%", toPromote.getName())
                .replace("%player%", target.getName())
        );

        sendMessage(sender, getLanguageConfig().getString("ADMIN_TEAM_COMMAND.TEAM_FORCEPROMOTE.FORCE_PROMOTED")
                .replace("%role%", toPromote.getName())
                .replace("%player%", target.getName())
        );
    }

    private Role getRole(Member member) {
        if ((roles.indexOf(member.getRole()) == roles.size() - 1) || member.getRole() == Role.LEADER) {
            return member.getRole(); // already highest
        }

        return roles.get(roles.indexOf(member.getRole()) + 1);
    }
}