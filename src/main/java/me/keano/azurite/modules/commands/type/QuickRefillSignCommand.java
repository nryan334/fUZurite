package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.signs.kitmap.QuickRefillSign;
import me.keano.azurite.modules.signs.kitmap.menu.QuickRefillMenu;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class QuickRefillSignCommand extends Command {

    public QuickRefillSignCommand(CommandManager manager) {
        super(
                manager,
                "quickrefillsign"
        );
        this.setPermissible("azurite.quickrefillcommand");
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
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.INSUFFICIENT_PERM);
            return;
        }

        Player player = (Player) sender;
        Team at = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
        QuickRefillSign quickRefillSign = getInstance().getCustomSignManager().getQuickRefillSign();

        if (!(at instanceof SafezoneTeam) && !sender.hasPermission("azurite.quickrefillcommand.everywhere")) {
            sendMessage(sender, getLanguageConfig().getString("CUSTOM_SIGNS.QUICK_REFILL_SIGN.NOT_USE_SPAWN"));
            return;
        }

        if (quickRefillSign != null && quickRefillSign.getCooldown().hasCooldown(player)) {
            sendMessage(sender, getLanguageConfig().getString("CUSTOM_SIGNS.QUICK_REFILL_SIGN.COOLDOWN")
                    .replace("%time%", quickRefillSign.getCooldown().getRemaining(player))
            );
            return;
        }

        new QuickRefillMenu(getInstance().getMenuManager(), player).open();

        if (quickRefillSign != null) {
            quickRefillSign.getCooldown().applyCooldown(player, getConfig().getInt("SIGNS_CONFIG.QUICK_REFILL_SIGN.COOLDOWN"));
        }
    }
}