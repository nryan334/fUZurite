package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ClearLagCommand extends Command {

    public ClearLagCommand(CommandManager manager) {
        super(
                manager,
                "clearlag"
        );
        this.setPermissible("azurite.clearlag");
    }

    @Override
    public List<String> aliases() {
        return null;
    }

    @Override
    public List<String> usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {

            }
        }
    }
}