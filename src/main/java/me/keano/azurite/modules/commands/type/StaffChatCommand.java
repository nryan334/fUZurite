package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.settings.TeamChatSetting;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class StaffChatCommand extends Command {

    public StaffChatCommand(CommandManager manager) {
        super(
                manager,
                "staffchat"
        );
        this.setPermissible("azurite.staffchat");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("sc", "chatstaff");
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
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (user.getTeamChatSetting() == TeamChatSetting.STAFF) {
            user.setTeamChatSetting(TeamChatSetting.PUBLIC);
            user.save();
            sendMessage(sender, getLanguageConfig().getString("STAFF_CHAT_COMMAND.TOGGLED_OFF"));
            return;
        }

        user.setTeamChatSetting(TeamChatSetting.STAFF);
        user.save();
        sendMessage(sender, getLanguageConfig().getString("STAFF_CHAT_COMMAND.TOGGLED_ON"));
    }
}