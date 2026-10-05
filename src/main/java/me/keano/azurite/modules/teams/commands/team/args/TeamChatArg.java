package me.keano.azurite.modules.teams.commands.team.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.settings.TeamChatSetting;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamChatArg extends Argument {

    private final List<TeamChatSetting> chatSettings; // cache them

    public TeamChatArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "chat",
                        "c"
                )
        );
        this.chatSettings = new ArrayList<>(Arrays.asList(TeamChatSetting.values()));
        this.load();
    }

    private void load() {
        Iterator<TeamChatSetting> iterator = chatSettings.iterator();

        while (iterator.hasNext()) {
            TeamChatSetting setting = iterator.next();

            if (setting == TeamChatSetting.PUBLIC) continue;

            if (setting == TeamChatSetting.STAFF) {
                iterator.remove();
                continue;
            }

            if (!getConfig().getBoolean("CHAT_FORMAT." + setting.name() + "_CHAT.ENABLED")) {
                iterator.remove();
            }
        }
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
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        TeamChatSetting toChange = getSetting(user.getTeamChatSetting());

        if (args.length > 0) {
            TeamChatSetting arg = getArgument(player, pt, args[0]);

            if (arg != null) {
                toChange = arg;
            }
        }

        while (!canUseSetting(player, pt, toChange)) {
            toChange = getSetting(toChange);
        }

        user.setTeamChatSetting(toChange);
        player.sendMessage(getLanguageConfig().getString("TEAM_COMMAND.TEAM_CHAT.CHAT_CHANGED")
                .replace("%chat%", toChange.name().toLowerCase())
        );
    }

    private TeamChatSetting getArgument(Player player, PlayerTeam pt, String string) {
        switch (string) {
            case "p":
            case "g":
            case "global":
            case "public":
            case "gc":
                return TeamChatSetting.PUBLIC;


            case "a":
            case "allies":
            case "ally":
            case "alliance":
            case "ac":
                return TeamChatSetting.ALLY;

            case "t":
            case "team":
            case "f":
            case "fac":
            case "faction":
            case "fc":
                return TeamChatSetting.TEAM;

            case "coleader":
            case "co":
            case "c":
                return (canUseSetting(player, pt, TeamChatSetting.CO_LEADER) ? TeamChatSetting.CO_LEADER : null);

            case "oc":
            case "captain":
            case "officer":
            case "o":
                return (canUseSetting(player, pt, TeamChatSetting.OFFICER) ? TeamChatSetting.OFFICER : null);
        }

        return null;
    }

    private boolean canUseSetting(Player player, PlayerTeam pt, TeamChatSetting setting) {
        switch (setting) {
            case TEAM:
            case PUBLIC:
            case ALLY:
                return true;

            case OFFICER:
                return pt.checkRole(player, Role.CAPTAIN);

            case CO_LEADER:
                return pt.checkRole(player, Role.CO_LEADER);
        }

        return false;
    }

    private TeamChatSetting getSetting(TeamChatSetting setting) {
        int indexOf = chatSettings.indexOf(setting);

        if (indexOf == chatSettings.size() - 1) {
            return chatSettings.get(0); // Return first one
        }

        return chatSettings.get(indexOf + 1);
    }
}