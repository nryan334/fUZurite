package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CheckRedeemsCommand extends Command {

    public CheckRedeemsCommand(CommandManager manager) {
        super(
                manager,
                "checkredeems"
        );
        this.setPermissible("azurite.checkredeems");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "redeemscount",
                "redeemcount",
                "countredeems"
        );
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (String s : getLanguageConfig().getStringList("CHECKREDEEMS_COMMAND.FORMAT")) {
            if (!s.equalsIgnoreCase("%redeems%")) {
                sendMessage(sender, s);
                continue;
            }

            for (String string : getMiscConfig().getKeys(false)) {
                if (!string.endsWith("_REDEEM")) continue;

                String redeem = getMiscConfig().getString(string);
                String[] split = redeem.split(", ");
                String path = split[0];

                sendMessage(sender, getLanguageConfig().getString("CHECKREDEEMS_COMMAND.REDEEM_FORMAT")
                        .replace("%name%", getConfig().getString(path + "NAME"))
                        .replace("%count%", String.valueOf(Integer.parseInt(split[1])))
                );
            }
        }
    }
}