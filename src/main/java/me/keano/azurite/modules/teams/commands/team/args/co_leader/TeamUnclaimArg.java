package me.keano.azurite.modules.teams.commands.team.args.co_leader;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.task.BaseTask;
import me.keano.azurite.modules.teams.task.FalltrapTask;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.UnaryOperator;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamUnclaimArg extends Argument {

    public TeamUnclaimArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "unclaim"
                )
        );
    }

    @Override
    public String usage() {
        return "/t unclaim";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (pt.getClaims().isEmpty()) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNCLAIM.NO_CLAIMS"));
            return;
        }

        if (pt.isRaidable()) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNCLAIM.RAIDABLE"));
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("ALL")) {
            int refundBalance = 0;

            for (Claim claim : pt.getClaims()) {
                refundBalance += getInstance().getTeamManager().getClaimManager().getPrice(claim, true);
                getInstance().getTeamManager().getClaimManager().deleteClaim(claim);
            }

            if (pt.getHq() != null) {
                // Azurite - Lunar Integration
                for (Player member : pt.getOnlinePlayers(true)) {
                    getInstance().getWaypointManager().getHqWaypoint().remove(member, pt.getHq(), UnaryOperator.identity());
                }

                pt.setHq(null); // no claim for it to be in
            }

            pt.setBalance(pt.getBalance() + refundBalance);
            pt.getClaims().clear();
            pt.save();

            pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNCLAIM.UNCLAIMED_ALL")
                    .replace("%player%", player.getName())
                    .replace("%balance%", String.valueOf(refundBalance))
            );
            return;
        }

        Claim atUnclaim = getInstance().getTeamManager().getClaimManager().getClaim(player.getLocation());

        if (!pt.getClaims().contains(atUnclaim)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNCLAIM.NOT_OWNED"));
            return;
        }

        int refund = getInstance().getTeamManager().getClaimManager().getPrice(atUnclaim, true);
        getInstance().getTeamManager().getClaimManager().deleteClaim(atUnclaim);

        if (pt.getHq() != null && atUnclaim.contains(pt.getHq())) {
            // Azurite - Lunar Integration
            for (Player member : pt.getOnlinePlayers(true)) {
                getInstance().getWaypointManager().getHqWaypoint().remove(member, pt.getHq(), UnaryOperator.identity());
            }

            pt.setHq(null);
        }

        Iterator<FalltrapTask> falltrapIterator = pt.getFalltrapTasks().iterator();

        while (falltrapIterator.hasNext()) {
            FalltrapTask falltrapTask = falltrapIterator.next();

            for (Block block : atUnclaim) {
                if (falltrapTask.getClaim().contains(block)) {
                    falltrapTask.cancelTask();
                    falltrapIterator.remove();
                    break;
                }
            }
        }

        Iterator<BaseTask> baseIterator = pt.getBaseTasks().iterator();

        while (baseIterator.hasNext()) {
            BaseTask baseTask = baseIterator.next();

            for (Block block : atUnclaim) {
                if (baseTask.getClaim().contains(block)) {
                    baseTask.cancelTask();
                    baseIterator.remove();
                    break;
                }
            }
        }

        pt.setBalance(pt.getBalance() + refund);
        pt.getClaims().remove(atUnclaim);
        pt.save();

        pt.broadcast(getLanguageConfig().getString("TEAM_COMMAND.TEAM_UNCLAIM.UNCLAIMED_LAND")
                .replace("%player%", player.getName())
                .replace("%x1%", String.valueOf(atUnclaim.getX1()))
                .replace("%z1%", String.valueOf(atUnclaim.getZ1()))
                .replace("%x2%", String.valueOf(atUnclaim.getX2()))
                .replace("%z2%", String.valueOf(atUnclaim.getZ2()))
                .replace("%balance%", String.valueOf(refund))
        );
    }
}