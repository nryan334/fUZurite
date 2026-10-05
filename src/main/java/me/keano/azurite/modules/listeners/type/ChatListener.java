package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.teams.extra.TeamPosition;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.settings.TeamChatSetting;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.extra.Cooldown;
import me.keano.azurite.utils.extra.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ChatListener extends Module<ListenerManager> {

    private final Map<String, TeamChatSetting> shortcuts;
    private final List<String> deniedChatMessages;
    private final Cooldown chatCooldown;

    public ChatListener(ListenerManager manager) {
        super(manager);

        this.chatCooldown = new Cooldown(manager);
        this.shortcuts = new HashMap<>();
        this.deniedChatMessages = new ArrayList<>();

        this.load();
    }

    private void load() {
        deniedChatMessages.addAll(getConfig().getStringList("CHAT_FORMAT.DENIED_WORDS")
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList()));

        for (TeamChatSetting setting : TeamChatSetting.values()) {
            if (setting == TeamChatSetting.PUBLIC) continue;
            if (setting == TeamChatSetting.STAFF) continue;

            String shortcut = getConfig().getString("CHAT_FORMAT." + setting.name() + "_CHAT.SHORTCUT");

            if (!shortcut.isEmpty()) shortcuts.put(shortcut, setting);
        }
    }

    private Pair<TeamChatSetting, String> getShortcut(String string) {
        if (string.isEmpty()) return null;

        TeamChatSetting setting = shortcuts.get(String.valueOf(string.charAt(0)));

        if (setting == null) return null;

        for (String s : shortcuts.keySet()) {
            string = string.replaceAll(s, "");
        }

        return new Pair<>(setting, string);
    }

    @EventHandler(priority = EventPriority.MONITOR) // call last so other events are able to cancel it.
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        String kill = getInstance().getUserManager().getPrefix(player);

        String message = (player.hasPermission("azurite.chat.color") ? CC.t(e.getMessage()) : e.getMessage());
        String killTop = "";
        String ftop = "";

        Pair<TeamChatSetting, String> shortcut = getShortcut(message);
        TeamChatSetting chatType = user.getTeamChatSetting();

        String prefix = CC.t(getInstance().getRankHook().getRankPrefix(player));
        String suffix = CC.t(getInstance().getRankHook().getRankSuffix(player));
        String color = CC.t(getInstance().getRankHook().getRankColor(player));
        String tag = CC.t(getInstance().getTagHook().getTag(player));

        boolean bypassed = player.hasPermission("azurite.chat.bypass");

        if (pt != null) {
            if (shortcut != null) {
                chatType = shortcut.getKey();
                message = shortcut.getValue();
            }

            TeamPosition pos = pt.getTeamPosition();
            ftop = (pos == null ? "" : Config.CHAT_FTOP_FORMAT
                    .replace("%ftop%", pos.getPrefix())
            );
        }

        if (kill != null) {
            killTop = getConfig().getString("CHAT_FORMAT.KILL_TOP_FORMAT")
                    .replace("%killtop%", kill.replace(" ", ""));
        }

        if (e.isCancelled() && chatType == TeamChatSetting.PUBLIC)
            return; // Don't send messages if its cancelled but make sure they can speak in chats other than public

        // We only censor messages in public chat.
        if (!player.hasPermission("azurite.profanity.bypass") && chatType == TeamChatSetting.PUBLIC) {
            for (String deniedChatMessage : deniedChatMessages) {
                if (!message.toUpperCase().contains(deniedChatMessage)) continue;

                e.setCancelled(true); // do nothing
                player.sendMessage(Config.CHAT_FORBIDDEN);
                return;
            }
        }

        if (chatCooldown.hasCooldown(player) && !bypassed && chatType == TeamChatSetting.PUBLIC) {
            e.setCancelled(true); // do nothing
            player.sendMessage(Config.CHAT_COOLDOWN
                    .replace("%seconds%", chatCooldown.getRemaining(player))
            );
            return;
        }

        // only slow their chat if they are in public.
        if (!bypassed && chatType == TeamChatSetting.PUBLIC) {
            chatCooldown.applyCooldown(player, getConfig().getInt("CHAT_FORMAT.COOLDOWN"));
        }

        e.setCancelled(true); // we send message per player not broadcast

        switch (chatType) {
            case STAFF:
                String staffMessage = "";

                for (Player recipient : e.getRecipients()) {
                    if (recipient.hasPermission("azurite.staff")) {
                        staffMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_STAFF)
                                .replace("%prefix%", prefix)
                                .replace("%suffix%", suffix)
                                .replace("%color%", color)
                                .replace("%player%", player.getName())
                                .replace("%s", message);
                        recipient.sendMessage(staffMessage);
                    }
                }

                Bukkit.getConsoleSender().sendMessage(staffMessage);
                return;

            case PUBLIC:
                String publicMessage = "";

                for (Player recipient : e.getRecipients()) {
                    User recipientUser = getInstance().getUserManager().getByUUID(recipient.getUniqueId());
                    if (!recipientUser.isPublicChat()) continue;

                    if (pt == null) {
                        publicMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_PUBLIC_NO_TEAM)
                                .replace("%prefix%", prefix)
                                .replace("%suffix%", suffix)
                                .replace("%color%", color)
                                .replace("%tag%", tag)
                                .replace("%ftop%", ftop)
                                .replace("%killtop%", killTop)
                                .replace("%player%", player.getName())
                                .replace("%s", message);
                        recipient.sendMessage(publicMessage);
                        continue;
                    }

                    TeamPosition pos = pt.getTeamPosition();
                    String format = getInstance().getPlaceholderHook().replace(player, Config.CHAT_PUBLIC_TEAM);
                    publicMessage = format
                            .replace("%prefix%", prefix)
                            .replace("%suffix%", suffix)
                            .replace("%color%", color)
                            .replace("%tag%", tag)
                            .replace("%ftop%", ftop)
                            .replace("%killtop%", killTop)
                            .replace("%team%", (pos != null && format.contains("%ftop-color%") ? pt.getName() : pt.getDisplayName(recipient)))
                            .replace("%ftop-color%", (pos != null ? pos.getColor() : ""))
                            .replace("%player%", player.getName())
                            .replace("%s", message);
                    recipient.sendMessage(publicMessage);
                }

                Bukkit.getConsoleSender().sendMessage(publicMessage);
                return;

            case TEAM:
                String teamMessage = "";

                // Reset the users chat if they are not in a team and set their chat type back to public
                if (pt == null) {
                    user.setTeamChatSetting(TeamChatSetting.PUBLIC);
                    user.save();

                    for (Player recipient : e.getRecipients()) {
                        User recipientUser = getInstance().getUserManager().getByUUID(recipient.getUniqueId());
                        if (!recipientUser.isPublicChat()) continue;

                        teamMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_PUBLIC_NO_TEAM)
                                .replace("%prefix%", prefix)
                                .replace("%suffix%", suffix)
                                .replace("%color%", color)
                                .replace("%tag%", tag)
                                .replace("%ftop%", ftop)
                                .replace("%killtop%", killTop)
                                .replace("%player%", player.getName())
                                .replace("%s", message);
                        recipient.sendMessage(teamMessage);
                    }

                    Bukkit.getConsoleSender().sendMessage(teamMessage);
                    return;
                }

                for (Player member : pt.getOnlinePlayers(true)) {
                    teamMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_TEAM)
                            .replace("%prefix%", prefix)
                            .replace("%suffix%", suffix)
                            .replace("%color%", color)
                            .replace("%player%", player.getName())
                            .replace("%s", message);
                    member.sendMessage(teamMessage);
                }

                Bukkit.getConsoleSender().sendMessage(teamMessage);
                return;

            case ALLY:
                String allyMessage = "";

                // Reset the users chat if they are not in a team and set their chat type back to public
                if (pt == null) {
                    user.setTeamChatSetting(TeamChatSetting.PUBLIC);
                    user.save();

                    for (Player recipient : e.getRecipients()) {
                        User recipientUser = getInstance().getUserManager().getByUUID(recipient.getUniqueId());
                        if (!recipientUser.isPublicChat()) continue;

                        allyMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_PUBLIC_NO_TEAM)
                                .replace("%prefix%", prefix)
                                .replace("%suffix%", suffix)
                                .replace("%color%", color)
                                .replace("%tag%", tag)
                                .replace("%ftop%", ftop)
                                .replace("%killtop%", killTop)
                                .replace("%player%", player.getName())
                                .replace("%s", message);
                        recipient.sendMessage(allyMessage);
                    }

                    Bukkit.getConsoleSender().sendMessage(allyMessage);
                    return;
                }

                for (Player member : pt.getOnlinePlayers(true)) {
                    allyMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_ALLY)
                            .replace("%prefix%", prefix)
                            .replace("%suffix%", suffix)
                            .replace("%color%", color)
                            .replace("%player%", player.getName())
                            .replace("%s", message);
                    member.sendMessage(allyMessage);
                }

                for (UUID ally : pt.getAllies()) {
                    PlayerTeam allied = getInstance().getTeamManager().getPlayerTeam(ally);

                    if (allied == null) continue;

                    for (Player allyPlayer : allied.getOnlinePlayers(true)) {
                        allyPlayer.sendMessage(allyMessage);
                    }
                }

                Bukkit.getConsoleSender().sendMessage(allyMessage);
                return;

            case OFFICER:
                String officerMessage = "";

                // Reset the users chat if they are not in a team and set their chat type back to public
                if (pt == null) {
                    user.setTeamChatSetting(TeamChatSetting.PUBLIC);
                    user.save();

                    for (Player recipient : e.getRecipients()) {
                        User recipientUser = getInstance().getUserManager().getByUUID(recipient.getUniqueId());
                        if (!recipientUser.isPublicChat()) continue;

                        officerMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_PUBLIC_NO_TEAM)
                                .replace("%prefix%", prefix)
                                .replace("%suffix%", suffix)
                                .replace("%color%", color)
                                .replace("%tag%", tag)
                                .replace("%ftop%", ftop)
                                .replace("%killtop%", killTop)
                                .replace("%player%", player.getName())
                                .replace("%s", message);
                        recipient.sendMessage(officerMessage);
                    }

                    Bukkit.getConsoleSender().sendMessage(officerMessage);
                    return;
                }

                for (Player member : pt.getOnlinePlayers(true)) {
                    if (!pt.checkRole(member, Role.CAPTAIN)) continue;

                    officerMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_OFFICER)
                            .replace("%prefix%", prefix)
                            .replace("%suffix%", suffix)
                            .replace("%color%", color)
                            .replace("%player%", player.getName())
                            .replace("%s", message);
                    member.sendMessage(officerMessage);
                }

                Bukkit.getConsoleSender().sendMessage(officerMessage);
                return;

            case CO_LEADER:
                String coLeaderMessage = "";

                // Reset the users chat if they are not in a team and set their chat type back to public
                if (pt == null) {
                    user.setTeamChatSetting(TeamChatSetting.PUBLIC);
                    user.save();

                    for (Player recipient : e.getRecipients()) {
                        User recipientUser = getInstance().getUserManager().getByUUID(recipient.getUniqueId());
                        if (!recipientUser.isPublicChat()) continue;

                        coLeaderMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_PUBLIC_NO_TEAM)
                                .replace("%prefix%", prefix)
                                .replace("%suffix%", suffix)
                                .replace("%color%", color)
                                .replace("%tag%", tag)
                                .replace("%ftop%", ftop)
                                .replace("%killtop%", killTop)
                                .replace("%player%", player.getName())
                                .replace("%s", message);
                        recipient.sendMessage(coLeaderMessage);
                    }

                    Bukkit.getConsoleSender().sendMessage(coLeaderMessage);
                    return;
                }

                for (Player member : pt.getOnlinePlayers(true)) {
                    if (!pt.checkRole(member, Role.CO_LEADER)) continue;

                    coLeaderMessage = getInstance().getPlaceholderHook().replace(player, Config.CHAT_CO_LEADER)
                            .replace("%prefix%", prefix)
                            .replace("%suffix%", suffix)
                            .replace("%color%", color)
                            .replace("%player%", player.getName())
                            .replace("%s", message);
                    member.sendMessage(coLeaderMessage);
                }

                Bukkit.getConsoleSender().sendMessage(coLeaderMessage);
                break;
        }
    }
}