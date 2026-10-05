package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.utils.BukkitSerialization;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class EditMenuCommand extends Command {

    public EditMenuCommand(CommandManager manager) {
        super(
                manager,
                "editmenu"
        );
        this.completions.add(new TabCompletion(Arrays.stream(MenuType.values())
                .map(MenuType::name).collect(Collectors.toList()), 0, "azurite.editmenu"));
        this.setPermissible("azurite.editmenu");
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "kitmenu",
                "menu"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("EDIT_MENU_COMMAND.USAGE");
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
        MenuType type;

        try {

            type = MenuType.valueOf(args[0]);

        } catch (IllegalArgumentException e) {
            sendMessage(sender, getLanguageConfig().getString("EDIT_MENU_COMMAND.MENU_NOT_FOUND")
                    .replace("%menu%", args[0])
            );
            return;
        }

        new EditMenu(getInstance().getMenuManager(), player, type).open();
    }

    private enum MenuType {

        QUICK_REFILL,
        REFILL

    }

    private static class EditMenu extends Menu {

        private final MenuType type;

        public EditMenu(MenuManager manager, Player player, MenuType type) {
            super(
                    manager,
                    player,
                    (type == MenuType.REFILL ?
                            manager.getConfig().getString("SIGNS_CONFIG.REFILL_SIGN.MENU_TITLE") :
                            manager.getConfig().getString("SIGNS_CONFIG.QUICK_REFILL_SIGN.MENU_TITLE")),
                    (type == MenuType.REFILL ?
                            manager.getConfig().getInt("SIGNS_CONFIG.REFILL_SIGN.MENU_SIZE") :
                            manager.getConfig().getInt("SIGNS_CONFIG.QUICK_REFILL_SIGN.MENU_SIZE")),
                    false
            );
            this.type = type;
            this.setAllowInteract(true);
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();
            ItemStack[] current = (type == MenuType.REFILL ? Config.REFILL_SIGN : Config.QUICK_REFILL_SIGN);

            for (int i = 1; i <= current.length; i++) {
                int copy = i;
                buttons.put(i, new Button() {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        // Empty
                    }

                    @Override
                    public ItemStack getItemStack() {
                        return current[copy - 1];
                    }
                });
            }

            return buttons;
        }

        @Override
        public void onClose() {
            String toSave = BukkitSerialization.itemStackArrayToBase64(Arrays.stream(getInventory().getContents())
                    .filter(Objects::nonNull)
                    .toArray(ItemStack[]::new));

            switch (type) {
                case REFILL:
                    getMiscConfig().set("REFILL_SIGN", toSave);
                    break;

                case QUICK_REFILL:
                    getMiscConfig().set("QUICK_REFILL", toSave);
                    break;
            }

            getMiscConfig().save();
            getMiscConfig().reload();
            getMiscConfig().reloadCache();
            Config.load(getInstance().getConfigsObject(), true); // We need to reload the config file / items cached in Config
            getPlayer().sendMessage(getLanguageConfig().getString("EDIT_MENU_COMMAND.SAVED_MENU"));
        }
    }
}