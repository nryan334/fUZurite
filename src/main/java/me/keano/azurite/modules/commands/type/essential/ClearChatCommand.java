package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ClearChatCommand extends Command {

    private final String clearString; // we can cache instead of creating each time.

    public ClearChatCommand(CommandManager manager) {
        super(
                manager,
                "clearchat"
        );

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= 300; i++) builder.append(CC.t("&7 &d &f &3 &b &9 &6 \n"));

        this.clearString = builder.toString();
        this.setPermissible("azurite.clearchat");
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList(
                "cc"
        );
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Bukkit.broadcastMessage(clearString);
    }
}