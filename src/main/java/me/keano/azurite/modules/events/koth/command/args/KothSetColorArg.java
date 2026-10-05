package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.utils.CC;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothSetColorArg extends Argument {

    public KothSetColorArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "setcolor"
                )
        );
        this.setPermissible("azurite.koth.setcolor");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_SETCOLOR.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Koth koth = getInstance().getKothManager().getKoth(args[0]);
        String color = CC.t(args[1]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        koth.setColor(color);
        koth.save();

        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_SETCOLOR.UPDATED_COLOR")
                .replace("%koth%", koth.getName())
                .replace("%color%", color)
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getKothManager().getKoths().values()
                    .stream()
                    .map(Koth::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}