package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.discord.type.RequestWebhook;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.extra.StaffRequest;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RequestCommand extends Command {

    private final Cooldown requestCooldown;

    public RequestCommand(CommandManager manager) {
        super(
                manager,
                "request"
        );
        this.requestCooldown = new Cooldown(manager);
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "helpop",
                "staffhelp"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("REQUEST_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        String reason = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        if (requestCooldown.hasCooldown(player)) {
            sendMessage(sender, getLanguageConfig().getString("REQUEST_COMMAND.REQUEST_COOLDOWN")
                    .replace("%time%", requestCooldown.getRemaining(player))
            );
            return;
        }

        // Handle adding to list for menu
        List<StaffRequest> staffRequests = getInstance().getStaffManager().getRequests();
        if (staffRequests.size() >= getConfig().getInt("STAFF_MODE.REQUESTS_MENU.MAX_REPORTS")) staffRequests.clear();
        staffRequests.add(new StaffRequest(getInstance().getStaffManager(), player.getUniqueId(), reason));

        requestCooldown.applyCooldown(player, getConfig().getInt("TIMERS_COOLDOWN.REQUEST_COMMAND"));
        sendMessage(sender, getLanguageConfig().getString("REQUEST_COMMAND.REQUESTED")
                .replace("%reason%", reason)
        );

        if (Config.WEBHOOKS_ENABLED) {
            new RequestWebhook(getManager(), player, reason).executeAsync();
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("azurite.staff")) {
                sendMessage(online, getLanguageConfig().getString("REQUEST_COMMAND.REQUESTED_STAFF")
                        .replace("%player%", player.getName())
                        .replace("%reason%", reason)
                );
            }
        }
    }
}