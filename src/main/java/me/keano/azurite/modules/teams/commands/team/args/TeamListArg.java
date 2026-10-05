package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.extra.TeamSorting;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.settings.TeamListSetting;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.fanciful.FancyMessage;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamListArg extends Argument {

    public TeamListArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "list"
                )
        );
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        TeamSorting sorting = getInstance().getTeamManager().getTeamSorting();
        TeamListSetting setting = sorting.getSetting(sender);

        Map<Integer, List<PlayerTeam>> teams = new HashMap<>();
        List<PlayerTeam> sorted = getInstance().getTeamManager().getTeamSorting().getList(sender);

        int page = 1; // Default is always 1

        if (sorted.isEmpty()) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LIST.NO_TEAMS_ONLINE"));
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
        if (args.length > 0) {
            Integer pageWanted = getInt(args[0]);

            if (pageWanted == null) {
                sendMessage(sender, Config.NOT_VALID_NUMBER
                        .replace("%number%", args[0])
                );
                return;
            }

            if (!teams.containsKey(pageWanted)) {
                sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_LIST.PAGE_NOT_FOUND")
                        .replace("%number%", args[0])
                );
                return;
            }

            page = pageWanted;
        }

        List<String> teamList = getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_LIST.LIST_SHOWN");

        for (String string : teamList) {
            if (!string.equalsIgnoreCase("%team_list%")) {
                new FancyMessage(string
                        .replace("%page%", String.valueOf(page))
                        .replace("%max-pages%", String.valueOf(pageCounter))
                ).send(sender);
                continue;
            }

            List<PlayerTeam> pageTeams = teams.get(page);

            for (int i = 0; i < pageTeams.size(); i++) {
                PlayerTeam pt = pageTeams.get(i);
                String line;

                // Handle the type of listing.
                if (setting.name().contains("DTR")) {
                    line = getLanguageConfig().getString("TEAM_COMMAND.TEAM_LIST.FORMAT_DTR")
                            .replace("%team%", pt.getName())
                            .replace("%dtr%", pt.getDtrString())
                            .replace("%max-dtr%", Formatter.formatDtr(pt.getMaxDtr()));

                } else {
                    line = getLanguageConfig().getString("TEAM_COMMAND.TEAM_LIST.FORMAT_ONLINE")
                            .replace("%team%", pt.getName())
                            .replace("%online%", String.valueOf(pt.getOnlinePlayersSize(false)))
                            .replace("%max-online%", String.valueOf(pt.getPlayers().size()));
                }

                List<String> hover = getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_LIST.HOVER_MESSAGE");

                hover.replaceAll(s -> s
                        .replace("%dtr%", pt.getDtrString())
                        .replace("%hq%", pt.getHQFormatted())
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
    }
}