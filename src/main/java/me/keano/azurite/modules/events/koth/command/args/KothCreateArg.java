package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.utils.CC;
import org.bukkit.command.CommandSender;

import java.util.Collections;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothCreateArg extends Argument {

    public KothCreateArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "create"
                )
        );
        this.setPermissible("azurite.koth.create");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_CREATE.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sendUsage(sender);
            return;
        }

        String name = args[0];
        String color = CC.t(args[1]);
        Integer minutes = getInt(args[2]);
        Integer pointsReward = getInt(args[3]);

        if (minutes == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[2])
            );
            return;
        }

        if (pointsReward == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[3])
            );
            return;
        }

        if (getInstance().getKothManager().getKoth(name) != null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_CREATE.ALREADY_EXISTS"));
            return;
        }

        Koth koth = new Koth(getInstance().getKothManager(), name, color, pointsReward, minutes);
        koth.save();

        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_CREATE.CREATED")
                .replace("%koth%", koth.getName())
        );
    }
}