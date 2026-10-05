package me.keano.azurite.modules.tablist.adapter;

import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.scheduler.extra.NextSchedule;
import me.keano.azurite.modules.tablist.Tablist;
import me.keano.azurite.modules.tablist.TablistAdapter;
import me.keano.azurite.modules.tablist.TablistManager;
import me.keano.azurite.modules.tablist.extra.TablistData;
import me.keano.azurite.modules.tablist.extra.TablistPlaceholder;
import me.keano.azurite.modules.tablist.extra.TablistSkin;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.player.Member;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.cuboid.Cuboid;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AzuriteTablist extends Module<TablistManager> implements TablistAdapter {

    private static final Comparator<Member> MEMBER_COMPARATOR = (o1, o2) -> o2.getRole().ordinal() - o1.getRole().ordinal();

    private final String[] header;
    private final String[] footer;

    private final List<String> leftTablist;
    private final List<String> middleTablist;
    private final List<String> rightTablist;
    private final List<String> farRightTablist;

    private final List<String> teamInfo;
    private final List<String> notInTeamInfo;

    private final Triple<Integer, Integer, List<TablistPlaceholder>> placeholders;

    public AzuriteTablist(TablistManager manager) {
        super(manager);

        this.header = getTablistConfig().getStringList("TABLIST_INFO.HEADER").toArray(new String[0]);
        this.footer = getTablistConfig().getStringList("TABLIST_INFO.FOOTER").toArray(new String[0]);

        this.leftTablist = getTablistConfig().getStringList("LEFT");
        this.middleTablist = getTablistConfig().getStringList("MIDDLE");
        this.rightTablist = getTablistConfig().getStringList("RIGHT");
        this.farRightTablist = getTablistConfig().getStringList("FAR_RIGHT");

        this.teamInfo = getTablistConfig().getStringList("TEAM_FORMAT.IN_TEAM");
        this.notInTeamInfo = getTablistConfig().getStringList("TEAM_FORMAT.NO_TEAM");

        this.placeholders = new Triple<>();
        this.load();
    }

    private void load() {
        // This will split the skins to the text itself.
        for (int row = 0; row < 20; row++) {
            String[] left = leftTablist.get(row).split(";");
            leftTablist.set(row, (left.length == 1 ? "" : left[1]));
            cachePlaceholders(0, row, (left.length == 1 ? "" : left[1]));

            String[] middle = middleTablist.get(row).split(";");
            middleTablist.set(row, (middle.length == 1 ? "" : middle[1]));
            cachePlaceholders(1, row, (middle.length == 1 ? "" : middle[1]));

            String[] right = rightTablist.get(row).split(";");
            rightTablist.set(row, (right.length == 1 ? "" : right[1]));
            cachePlaceholders(2, row, (right.length == 1 ? "" : right[1]));

            String[] farRight = farRightTablist.get(row).split(";");
            farRightTablist.set(row, (farRight.length == 1 ? "" : farRight[1]));
            cachePlaceholders(3, row, (farRight.length == 1 ? "" : farRight[1]));
        }
    }

    private void cachePlaceholders(int col, int row, String string) {
        List<TablistPlaceholder> list = new ArrayList<>();

        if (string.contains("%team-")) {
            int number = Integer.parseInt(string.split("-")[1].replace("%", ""));

            list.add((data, line, add) -> {
                List<PlayerTeam> teams = data.getTeamList();

                if (number > teams.size() - 1) {
                    line = line.replace("%team-" + number + "%", "");
                    if (add) data.getInfo().add(col, row, line);
                    return line;
                }

                PlayerTeam team = teams.get(number);
                line = line
                        .replace("%team-" + number + "%", data.getTeamListString()
                                .replace("%team-name%", team.getDisplayName(data.getPlayer()))
                                .replace("%dtr-color%", team.getDtrColor())
                                .replace("%dtr%", team.getDtrString())
                                .replace("%dtr-symbol%", team.getDtrSymbol())
                                .replace("%max-dtr%", Formatter.formatDtr(team.getMaxDtr()))
                                .replace("%team-online%", String.valueOf(team.getOnlinePlayersSize(false)))
                                .replace("%team-max-online%", String.valueOf(team.getPlayers().size()))
                                .replace("%number%", String.valueOf(number + 1)));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%member-")) {
            int number = Integer.parseInt(string.split("-")[1].replace("%", ""));

            list.add((data, line, add) -> {
                PlayerTeam pt = data.getPt();

                if (pt == null) {
                    line = line.replace("%member-" + number + "%", "");
                    if (add) data.getInfo().add(col, row, line);
                    return line;
                }

                List<Member> members = pt.getOnlineMembers(false);

                // Out of bounds
                if (number > members.size() - 1) {
                    line = line.replace("%member-" + number + "%", "");
                    if (add) data.getInfo().add(col, row, line);
                    return line;
                }

                // Normal replacing
                members.sort(MEMBER_COMPARATOR);
                Member member = members.get(number);

                line = line.replace("%member-" + number + "%", Config.TABLIST_MEMBER_FORMAT
                        .replaceAll("%role%", member.getAsterisk())
                        .replaceAll("%player%", Bukkit.getPlayer(member.getUniqueID()).getName()));

                if (add) {
                    Player memberPlayer = Bukkit.getPlayer(member.getUniqueID());

                    if (Config.TABLIST_MEMBER_SKIN && memberPlayer != null) {
                        data.getInfo().add(col, row,
                                TablistSkin.getPlayerSkin(getManager(), memberPlayer), line,
                                getInstance().getVersionManager().getVersion().getPing(memberPlayer));

                    } else data.getInfo().add(col, row, line);
                }
                return line;
            });
        }

        if (string.contains("%teaminfo-")) {
            int number = Integer.parseInt(string.split("-")[1].replace("%", ""));

            list.add((data, line, add) -> {
                PlayerTeam pt = data.getPt();

                if (pt == null) {
                    line = line.replace("%teaminfo-" + number + "%", notInTeamInfo.get(number));
                    if (add) data.getInfo().add(col, row, line);
                    return line;
                }

                line = line.replace("%teaminfo-" + number + "%", teamInfo.get(number)
                        .replace("%team-dtr-color%", pt.getDtrColor())
                        .replace("%team-dtr%", pt.getDtrString())
                        .replace("%team-dtr-symbol%", pt.getDtrSymbol())
                        .replace("%team-hq%", pt.getHQFormatted())
                        .replace("%team-online%", String.valueOf(pt.getOnlinePlayersSize(false)))
                        .replace("%team-maxonline%", String.valueOf(pt.getPlayers().size()))
                        .replace("%team-balance%", String.valueOf(pt.getBalance())));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%team%")) {
            list.add((data, line, add) -> {
                PlayerTeam pt = data.getPt();
                line = line.replace("%team%", (pt != null ? pt.getName() : ""));

                if (add) {
                    if (Config.TABLIST_TEAM_SKIN && pt != null) {
                        data.getInfo().add(col, row, Config.TABLIST_TEAM_SKIN_SKIN, line);

                    } else data.getInfo().add(col, row, line);
                }
                return line;
            });
        }

        if (string.contains("%balance%")) {
            list.add((data, line, add) -> {
                line = line.replace("%balance%", String.valueOf(data.getUser().getBalance()));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%lives%")) {
            list.add((data, line, add) -> {
                line = line.replace("%lives%", String.valueOf(data.getUser().getLives()));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%kills%")) {
            list.add((data, line, add) -> {
                line = line.replace("%kills%", String.valueOf(data.getUser().getKills()));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%deaths%")) {
            list.add((data, line, add) -> {
                line = line.replace("%deaths%", String.valueOf(data.getUser().getDeaths()));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%online%")) {
            list.add((data, line, add) -> {
                line = line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size() - data.getStaff()));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%max-online%")) {
            list.add((data, line, add) -> {
                line = line.replace("%max-online%", String.valueOf(Bukkit.getMaxPlayers()));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%direction%")) {
            list.add((data, line, add) -> {
                line = line.replace("%direction%", Utils.getCardinalDirection(data.getPlayer()));
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%claim%")) {
            list.add((data, line, add) -> {
                line = line.replace("%claim%", data.getAtPlayer().getDisplayName(data.getPlayer()));

                if (add) {
                    if (Config.TABLIST_CLAIM_SKIN) {
                        TeamType type = data.getAtPlayer().getType();
                        TablistSkin skin;

                        switch (type) {
                            case SAFEZONE:
                                skin = Config.TABLIST_TEAM_CLAIM_SAFEZONE;
                                break;

                            case ROAD:
                                skin = Config.TABLIST_TEAM_CLAIM_ROAD;
                                break;

                            case EVENT:
                                skin = Config.TABLIST_TEAM_CLAIM_EVENT;
                                break;

                            case CITADEL:
                                skin = Config.TABLIST_TEAM_CLAIM_CITADEL;
                                break;

                            case WARZONE:
                                skin = Config.TABLIST_TEAM_CLAIM_WARZONE;
                                break;

                            case CONQUEST:
                                skin = Config.TABLIST_TEAM_CLAIM_CONQUEST;
                                break;

                            case MOUNTAIN:
                                skin = Config.TABLIST_TEAM_CLAIM_MOUNTAIN;
                                break;

                            case WILDERNESS:
                                skin = Config.TABLIST_TEAM_CLAIM_WILDERNESS;
                                break;

                            case PLAYER:
                                PlayerTeam pt = data.getPt();
                                PlayerTeam target = (PlayerTeam) data.getAtPlayer();

                                if (target.getPlayers().contains(data.getPlayer().getUniqueId())) {
                                    skin = Config.TABLIST_TEAM_CLAIM_PLAYER_TEAM;

                                } else if (pt != null && pt.getAllies().contains(target.getUniqueID())) {
                                    skin = Config.TABLIST_TEAM_CLAIM_PLAYER_ALLY;

                                } else if (pt != null && pt.getFocus() == target.getUniqueID()) {
                                    skin = Config.TABLIST_TEAM_CLAIM_PLAYER_FOCUS;

                                } else skin = Config.TABLIST_TEAM_CLAIM_PLAYER_ENEMY;
                                break;

                            default:
                                skin = null;
                        }

                        data.getInfo().add(col, row, skin, line);

                    } else data.getInfo().add(col, row, line);
                }

                return line;
            });
        }

        if (string.contains("%title%")) {
            list.add((data, line, add) -> {
                line = line.replace("%title%", getManager().getTitle().getCurrent());
                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%scheduled%")) {
            list.add((data, line, add) -> {
                if (Config.TABLIST_REPLACE_KOTH && !data.getActiveKoths().isEmpty()) {
                    List<String> replace = new ArrayList<>();

                    for (Koth activeKoth : data.getActiveKoths()) {
                        Cuboid capzone = activeKoth.getCaptureZone();
                        replace.add(Config.TABLIST_REPLACE_KOTH_NAME
                                .replace("%koth%", activeKoth.getName())
                                .replace("%color%", activeKoth.getColor())
                                .replace("%loc%", capzone == null ? "None" : Utils.formatLocation(capzone.getCenter()))
                                .replace("%time%", Formatter.getRemaining(activeKoth.getRemaining(), false)));
                    }

                    line = line.replace("%scheduled%", String.join("§7, ", replace));

                } else {
                    line = line.replace("%scheduled%", data.getSchedule().getName());
                }

                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%scheduledtime%")) {
            list.add((data, line, add) -> {
                if (Config.TABLIST_REPLACE_KOTH && !data.getActiveKoths().isEmpty()) {
                    List<String> replace = new ArrayList<>();

                    for (Koth activeKoth : data.getActiveKoths()) {
                        Cuboid capzone = activeKoth.getCaptureZone();
                        replace.add(Config.TABLIST_REPLACE_KOTH_TIME
                                .replace("%koth%", activeKoth.getName())
                                .replace("%color%", activeKoth.getColor())
                                .replace("%loc%", capzone == null ? "None" : Utils.formatLocation(capzone.getCenter()))
                                .replace("%time%", Formatter.getRemaining(activeKoth.getRemaining(), false)));
                    }

                    line = line.replace("%scheduledtime%", String.join("§7, ", replace));

                } else {
                    line = line.replace("%scheduledtime%", data.getSchedule().getTimeFormatted());
                }

                if (add) data.getInfo().add(col, row, line);
                return line;
            });
        }

        if (string.contains("%location%")) {
            list.add(((data, line, add) -> {
                Location location = data.getPlayer().getLocation();
                line = line.replace("%location%", location.getBlockX() + ", " + location.getBlockZ());
                if (add) data.getInfo().add(col, row, line);
                return line;
            }));
        }

        if (!list.isEmpty()) {
            placeholders.put(col, row, list);
        }
    }

    @Override
    public String[] getHeader(Player player) {
        String[] finalHeader = header.clone();
        int staff = getInstance().getStaffManager().getVanished().size();

        // Need to do some replacing
        for (int i = 0; i < finalHeader.length; i++) {
            String string = finalHeader[i];
            finalHeader[i] = getInstance().getPlaceholderHook().replace(player, string)
                    .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size() - staff))
                    .replace("%max-online%", String.valueOf(Bukkit.getMaxPlayers()))
                    .replace("%title%", getManager().getTitle().getCurrent());
        }

        return finalHeader;
    }

    @Override
    public String[] getFooter(Player player) {
        String[] finalFooter = footer.clone();
        int staff = getInstance().getStaffManager().getVanished().size();

        // Need to do some replacing
        for (int i = 0; i < finalFooter.length; i++) {
            String string = finalFooter[i];
            finalFooter[i] = getInstance().getPlaceholderHook().replace(player, string)
                    .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size() - staff))
                    .replace("%max-online%", String.valueOf(Bukkit.getMaxPlayers()))
                    .replace("%title%", getManager().getTitle().getCurrent());
        }

        return finalFooter;
    }

    @Override
    public Tablist getInfo(Player player) {
        Tablist info = getManager().getTablists().get(player.getUniqueId());
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        Team atPlayer = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        NextSchedule schedule = getInstance().getScheduleManager().getNextSchedule();
        List<Koth> active = getInstance().getKothManager().getActiveKoths();
        List<PlayerTeam> teamList = getInstance().getTeamManager().getTeamSorting().getList(player);
        String teamListString = (user.getTeamListSetting().name().contains("DTR") ? Config.TABLIST_LIST_FORMAT_DTR : Config.TABLIST_LIST_FORMAT_ONLINE);

        int staff = getInstance().getStaffManager().getVanished().size();
        TablistData data = new TablistData(info, user, pt, atPlayer, schedule, teamListString, player, teamList, active, staff);

        for (int row = 0; row < 20; row++) {
            String left = leftTablist.get(row);
            String middle = middleTablist.get(row);
            String right = rightTablist.get(row);
            String farRight = farRightTablist.get(row);

            List<TablistPlaceholder> leftPlaceholders = placeholders.get(0, row);
            List<TablistPlaceholder> middlePlaceholders = placeholders.get(1, row);
            List<TablistPlaceholder> rightPlaceholders = placeholders.get(2, row);
            List<TablistPlaceholder> farRightPlaceholders = placeholders.get(3, row);

            this.addEntry(0, row, left, leftPlaceholders, data);
            this.addEntry(1, row, middle, middlePlaceholders, data);
            this.addEntry(2, row, right, rightPlaceholders, data);
            this.addEntry(3, row, farRight, farRightPlaceholders, data);
        }

        return info;
    }

    private void addEntry(int col, int row, String line, List<TablistPlaceholder> placeholders, TablistData data) {
        if (line.isEmpty()) return;

        boolean add = false;

        if (placeholders != null) {
            for (int i = 0; i < placeholders.size(); i++) {
                TablistPlaceholder placeholder = placeholders.get(i);

                if (i == placeholders.size() - 1) {
                    add = true;
                }

                line = placeholder.replaceOrAdd(data, line, add);
            }
        }

        if (!add) {
            data.getInfo().add(col, row, line);
        }
    }
}