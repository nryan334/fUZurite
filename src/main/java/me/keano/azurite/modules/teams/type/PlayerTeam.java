package me.keano.azurite.modules.teams.type;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.extra.TeamPosition;
import me.keano.azurite.modules.teams.player.Member;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.task.BaseTask;
import me.keano.azurite.modules.teams.task.FalltrapTask;
import me.keano.azurite.modules.timers.listeners.servertimers.anticlean.AntiCleanTask;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.waypoints.WaypointManager;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.fanciful.FancyMessage;
import me.keano.azurite.utils.fanciful.MessagePart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class PlayerTeam extends Team {

    private Set<UUID> players;
    private Set<UUID> allies;
    private Set<UUID> singularFocus;

    private Set<UUID> invitedPlayers;
    private Set<UUID> allyRequests;

    private Set<Member> roster;
    private Set<Member> members;
    private Set<FalltrapTask> falltrapTasks;
    private Set<BaseTask> baseTasks;

    private BukkitRunnable teamViewerTask;

    private UUID focus;
    private Location rallyPoint;
    private String announcement;

    private int balance;
    private int kothCaptures;
    private int points;
    private int raidablePoints;
    private int kills;
    private int deaths;

    private double dtr;
    private boolean minuteRegen; // was the minute regen task active, so we can start it if the server closed.
    private boolean friendlyFire;
    private boolean open;

    private AntiCleanTask antiCleanTask;

    // For deserialization
    public PlayerTeam(TeamManager manager, Map<String, Object> map) {
        super(
                manager,
                map,
                true,
                TeamType.PLAYER
        );
        this.allyRequests = new HashSet<>();
        this.invitedPlayers = new HashSet<>();
        this.players = Serializer.fetchUUIDs(map.get("players"));
        this.allies = Serializer.fetchUUIDs(map.get("allies"));
        this.singularFocus = Serializer.fetchUUIDs(map.get("singularFocus"));
        this.roster = Serializer.fetchMembers(map.get("roster"));
        this.members = Serializer.fetchMembers(map.get("members"));
        this.falltrapTasks = Serializer.fetchFalltrapTasks(getManager(), map.get("falltrapTasks"));
        this.baseTasks = Serializer.fetchBaseTasks(getManager(), map.get("baseTasks"));
        this.balance = Integer.parseInt((String) map.get("balance"));
        this.kothCaptures = Integer.parseInt((String) map.get("kothCaptures"));
        this.points = Integer.parseInt((String) map.get("points"));
        this.raidablePoints = Integer.parseInt((String) map.get("raidablePoints"));
        this.kills = Integer.parseInt((String) map.get("kills"));
        this.deaths = Integer.parseInt((String) map.get("deaths"));
        this.dtr = Double.parseDouble((String) map.get("dtr"));
        this.minuteRegen = Boolean.parseBoolean((String) map.get("minuteRegen"));
        this.friendlyFire = Boolean.parseBoolean((String) map.get("friendlyFire"));
        this.open = Boolean.parseBoolean((String) map.get("open"));

        if (Long.parseLong((String) map.get("regen")) > 0L) {
            getInstance().getTimerManager().getTeamRegenTimer().applyTimer(this, Long.parseLong((String) map.get("regen")));
        }

        getInstance().getTimerManager().getTeamRegenTimer().startMinuteRegen(this);

        if (map.containsKey("focus")) this.focus = UUID.fromString((String) map.get("focus"));
        if (map.containsKey("rallyPoint")) this.rallyPoint = Serializer.fetchLocation((String) map.get("rallyPoint"));
        if (map.containsKey("announcement")) this.announcement = (String) map.get("announcement");

        for (UUID player : getPlayers()) {
            getManager().getPlayerTeams().put(player, this);
        }
    }

    public PlayerTeam(TeamManager manager, String name, UUID leader) {
        super(
                manager,
                name,
                leader,
                true,
                TeamType.PLAYER
        );
        this.invitedPlayers = new HashSet<>();
        this.allyRequests = new HashSet<>();
        this.allies = new HashSet<>();
        this.singularFocus = new HashSet<>();
        this.roster = new HashSet<>();
        this.players = new HashSet<>(Collections.singletonList(leader));
        this.members = new HashSet<>(Collections.singletonList(new Member(leader, Role.LEADER)));
        this.falltrapTasks = new HashSet<>();
        this.baseTasks = new HashSet<>();
        this.balance = 0;
        this.kothCaptures = 0;
        this.points = 0;
        this.raidablePoints = 0;
        this.kills = 0;
        this.deaths = 0;
        this.dtr = getMaxDtr();
        this.focus = null;
        this.rallyPoint = null;
        this.announcement = null;
        this.minuteRegen = false;
        this.friendlyFire = false;
        this.open = false;
    }

    @Override
    public String getDisplayName(Player player) {
        return getDisplayColor(player) + super.getDisplayName(player);
    }

    public String getDisplayColor(Player player) {
        PlayerTeam viewer = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (isAlly(player)) {
            return Config.RELATION_ALLIED;

        } else if (viewer != null && viewer.getFocus() == uniqueID) {
            return Config.RELATION_FOCUSED;

        } else {
            return (getPlayers().contains(player.getUniqueId()) ? Config.RELATION_TEAMMATE : Config.RELATION_ENEMY);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();

        map.put("players", Serializer.serializeUUIDs(players));
        map.put("roster", Serializer.serializeMembers(roster));
        map.put("members", Serializer.serializeMembers(members));
        map.put("falltrapTasks", Serializer.serializeFalltrapTasks(falltrapTasks));
        map.put("baseTasks", Serializer.serializeBaseTasks(baseTasks));
        map.put("allies", Serializer.serializeUUIDs(allies));
        map.put("singularFocus", Serializer.serializeUUIDs(singularFocus));
        map.put("balance", String.valueOf(balance));
        map.put("kothCaptures", String.valueOf(kothCaptures));
        map.put("points", String.valueOf(points));
        map.put("raidablePoints", String.valueOf(raidablePoints));
        map.put("kills", String.valueOf(kills));
        map.put("deaths", String.valueOf(deaths));
        map.put("dtr", String.valueOf(dtr));
        map.put("regen", String.valueOf(getRegen()));
        map.put("minuteRegen", String.valueOf(minuteRegen));
        map.put("friendlyFire", String.valueOf(friendlyFire));
        map.put("open", String.valueOf(open));

        if (focus != null) map.put("focus", focus.toString());
        if (rallyPoint != null) map.put("rallyPoint", Serializer.serializeLoc(rallyPoint));
        if (announcement != null) map.put("announcement", announcement);

        return map;
    }

    @Override
    public void sendTeamStats(CommandSender sender) {
        List<String> co_leaders = members.stream()
                .filter(m -> m.getRole() == Role.CO_LEADER)
                .map(member -> makeMemberNice(member.getUniqueID()))
                .collect(Collectors.toList());

        List<String> captains = members.stream()
                .filter(m -> m.getRole() == Role.CAPTAIN)
                .map(member -> makeMemberNice(member.getUniqueID()))
                .collect(Collectors.toList());

        List<String> member = members.stream()
                .filter(m -> m.getRole() == Role.MEMBER)
                .map(m -> makeMemberNice(m.getUniqueID()))
                .collect(Collectors.toList());

        List<String> allied = allies.stream()
                .map(this::makeAllyNice)
                .collect(Collectors.toList());

        List<FancyMessage> toSend = Config.TEAM_INFO_ENEMIES;

        if (sender.hasPermission("azurite.teaminfo.staff")) {
            toSend = Config.TEAM_INFO_STAFF;

        } else if (sender instanceof Player) {
            Player player = (Player) sender;

            if (players.contains(player.getUniqueId()) || isAlly(player)) {
                toSend = Config.TEAM_INFO_FRIENDLY;
            }
        }

        toSend = toSend.stream().map(FancyMessage::clone).collect(Collectors.toList());

        toSend.removeIf(message -> {
            for (MessagePart messagePart : message.getMessageParts()) {
                String s = messagePart.text.getReadableString();
                if (s.contains("%co-leaders%") && co_leaders.isEmpty()) return true;
                if (s.contains("%captains%") && captains.isEmpty()) return true;
                if (s.contains("%members%") && member.isEmpty()) return true;
                if (s.contains("%allies%") && allied.isEmpty()) return true;
                if (s.contains("%announcement%") && (announcement == null || announcement.isEmpty())) return true;
                if (s.contains("%regen%") && !hasRegen()) return true;
            }
            return false;
        });

        UnaryOperator<String> replacer = s -> s
                .replace("%balance%", String.valueOf(balance))
                .replace("%name%", name)
                .replace("%online%", String.valueOf(getOnlinePlayersSize(false)))
                .replace("%max-online%", String.valueOf(getPlayers().size()))
                .replace("%hq%", getHQFormatted())

                .replace("%leader%", makeMemberNice(leader))
                .replace("%allies%", String.join(", ", allied))
                .replace("%co-leaders%", String.join(", ", co_leaders))
                .replace("%captains%", String.join(", ", captains))
                .replace("%members%", String.join(", ", member))
                .replace("%announcement%", (announcement != null ? announcement : ""))

                .replace("%points%", String.valueOf(points))
                .replace("%raidablepoints%", String.valueOf(raidablePoints))
                .replace("%kothCaptures%", String.valueOf(kothCaptures))
                .replace("%balance%", String.valueOf(balance))
                .replace("%dtr%", getDtrString())
                .replace("%dtr-color%", getDtrColor())
                .replace("%dtr-symbol%", getDtrSymbol())
                .replace("%regen%", Formatter.formatDetailed(getRegen()))

                .replace("%kills%", String.valueOf(kills))
                .replace("%deaths%", String.valueOf(deaths));

        for (FancyMessage fancyMessage : toSend) {
            fancyMessage.send(sender, replacer);
        }
    }

    public TeamPosition getTeamPosition() {
        if (!Config.LUNAR_PREFIXES_ENABLED) return null;

        int index = getManager().getTeamSorting().getTeamTop().indexOf(this); // No need to limit.

        switch (index) {
            case 0:
                return new TeamPosition(Config.LUNAR_ONE_COLOR, Config.LUNAR_ONE_PREFIX);
            case 1:
                return new TeamPosition(Config.LUNAR_TWO_COLOR, Config.LUNAR_TWO_PREFIX);
            case 2:
                return new TeamPosition(Config.LUNAR_THREE_COLOR, Config.LUNAR_THREE_PREFIX);
            default:
                return null;
        }
    }

    public long getRegen() {
        return getInstance().getTimerManager().getTeamRegenTimer().getRemaining(this);
    }

    public boolean hasRegen() {
        return getInstance().getTimerManager().getTeamRegenTimer().hasTimer(this);
    }

    public void setDtr(double newDtr) {
        if (newDtr > getMaxDtr()) {
            getInstance().getTimerManager().getTeamRegenTimer().removeTimer(this); // Remove since we're already max
            dtr = getMaxDtr();

        } else {
            dtr = Math.max(newDtr, -0.99);
        }
    }

    public double getMaxDtr() {
        int size = Config.DTR_PER_MEMBERS.size();
        int member = members.size();
        return member > size ? Config.DTR_PER_MEMBERS.get(size - 1) : Config.DTR_PER_MEMBERS.get(member - 1);
    }

    public String getDtrString() {
        return Formatter.formatDtr(dtr);
    }

    public String getDtrColor() {
        if (isRaidable()) {
            return Config.RAIDABLE_COLOR;

        } else if (dtr <= Config.LOW_DTR) {
            return Config.LOW_DTR_COLOR;

        } else {
            return Config.NORMAL_DTR_COLOR;
        }
    }

    public String getDtrSymbol() {
        if (minuteRegen) return Config.SYMBOL_REGENERATING;
        if (hasRegen()) return Config.SYMBOL_FREEZE;
        return Config.SYMBOL_NORMAL;
    }

    public boolean isRaidable() {
        return this.getDtr() <= 0;
    }

    public boolean isAlly(Player player) {
        PlayerTeam pt = getManager().getByPlayer(player.getUniqueId());
        if (pt == null) return false;
        return allies.contains(pt.getUniqueID());
    }

    public boolean isFocused(Player player) {
        if (focus == null) return false;
        Team pt = getManager().getByPlayer(player.getUniqueId());
        if (pt == null) return false;
        return pt.getUniqueID() == focus;
    }

    public Team getFocusedTeam() {
        return getManager().getTeam(focus);
    }

    public List<Player> getOnlinePlayers(boolean countVanish) {
        List<Player> online = new ArrayList<>();

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                if (!countVanish && getInstance().getStaffManager().isVanished(player)) continue;
                online.add(player);
            }
        }

        return online;
    }

    public List<Member> getOnlineMembers(boolean countVanish) {
        List<Member> members = new ArrayList<>();

        for (Player player : getOnlinePlayers(countVanish)) {
            members.add(getMember(player.getUniqueId()));
        }

        return members;
    }

    public int getOnlinePlayersSize(boolean countVanish) {
        return getOnlinePlayers(countVanish).size();
    }

    public Member getRoster(UUID uuid) {
        for (Member member : roster) if (member.getUniqueID().equals(uuid)) return member;
        return null;
    }

    public Member getMember(UUID uuid) {
        for (Member member : members) if (member.getUniqueID().equals(uuid)) return member;
        return null;
    }

    public boolean checkRole(Player player, Role role) {
        return getMember(player.getUniqueId()).getRole().ordinal() >= role.ordinal();
    }

    public void broadcast(String... s) {
        for (Player player : getOnlinePlayers(true)) {
            for (String string : s) player.sendMessage(CC.t(string));
        }
    }

    public void broadcastAlly(String... s) {
        if (allies.isEmpty()) return;

        for (UUID uuid : allies) {
            PlayerTeam allied = getManager().getPlayerTeam(uuid);
            List<Player> alliedOnline = allied.getOnlinePlayers(true);

            if (alliedOnline.isEmpty()) continue;

            for (Player alliedPlayer : alliedOnline) {
                for (String string : s) {
                    alliedPlayer.sendMessage(CC.t(string));
                }
            }
        }
    }

    private String makeMemberNice(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        User user = getInstance().getUserManager().getByUUID(uuid);

        if (user == null) {
            return "NULL USER";
        }

        return (player != null && !getInstance().getStaffManager().isVanished(player) ?
                Config.MEMBER_ONLINE
                        .replace("%player%", user.getName())
                        .replace("%kills%", String.valueOf(user.getKills())) : user.getDeathban() != null ?
                Config.MEMBER_DEATHBANNED
                        .replace("%player%", user.getName())
                        .replace("%kills%", String.valueOf(user.getKills())) :
                Config.MEMBER_OFFLINE
                        .replace("%player%", user.getName())
                        .replace("%kills%", String.valueOf(user.getKills()))
        );
    }

    private String makeAllyNice(UUID uuid) {
        PlayerTeam pt = getManager().getPlayerTeam(uuid);
        return (Config.ALLY_FORMAT
                .replace("%team%", pt.getName())
                .replace("%online%", String.valueOf(pt.getOnlinePlayers(false).size()))
                .replace("%max-online%", String.valueOf(pt.getPlayers().size()))
        );
    }

    public void joinMember(Player player, Role role) {
        players.add(player.getUniqueId());
        members.add(new Member(player.getUniqueId(), role));
        invitedPlayers.remove(player.getUniqueId());
        if (!hasRegen()) this.setDtr(getMaxDtr());
        this.save();

        getManager().getPlayerTeams().put(player.getUniqueId(), this);
        getManager().checkTeamSorting(player.getUniqueId());
        WaypointManager manager = getInstance().getWaypointManager();

        // Azurite - Lunar Integration
        manager.getHqWaypoint().send(player, hq, UnaryOperator.identity());
        manager.getRallyWaypoint().send(player, rallyPoint, UnaryOperator.identity());

        getInstance().getClassManager().checkClassLimit(player);

        if (focus != null) {
            Team focusedTeam = getFocusedTeam();

            if (focusedTeam != null) {
                manager.getFocusWaypoint().send(player, focusedTeam.getHq(), s -> s
                        .replace("%team%", focusedTeam.getName())
                );
            }
        }
    }

    public void removeMember(User user) {
        Player playerObject = user.getPlayer();

        if (playerObject != null) {
            WaypointManager manager = getInstance().getWaypointManager();

            for (Player onlinePlayer : getOnlinePlayers(true)) {
                // Azurite - Handle Teamviewer
                getInstance().getClientHook().clearTeamViewer(onlinePlayer);
            }

            // Azurite - Lunar Integration
            manager.getRallyWaypoint().remove(playerObject, rallyPoint, UnaryOperator.identity());
            manager.getHqWaypoint().remove(playerObject, hq, UnaryOperator.identity());

            if (focus != null) {
                Team focusedTeam = getFocusedTeam();

                if (focusedTeam != null) {
                    manager.getFocusWaypoint().remove(playerObject, focusedTeam.getHq(), s -> s
                            .replace("%team%", focusedTeam.getName())
                    );
                }
            }
        }

        players.remove(user.getUniqueID());
        members.remove(getMember(user.getUniqueID()));
        if (getDtr() > getMaxDtr()) setDtr(getMaxDtr()); // Limit the DTR
        this.save();
        getManager().getPlayerTeams().remove(user.getUniqueID());
        getManager().checkTeamSorting(user.getUniqueID());
    }

    public void disband() {
        User user = getInstance().getUserManager().getByUUID(leader);

        if (user != null) {
            for (Claim claim : claims) {
                user.setBalance(user.getBalance() + getInstance().getTeamManager().getClaimManager().getPrice(claim, true));
            }
        }

        WaypointManager manager = getInstance().getWaypointManager();

        // Azurite - Lunar Integration
        for (Player member : getOnlinePlayers(true)) {
            manager.getHqWaypoint().remove(member, hq, UnaryOperator.identity());
            manager.getRallyWaypoint().remove(member, rallyPoint, UnaryOperator.identity());

            if (focus != null) {
                Team focusedTeam = getFocusedTeam();

                if (focusedTeam != null) {
                    manager.getFocusWaypoint().remove(member, focusedTeam.getHq(), s -> s
                            .replace("%team%", focusedTeam.getName())
                    );
                }
            }
        }

        for (Player onlinePlayer : getOnlinePlayers(true)) {
            getInstance().getClientHook().clearTeamViewer(onlinePlayer);
        }

        Conquest conquest = getInstance().getConquestManager().getConquest();
        conquest.getPoints().remove(uniqueID);
        conquest.sortPoints();

        Utils.iterate(falltrapTasks, (task) -> {
            task.cancelTask();
            return true;
        });

        Utils.iterate(baseTasks, (task) -> {
            task.cancelTask();
            return true;
        });

        this.delete();

        for (Claim claim : claims) {
            getInstance().getTeamManager().getClaimManager().deleteClaim(claim);
        }
    }

    @Override
    public void save() {
        super.save();
        for (UUID player : players) getManager().getPlayerTeams().put(player, this);
    }

    @Override
    public void delete() {
        super.delete();
        getManager().getTeamSorting().remove(this);
        for (UUID ally : allies) getManager().getPlayerTeam(ally).getAllies().remove(uniqueID);
        for (UUID player : players) getManager().getPlayerTeams().remove(player);
    }
}