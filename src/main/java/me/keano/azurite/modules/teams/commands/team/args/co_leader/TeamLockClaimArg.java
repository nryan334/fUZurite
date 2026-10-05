package me.keano.azurite.modules.teams.commands.team.args.co_leader;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.extra.TeamCooldown;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamLockClaimArg extends Argument {

    private final TeamCooldown lockCooldown;

    public TeamLockClaimArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "lockclaim"
                )
        );
        this.lockCooldown = new TeamCooldown(manager);
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
        Claim claim = getInstance().getTeamManager().getClaimManager().getClaim(player.getLocation());
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (!getInstance().getSotwManager().isActive()) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LOCKCLAIM.ONLY_SOTW"));
            return;
        }

        if (!pt.checkRole(player, Role.CO_LEADER)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.CO_LEADER.getName())
            );
            return;
        }

        if (claim == null || pt.getUniqueID() != claim.getTeam()) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LOCKCLAIM.NO_OWN"));
            return;
        }

        if (claim.isLocked()) {
            claim.setLocked(false);
            pt.save();
            pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_LOCKCLAIM.UNLOCKED")
                    .replace("%player%", player.getName())
            );
            return;
        }

        // Below above so people can unlock without the cooldown.
        if (lockCooldown.hasCooldown(pt)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LOCKCLAIM.COOLDOWN")
                    .replace("%seconds%", lockCooldown.getRemaining(pt))
            );
            return;
        }

        lockCooldown.applyCooldown(pt, getConfig().getInt("TIMERS_COOLDOWN.LOCK_CLAIM"));
        claim.setLocked(true);

        pt.save();
        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_LOCKCLAIM.LOCKED")
                .replace("%player%", player.getName())
        );

        // We need to teleport the current players who are not in the team outside.
        for (Player claimPlayer : claim.getPlayers()) {
            if (pt.getPlayers().contains(claimPlayer.getUniqueId())) continue;

            getInstance().getTeamManager().getClaimManager().teleportSafe(claimPlayer);
            sendMessage(claimPlayer, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LOCKCLAIM.TELEPORTED_SAFE"));
        }
    }
}