package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.signs.kitmap.RefillSign;
import me.keano.azurite.modules.signs.kitmap.menu.RefillMenu;
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
public class RefillSignCommand extends Command {

    public RefillSignCommand(CommandManager manager) {
        super(
                manager,
                "refillsign"
        );
        this.setPermissible("azurite.refillcommand");
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
        RefillSign refillSign = getInstance().getCustomSignManager().getRefillSign();

        if (!(at instanceof SafezoneTeam) && !sender.hasPermission("azurite.refillcommand.everywhere")) {
            sendMessage(sender, getLanguageConfig().getString("CUSTOM_SIGNS.REFILL_SIGN.NOT_USE_SPAWN"));
            return;
        }

        if (refillSign != null && refillSign.getCooldown().hasCooldown(player)) {
            sendMessage(sender, getLanguageConfig().getString("CUSTOM_SIGNS.REFILL_SIGN.COOLDOWN")
                    .replace("%time%", refillSign.getCooldown().getRemaining(player))
            );
            return;
        }

        new RefillMenu(getInstance().getMenuManager(), player).open();

        if (refillSign != null) {
            refillSign.getCooldown().applyCooldown(player, getConfig().getInt("SIGNS_CONFIG.REFILL_SIGN.COOLDOWN"));
        }
    }
}