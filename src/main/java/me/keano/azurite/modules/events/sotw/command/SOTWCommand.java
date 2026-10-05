package me.keano.azurite.modules.events.sotw.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.utils.Formatter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SOTWCommand extends Command {

    public SOTWCommand(CommandManager manager) {
        super(
                manager,
                "SOTW"
        );
        this.completions.add(new TabCompletion(Collections.singletonList("enable"), 0));
        this.completions.add(new TabCompletion(Arrays.asList("start", "end", "stop", "extend"), 0, "azurite.sotw"));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void sendUsage(CommandSender sender) {
        if (sender.hasPermission("azurite.sotw.admin")) {
            for (String s : getLanguageConfig().getStringList("SOTW_COMMAND.USAGE_ADMIN")) {
                sender.sendMessage(s);
            }

        } else {
            for (String s : getLanguageConfig().getStringList("SOTW_COMMAND.USAGE_DEFAULT")) {
                sender.sendMessage(s);
            }
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        SOTWManager sotwManager = getInstance().getSotwManager();

        switch (args[0].toLowerCase()) {
            case "start":
                if (!sender.hasPermission("azurite.sotw.admin")) {
                    sendMessage(sender, Config.INSUFFICIENT_PERM);
                    return;
                }

                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Long time = Formatter.parse(args[1]);

                if (time == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[1])
                    );
                    return;
                }

                if (sotwManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.SOTW_START.ALREADY_ACTIVE"));
                    return;
                }

                sotwManager.startSOTW(time);
                sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.SOTW_START.STARTED"));
                return;

            case "extend":
                if (!sender.hasPermission("azurite.sotw.admin")) {
                    sendMessage(sender, Config.INSUFFICIENT_PERM);
                    return;
                }

                if (args.length < 2) {
                    sendUsage(sender);
                    return;
                }

                Long extend = Formatter.parse(args[1]);

                if (extend == null) {
                    sendMessage(sender, Config.NOT_VALID_NUMBER
                            .replace("%number%", args[1])
                    );
                    return;
                }

                if (!sotwManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.NOT_ACTIVE"));
                    return;
                }

                sotwManager.extendSOTW(extend);
                sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.SOTW_EXTEND.EXTENDED"));
                return;

            case "fly":
                if (!sender.hasPermission("azurite.sotw.fly")) {
                    sendMessage(sender, Config.INSUFFICIENT_PERM);
                    return;
                }

                if (!(sender instanceof Player)) {
                    sendMessage(sender, Config.PLAYER_ONLY);
                    return;
                }

                if (!sotwManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.NOT_ACTIVE"));
                    return;
                }

                Player player = (Player) sender;
                Team at = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
                boolean canUse = sotwManager.canFly(player, at);

                if (!canUse) {
                    sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.SOTW_FLY.NOT_ALLOWED"));
                    return;
                }

                boolean toggle = sotwManager.toggleFly(player);
                sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.SOTW_FLY.TOGGLE_" + Boolean.toString(toggle).toUpperCase()));
                return;

            case "stop":
            case "end":
                if (!sender.hasPermission("azurite.sotw.admin")) {
                    sendMessage(sender, Config.INSUFFICIENT_PERM);
                    return;
                }

                if (!sotwManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.NOT_ACTIVE"));
                    return;
                }

                sotwManager.endSOTW();
                sotwManager.getEnabled().clear(); // Make sure we clear these.
                sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.SOTW_END.ENDED"));
                return;

            case "enable":
                if (!(sender instanceof Player)) {
                    sendMessage(sender, Config.PLAYER_ONLY);
                    return;
                }

                if (!sotwManager.isActive()) {
                    sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.NOT_ACTIVE"));
                    return;
                }

                Player enable = (Player) sender;

                if (sotwManager.isEnabled(enable)) {
                    sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.SOTW_ENABLE.ALREADY_ENABLED"));
                    return;
                }

                if (sotwManager.getFlying().contains(enable.getUniqueId())) {
                    sotwManager.toggleFly(enable);
                }

                sotwManager.getEnabled().add(enable.getUniqueId());
                sendMessage(sender, getLanguageConfig().getString("SOTW_COMMAND.SOTW_ENABLE.ENABLED"));
                return;
        }

        sendUsage(sender);
    }
}