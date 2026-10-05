package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.fanciful.FancyMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamTopArg extends Argument {

    public TeamTopArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "top"
                )
        );
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("raidable")) {
            this.sendTeamList(
                    sender, args, getInstance().getTeamManager().getTeamSorting().getTeamTop(),
                    "TEAM_COMMAND.TEAM_TOP.FORMAT_TEAM_RAIDABLE",
                    "TEAM_COMMAND.TEAM_TOP.TOP_SHOWN_RAIDABLE",
                    1
            );

        } else {
            this.sendTeamList(
                    sender, args, getInstance().getTeamManager().getTeamSorting().getTeamTop(),
                    "TEAM_COMMAND.TEAM_TOP.FORMAT_TEAM",
                    "TEAM_COMMAND.TEAM_TOP.TOP_SHOWN",
                    0
            );
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return Collections.singletonList("raidable");
        }

        return super.tabComplete(sender, args);
    }

    private void sendTeamList(CommandSender sender, String[] args, List<PlayerTeam> sorted, String format, String formatList, int pageArg) {
        Map<Integer, List<PlayerTeam>> teams = new HashMap<>();
        int page = 1; // 1 is the default.

        if (sorted.isEmpty()) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_TOP.NO_TEAMS_ONLINE"));
            return;
        }

        int pageCounter = 0;

        for (int i = 0; i < sorted.size(); i++) {
            if (i % 10 == 0) {
                pageCounter++; // increment page on the 10th.
            }

            PlayerTeam pt = sorted.get(i);

            // Add the list if it's not there yet.
            if (!teams.containsKey(pageCounter)) teams.put(pageCounter, new ArrayList<>());

            teams.get(pageCounter).add(pt);
        }

        // Handle the next page if possible.
        if (args.length > pageArg) {
            Integer pageWanted = getInt(args[pageArg]);

            if (pageWanted == null) {
                sendMessage(sender, Config.NOT_VALID_NUMBER
                        .replace("%number%", args[pageArg])
                );
                return;
            }

            if (!teams.containsKey(pageWanted)) {
                sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_TOP.PAGE_NOT_FOUND")
                        .replace("%number%", args[pageArg])
                );
                return;
            }

            page = pageWanted;
        }

        List<String> teamTop = getLanguageConfig().getStringList(formatList);

        for (String string : teamTop) {
            if (!string.equals("%team_top%")) {
                new FancyMessage(string
                        .replace("%page%", String.valueOf(page))
                        .replace("%max-pages%", String.valueOf(pageCounter))
                ).send(sender);
                continue;
            }

            List<PlayerTeam> pageTeams = teams.get(page);

            for (int i = 0; i < pageTeams.size(); i++) {
                PlayerTeam pt = pageTeams.get(i);
                String line = getLanguageConfig().getString(format)
                        .replace("%team%", (sender instanceof Player ? pt.getDisplayName((Player) sender) : pt.getName()))
                        .replace("%points%", String.valueOf(pt.getPoints()))
                        .replace("%raidablepoints%", String.valueOf(pt.getRaidablePoints()));

                List<String> hover = getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_TOP.HOVER_MESSAGE");

                hover.replaceAll(s -> s
                        .replace("%team%", (sender instanceof Player ? pt.getDisplayName((Player) sender) : pt.getName()))
                        .replace("%leader%", getInstance().getUserManager().getByUUID(pt.getLeader()).getName())
                        .replace("%balance%", String.valueOf(pt.getBalance()))
                        .replace("%kills%", String.valueOf(pt.getKills()))
                        .replace("%deaths%", String.valueOf(pt.getDeaths()))
                        .replace("%captures%", String.valueOf(pt.getKothCaptures()))
                        .replace("%raidablepoints%", String.valueOf(pt.getRaidablePoints()))
                );

                // Used to calculate the page, eg page 2 will start at 11 and not 1.
                int number = (page - 1) * 10 + i;

                new FancyMessage(line
                        .replace("%number%", String.valueOf(number + 1)))
                        .command("/team info " + pt.getName())
                        .tooltip(hover)
                        .send(sender);
            }
        }

        teams.clear(); // lower memory usage
    }
}