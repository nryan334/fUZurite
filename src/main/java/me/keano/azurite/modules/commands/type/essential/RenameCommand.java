package me.keano.azurite.modules.commands.type.essential;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class RenameCommand extends Command {

    private final List<Material> deniedItems;
    private final List<String> deniedNames;

    public RenameCommand(CommandManager manager) {
        super(
                manager,
                "rename"
        );

        this.deniedItems = new ArrayList<>();
        this.deniedNames = new ArrayList<>();

        this.setPermissible("azurite.rename");
        this.load();
    }

    private void load() {
        deniedNames.addAll(getConfig().getStringList("RENAMING.DENIED_NAMES"));
        deniedItems.addAll(getConfig().getStringList("RENAMING.DENIED_ITEMS")
                .stream().map(Material::valueOf).collect(Collectors.toList()));
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
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null) {
            sendMessage(sender, getLanguageConfig().getString("RENAME_COMMAND.EMPTY_HAND"));
            return;
        }

        if (deniedItems.contains(hand.getType())) {
            sendMessage(sender, getLanguageConfig().getString("RENAME_COMMAND.FORBIDDEN_ITEM"));
            return;
        }

        if (deniedNames.contains(name)) {
            sendMessage(sender, getLanguageConfig().getString("RENAME_COMMAND.FORBIDDEN_NAME"));
            return;
        }

        ItemBuilder builder = new ItemBuilder(hand);
        builder.setName(name);
        getManager().setItemInHand(player, builder.toItemStack());

        sendMessage(sender, getLanguageConfig().getString("RENAME_COMMAND.RENAMED")
                .replace("%name%", name)
        );
    }
}