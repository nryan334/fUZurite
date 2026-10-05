package me.keano.azurite.modules.events.koth.command.args;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothEditLootArg extends Argument {

    public KothEditLootArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "editloot"
                )
        );
        this.setPermissible("azurite.koth.editloot");
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_EDITLOOT.USAGE");
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
        Koth koth = getInstance().getKothManager().getKoth(args[0]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        new KothEditLootMenu(getInstance().getMenuManager(), player, koth).open();
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

    private static class KothEditLootMenu extends Menu {

        private final Koth koth;

        public KothEditLootMenu(MenuManager manager, Player player, Koth koth) {
            super(
                    manager,
                    player,
                    manager.getLanguageConfig().getString("KOTH_COMMAND.KOTH_EDITLOOT.TITLE"),
                    manager.getLanguageConfig().getInt("KOTH_COMMAND.KOTH_EDITLOOT.SIZE"),
                    false
            );
            this.koth = koth;
            this.setAllowInteract(true);
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();

            int i = 1;

            for (ItemStack itemStack : koth.getLoot()) {
                buttons.put(i, new Button() {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                    }

                    @Override
                    public ItemStack getItemStack() {
                        return itemStack;
                    }
                });
                i++;
            }

            return buttons;
        }

        @Override
        public void onClose() {
            koth.getLoot().clear();

            for (ItemStack content : getInventory().getContents()) {
                if (content == null) continue;
                koth.getLoot().add(content);
            }

            koth.save();
            getPlayer().sendMessage(getLanguageConfig().getString("KOTH_COMMAND.KOTH_EDITLOOT.SAVED"));
        }
    }
}