package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LeaderboardsCommand extends Command {

    public LeaderboardsCommand(CommandManager manager) {
        super(
                manager,
                "leaderboards"
        );
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "leaderboard"
        );
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        new LeaderboardsMenu(getInstance().getMenuManager(), player).open();
    }

    private static class LeaderboardsMenu extends Menu {

        public LeaderboardsMenu(MenuManager manager, Player player) {
            super(
                    manager,
                    player,
                    manager.getLanguageConfig().getString("LEADERBOARDS_COMMAND.TITLE"),
                    manager.getLanguageConfig().getInt("LEADERBOARDS_COMMAND.SIZE"),
                    false
            );
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();

            for (String key : getLanguageConfig().getConfigurationSection("LEADERBOARDS_COMMAND.ITEMS").getKeys(false)) {
                String path = "LEADERBOARDS_COMMAND.ITEMS." + key + ".";

                buttons.put(getLanguageConfig().getInt(path + "SLOT"), new Button() {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        e.setCancelled(true);
                    }

                    @Override
                    public ItemStack getItemStack() {
                        ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getLanguageConfig().getString(path + "MATERIAL")))
                                .setName(getLanguageConfig().getString(path + "NAME"))
                                .data(getManager(), getLanguageConfig().getInt(path + "DATA"));

                        List<String> lore = getLanguageConfig().getStringList(path + "LORE");
                        LeaderboardType type = LeaderboardType.getByName(key.toUpperCase());

                        lore.replaceAll(s -> {
                            if (type != null) {
                                s = type.replace(getManager(), path, s);
                            }

                            // Any extra
                            if (s.contains("%deaths_top") || s.contains("%kills_top") || s.contains("%team_top") ||
                                    s.contains("%killstreaks_top") || s.contains("%kdr_top")) {
                                s = getLanguageConfig().getString("LEADERBOARDS_COMMAND.NONE_MESSAGE");
                            }

                            return s;
                        });

                        if (type != null) {
                            String owner = type.getSkull(getManager());
                            if (owner != null) builder.setSkullOwner(owner);
                        }

                        builder.setLore(lore);
                        return builder.toItemStack();
                    }
                });
            }

            return buttons;
        }
    }

    private enum LeaderboardType {

        TEAM_TOP("%team_top"),
        KILLS_TOP("%kills_top"),
        DEATHS_TOP("%deaths_top"),
        KDR_TOP("%kdr_top"),
        KILLSTREAKS_TOP("%killstreaks_top");

        private final String placeholder;

        LeaderboardType(String placeholder) {
            this.placeholder = placeholder;
        }

        public String replace(Manager manager, String path, String s) {
            List<PlayerTeam> toReplaceTeam = null;
            List<User> toReplace = null;

            switch (this) {
                case TEAM_TOP:
                    toReplaceTeam = manager.getInstance().getTeamManager().getTeamSorting().getTeamTop();
                    break;

                case KILLS_TOP:
                    toReplace = manager.getInstance().getUserManager().getTopKills();
                    break;

                case DEATHS_TOP:
                    toReplace = manager.getInstance().getUserManager().getTopDeaths();
                    break;

                case KDR_TOP:
                    toReplace = manager.getInstance().getUserManager().getTopKDR();
                    break;

                case KILLSTREAKS_TOP:
                    toReplace = manager.getInstance().getUserManager().getTopKillStreaks();
                    break;
            }

            if (toReplaceTeam != null) {
                for (int i = 0; i < toReplaceTeam.size(); i++) {
                    if (i == 10) break; // Limit the loop

                    PlayerTeam pt = toReplaceTeam.get(i);
                    s = s.replace(placeholder + (i + 1) + "%", manager.getLanguageConfig()
                            .getString(path + "FORMAT")
                            .replace("%team%", pt.getName())
                            .replace("%points%", String.valueOf(pt.getPoints())));
                }
            }

            if (toReplace != null) {
                for (int i = 0; i < toReplace.size(); i++) {
                    if (i == 10) break; // Limit the loop

                    User user = toReplace.get(i);
                    s = s.replace(placeholder + (i + 1) + "%", manager.getLanguageConfig()
                            .getString(path + "FORMAT")
                            .replace("%player%", user.getName())
                            .replace("%kills%", String.valueOf(user.getKills()))
                            .replace("%deaths%", String.valueOf(user.getDeaths()))
                            .replace("%killstreak%", String.valueOf(user.getKillstreak()))
                            .replace("%kdr%", user.getKDRString())
                    );
                }
            }

            return s;
        }

        public String getSkull(Manager manager) {
            List<PlayerTeam> toReplaceTeam = null;
            List<User> toReplace = null;

            switch (this) {
                case TEAM_TOP:
                    toReplaceTeam = manager.getInstance().getTeamManager().getTeamSorting().getTeamTop();
                    break;

                case KILLS_TOP:
                    toReplace = manager.getInstance().getUserManager().getTopKills();
                    break;

                case DEATHS_TOP:
                    toReplace = manager.getInstance().getUserManager().getTopDeaths();
                    break;

                case KDR_TOP:
                    toReplace = manager.getInstance().getUserManager().getTopKDR();
                    break;

                case KILLSTREAKS_TOP:
                    toReplace = manager.getInstance().getUserManager().getTopKillStreaks();
                    break;
            }

            if (toReplaceTeam != null) {
                try {

                    PlayerTeam team = toReplaceTeam.get(0);
                    return manager.getInstance().getUserManager().getByUUID(team.getLeader()).getName();

                } catch (IndexOutOfBoundsException e) {
                    // Ignored
                }
            }

            if (toReplace != null) {
                try {

                    User user = toReplace.get(0);
                    return user.getName();

                } catch (IndexOutOfBoundsException e) {
                    // Ignored
                }
            }

            return null;
        }

        public static LeaderboardType getByName(String name) {
            for (LeaderboardType type : LeaderboardType.values()) {
                if (type.name().equalsIgnoreCase(name)) return type;
            }
            return null;
        }
    }

}