package me.keano.azurite.modules.kits.commands.kit;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.kits.Kit;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.listeners.playertimers.CombatTimer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KitCommand extends Command {

    public KitCommand(CommandManager manager) {
        super(
                manager,
                "kit"
        );
        this.setPermissible("azurite.kit");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("KIT_COMMAND.USAGE");
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
        Kit kit = getInstance().getKitManager().getKit(args[0]);
        Team at = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());
        CombatTimer combatTimer = getInstance().getTimerManager().getCombatTimer();

        if (kit == null || kit.isSystemKit()) {
            sendMessage(sender, getLanguageConfig().getString("KIT_COMMAND.NOT_FOUND")
                    .replace("%kit%", args[0])
            );
            return;
        }

        if (!(at instanceof SafezoneTeam) && !sender.hasPermission("azurite.kit.everywhere")) {
            sendMessage(sender, getLanguageConfig().getString("KIT_COMMAND.ONLY_SPAWN"));
            return;
        }

        if (combatTimer.hasTimer(player)) {
            sendMessage(sender, getLanguageConfig().getString("KIT_COMMAND.IN_COMBAT"));
            return;
        }

        if (!sender.hasPermission("azurite.kit." + kit.getName())) {
            sendMessage(sender, getLanguageConfig().getString("KIT_COMMAND.INSUFFICIENT_PERM")
                    .replace("%kit%", kit.getName())
            );
            return;
        }

        if (kit.getCooldown().hasCooldown(player)) {
            sendMessage(sender, getLanguageConfig().getString("KIT_COMMAND.ON_COOLDOWN")
                    .replace("%time%", kit.getCooldown().getRemaining(player))
            );
            return;
        }

        if (kit.checkConfirmation(player)) {
            return;
        }

        kit.getCooldown().applyCooldown(player, kit.getSeconds());
        kit.equip(player);
        sendMessage(sender, getLanguageConfig().getString("KIT_COMMAND.EQUIPPED")
                .replace("%kit%", kit.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getKitManager().getKits().values()
                    .stream()
                    .filter(kit -> !kit.isSystemKit())
                    .map(Kit::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}