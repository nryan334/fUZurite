package me.keano.azurite.modules.teams.commands.team.args.co_leader;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Member;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamPromoteArg extends Argument {

    private final List<Role> roles;

    public TeamPromoteArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "promote"
                )
        );
        this.roles = new ArrayList<>(Arrays.asList(Role.values()));
        this.roles.remove(Role.LEADER); // we don't want to allow players to be promoted to leader via this command
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_PROMOTE.USAGE");
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

        if (!pt.checkRole(player, Role.CO_LEADER)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.CO_LEADER.getName())
            );
            return;
        }

        if (!pt.getPlayers().contains(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_PROMOTE.NOT_IN_TEAM")
                    .replace("%player%", target.getName())
            );
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueID())) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_PROMOTE.PROMOTE_SELF"));
            return;
        }

        Member targetMember = pt.getMember(target.getUniqueID());
        Role toPromote = getRole(targetMember);

        if (targetMember.getRole() == toPromote) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_PROMOTE.HIGHEST_ROLE"));
            return;
        }

        // only leaders can promote co-leaders
        if (toPromote == Role.CO_LEADER && !pt.checkRole(player, Role.LEADER)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_PROMOTE.HIGHER_ROLE"));
            return;
        }

        targetMember.setRole(toPromote);
        pt.save();
        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_PROMOTE.PROMOTED_BROADCAST")
                .replace("%player%", target.getName())
                .replace("%role%", targetMember.getRole().getName())
        );
    }

    private Role getRole(Member member) {
        if ((roles.indexOf(member.getRole()) == roles.size() - 1) || member.getRole() == Role.LEADER) {
            return member.getRole(); // already highest
        }

        return roles.get(roles.indexOf(member.getRole()) + 1);
    }
}