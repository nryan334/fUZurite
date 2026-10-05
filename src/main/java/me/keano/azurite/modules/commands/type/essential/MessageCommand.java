package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.CC;
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
public class MessageCommand extends Command {

    public MessageCommand(CommandManager manager) {
        super(
                manager,
                "message"
        );
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "msg",
                "tell",
                "m"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("MESSAGE_COMMAND.USAGE");
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
        Player target = Bukkit.getPlayer(args[0]);
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (getConfig().getBoolean("HIDE_VANISH_FROM_MESSAGE") && getInstance().getStaffManager().isVanished(target)
                && !player.hasPermission("azurite.vanish.message")) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND
                    .replace("%player%", args[0])
            );
            return;
        }

        if (message.isEmpty()) {
            sendMessage(sender, getLanguageConfig().getString("MESSAGE_COMMAND.CANNOT_SEND_EMPTY"));
            return;
        }

        User playerUser = getInstance().getUserManager().getByUUID(player.getUniqueId());
        User targetUser = getInstance().getUserManager().getByUUID(target.getUniqueId());

        if (playerUser.getIgnoring().contains(target.getUniqueId())) {
            sendMessage(sender, getLanguageConfig().getString("MESSAGE_COMMAND.IGNORING_TARGET"));
            return;
        }

        if (targetUser.getIgnoring().contains(player.getUniqueId())) {
            sendMessage(sender, getLanguageConfig().getString("MESSAGE_COMMAND.IGNORING_PLAYER"));
            return;
        }

        if (!playerUser.isPrivateMessages()) {
            sendMessage(sender, getLanguageConfig().getString("MESSAGE_COMMAND.TOGGLED_TARGET"));
            return;
        }

        if (!targetUser.isPrivateMessages()) {
            sendMessage(sender, getLanguageConfig().getString("MESSAGE_COMMAND.TOGGLED_PLAYER"));
            return;
        }

        target.sendMessage(getLanguageConfig().getString("MESSAGE_COMMAND.FROM_FORMAT")
                .replace("%player%", player.getName())
                .replace("%message%", message)
                .replace("%prefix%", CC.t(getInstance().getRankHook().getRankPrefix(player)))
                .replace("%suffix%", CC.t(getInstance().getRankHook().getRankSuffix(player)))
                .replace("%color%", CC.t(getInstance().getRankHook().getRankColor(player)))
        );

        player.sendMessage(getLanguageConfig().getString("MESSAGE_COMMAND.TO_FORMAT")
                .replace("%player%", target.getName())
                .replace("%message%", message)
                .replace("%prefix%", CC.t(getInstance().getRankHook().getRankPrefix(target)))
                .replace("%suffix%", CC.t(getInstance().getRankHook().getRankSuffix(target)))
                .replace("%color%", CC.t(getInstance().getRankHook().getRankColor(target)))
        );

        if (targetUser.isPrivateMessagesSound()) {
            getManager().playSound(target, getConfig().getString("MESSAGE_SOUND"), false);
        }

        playerUser.setReplied(target.getUniqueId());
        targetUser.setReplied(player.getUniqueId());
    }
}