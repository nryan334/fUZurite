package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.framework.extra.TitlesConfig;
import me.keano.azurite.modules.teams.task.TeamViewerTask;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.Formatter;
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
public class TeamCreateArg extends Argument {

    private final Cooldown createCooldown;

    public TeamCreateArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "create"
                )
        );
        this.createCooldown = new Cooldown(manager);
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("TEAM_COMMAND.TEAM_CREATE.USAGE");
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

        String name = args[0];
        Player player = (Player) sender;

        if (getInstance().getTeamManager().getByPlayer(player.getUniqueId()) != null) {
            sendMessage(sender, Config.ALREADY_IN_TEAM);
            return;
        }

        if (getInstance().getTeamManager().getTeam(name) != null) {
            sendMessage(sender, Config.TEAM_ALREADY_EXISTS
                    .replace("%team%", name)
            );
            return;
        }

        if (Utils.isNotAlphanumeric(name)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CREATE.NOT_ALPHANUMERICAL"));
            return;
        }

        if (name.length() < Config.TEAM_NAME_MIN_LENGTH) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CREATE.MIN_LENGTH")
                    .replace("%amount%", String.valueOf(Config.TEAM_NAME_MIN_LENGTH))
            );
            return;
        }

        if (name.length() > Config.TEAM_NAME_MAX_LENGTH) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CREATE.MAX_LENGTH")
                    .replace("%amount%", String.valueOf(Config.TEAM_NAME_MAX_LENGTH))
            );
            return;
        }

        if (createCooldown.hasCooldown(player)) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CREATE.CREATE_COOLDOWN")
                    .replace("%seconds%", createCooldown.getRemaining(player))
            );
            return;
        }

        PlayerTeam pt = new PlayerTeam(getInstance().getTeamManager(), name, player.getUniqueId());

        if (getInstance().getEotwManager().isActive()) {
            long time = Formatter.parse(getConfig().getString("EOTW_TIMER.RAIDABLE_TIME"));
            pt.setDtr(-100);
            getInstance().getTimerManager().getTeamRegenTimer().applyTimer(pt, time);
        }

        pt.setTeamViewerTask(new TeamViewerTask(getInstance().getTeamManager(), pt.getUniqueID()));
        pt.save();

        createCooldown.applyCooldown(player, getConfig().getInt("TIMERS_COOLDOWN.TEAM_CREATE_CD"));
        TitlesConfig.TEAM_CREATE.sendTitle(player);

        sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CREATE.CREATED"));
        getInstance().getTeamManager().checkTeamSorting(player.getUniqueId());

        Bukkit.broadcastMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_CREATE.CREATED_BROADCAST")
                .replace("%team%", name)
                .replace("%player%", player.getName())
                .replace("%rank-prefix%", CC.t(getInstance().getRankHook().getRankPrefix(player)))
                .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(player)))
        );
    }
}