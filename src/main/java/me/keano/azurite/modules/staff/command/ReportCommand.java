package me.keano.azurite.modules.staff.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.discord.type.ReportWebhook;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.staff.extra.StaffReport;
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
public class ReportCommand extends Command {

    private final Cooldown reportCooldown;

    public ReportCommand(CommandManager manager) {
        super(
                manager,
                "report"
        );
        this.reportCooldown = new Cooldown(manager);
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "cheater",
                "hacker"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("REPORT_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        Player reporting = Bukkit.getPlayer(args[0]);
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (reportCooldown.hasCooldown(player)) {
            sendMessage(sender, getLanguageConfig().getString("REPORT_COMMAND.REPORT_COOLDOWN")
                    .replace("%time%", reportCooldown.getRemaining(player))
            );
            return;
        }

        if (reporting == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        // Handle adding to list for menu
        List<StaffReport> staffReports = getInstance().getStaffManager().getReports();
        if (staffReports.size() >= getConfig().getInt("STAFF_MODE.REPORTS_MENU.MAX_REPORTS")) staffReports.clear();
        staffReports.add(new StaffReport(getInstance().getStaffManager(), player.getUniqueId(), reporting.getUniqueId(), reason));

        reportCooldown.applyCooldown(player, getConfig().getInt("TIMERS_COOLDOWN.REPORT_COMMAND"));
        sendMessage(sender, getLanguageConfig().getString("REPORT_COMMAND.REPORTED")
                .replace("%target%", reporting.getName())
                .replace("%reason%", reason)
        );

        if (Config.WEBHOOKS_ENABLED) {
            new ReportWebhook(getManager(), player, reporting, reason).executeAsync();
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("azurite.staff")) {
                sendMessage(online, getLanguageConfig().getString("REPORT_COMMAND.REPORTED_STAFF")
                        .replace("%player%", player.getName())
                        .replace("%target%", reporting.getName())
                        .replace("%reason%", reason)
                );
            }
        }
    }
}