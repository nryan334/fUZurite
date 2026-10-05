package me.keano.azurite.modules.teams.commands.team.args.leader;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamRenameArg extends Argument {

    private final Cooldown renameCooldown;

    public TeamRenameArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "rename"
                )
        );
        this.renameCooldown = new Cooldown(manager);
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_RENAME.USAGE");
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
        String toRename = args[0];
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (!pt.checkRole(player, Role.LEADER)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.LEADER.getName())
            );
            return;
        }

        if (pt.getName().equals(toRename)) { // Equals and not equalsIgnoreCase
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_RENAME.ALREADY_NAME")
                    .replace("%name%", toRename)
            );
            return;
        }

        if (getInstance().getTeamManager().getTeam(toRename) != null) {
            sendMessage(sender, Config.TEAM_ALREADY_EXISTS
                    .replace("%team%", toRename)
            );
            return;
        }

        if (Utils.isNotAlphanumeric(toRename)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_RENAME.NOT_ALPHANUMERICAL"));
            return;
        }

        if (toRename.length() < Config.TEAM_NAME_MIN_LENGTH) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_RENAME.MIN_LENGTH")
                    .replace("%amount%", String.valueOf(Config.TEAM_NAME_MIN_LENGTH))
            );
            return;
        }

        if (toRename.length() > Config.TEAM_NAME_MAX_LENGTH) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_RENAME.MAX_LENGTH")
                    .replace("%amount%", String.valueOf(Config.TEAM_NAME_MAX_LENGTH))
            );
            return;
        }

        if (renameCooldown.hasCooldown(player)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_RENAME.RENAME_COOLDOWN")
                    .replace("%seconds%", renameCooldown.getRemaining(player))
            );
            return;
        }

        Bukkit.broadcastMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_RENAME.RENAMED")
                .replace("%team%", pt.getName())
                .replace("%name%", toRename)
        );

        getInstance().getTeamManager().getStringTeams().remove(pt.getName());
        getInstance().getTeamManager().getStringTeams().put(toRename, pt);

        renameCooldown.applyCooldown(player, getConfig().getInt("TIMERS_COOLDOWN.TEAM_RENAME_CD"));
        pt.setName(toRename);
        pt.save();
    }
}