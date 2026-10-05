package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.utils.Formatter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothSetRemArg extends Argument {

    public KothSetRemArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "setremaining",
                        "setrem"
                )
        );
        this.setPermissible("azurite.koth.setremaining");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_SETREM.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Koth koth = getInstance().getKothManager().getKoth(args[0]);
        Long remaining = Formatter.parse(args[1]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        if (remaining == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[1])
            );
            return;
        }

        if (!koth.isActive()) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_SETREM.NOT_ACTIVE"));
            return;
        }

        koth.setRemaining(remaining);
        koth.save();

        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_SETREM.UPDATED_REM")
                .replace("%koth%", koth.getName())
                .replace("%rem%", Formatter.getRemaining(remaining, false))
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getKothManager().getKoths().values()
                    .stream()
                    .filter(Koth::isActive)
                    .map(Koth::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}