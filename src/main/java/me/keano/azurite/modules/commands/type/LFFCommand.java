package me.keano.azurite.modules.commands.type;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LFFCommand extends Command {

    private static Cooldown cooldown;

    public LFFCommand(CommandManager manager) {
        super(
                manager,
                "lff"
        );
        cooldown = new Cooldown(manager);
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
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (cooldown.hasCooldown(player)) {
            sendMessage(sender, getLanguageConfig().getString("LFF_COMMAND.ON_COOLDOWN")
                    .replace("%seconds%", cooldown.getRemaining(player))
            );
            return;
        }

        if (pt != null) {
            sendMessage(sender, getLanguageConfig().getString("LFF_COMMAND.ALREADY_IN_TEAM"));
            return;
        }

        new LFFMenu(getInstance().getMenuManager(), player).open();
    }

    @Getter
    @Setter
    private static class LFFItem {

        private String name;
        private ItemStack disabled;
        private ItemStack enabled;
        private boolean toggled;
        private int slot;

        public LFFItem(String name, ItemStack disabled, ItemStack enabled, int slot) {
            this.name = name;
            this.disabled = disabled;
            this.enabled = enabled;
            this.slot = slot;
        }
    }

    private static class LFFMenu extends Menu {

        private final List<LFFItem> items;

        public LFFMenu(MenuManager manager, Player player) {
            super(
                    manager,
                    player,
                    manager.getLanguageConfig().getString("LFF_COMMAND.LFF_MENU.TITLE"),
                    manager.getLanguageConfig().getInt("LFF_COMMAND.LFF_MENU.SIZE"),
                    false
            );
            this.items = new ArrayList<>();
            this.load();
        }

        private void load() {
            for (String key : getLanguageConfig().getConfigurationSection("LFF_COMMAND.LFF_MENU.CLASSES").getKeys(false)) {
                String path = "LFF_COMMAND.LFF_MENU.CLASSES." + key + ".";
                ItemStack enabled = new ItemBuilder(ItemUtils.getMat(getLanguageConfig().getString(path + "ENABLED_ITEM.MATERIAL")))
                        .setName(getLanguageConfig().getString(path + "ENABLED_ITEM.NAME"))
                        .data(getManager(), getLanguageConfig().getInt(path + "ENABLED_ITEM.DATA"))
                        .setLore(getLanguageConfig().getStringList(path + "ENABLED_ITEM.LORE"))
                        .toItemStack();

                ItemStack disabled = new ItemBuilder(ItemUtils.getMat(getLanguageConfig().getString(path + "DISABLED_ITEM.MATERIAL")))
                        .setName(getLanguageConfig().getString(path + "DISABLED_ITEM.NAME"))
                        .data(getManager(), getLanguageConfig().getInt(path + "DISABLED_ITEM.DATA"))
                        .setLore(getLanguageConfig().getStringList(path + "DISABLED_ITEM.LORE"))
                        .toItemStack();

                items.add(new LFFItem(
                        getLanguageConfig().getString(path + "NAME"),
                        Utils.splitEnchantAdd(getLanguageConfig().getString(path + "DISABLED_ITEM.ENCHANT"), disabled),
                        Utils.splitEnchantAdd(getLanguageConfig().getString(path + "ENABLED_ITEM.ENCHANT"), enabled),
                        getLanguageConfig().getInt(path + "SLOT"))
                );
            }
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();

            for (LFFItem item : items) {
                buttons.put(item.getSlot(), new Button() {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        e.setCancelled(true);
                        item.setToggled(!item.isToggled());
                        update();
                    }

                    @Override
                    public ItemStack getItemStack() {
                        if (item.isToggled()) {
                            return item.getEnabled();

                        } else return item.getDisabled();
                    }
                });
            }

            buttons.put(getLanguageConfig().getInt("LFF_COMMAND.LFF_MENU.CONFIRM_BUTTON.SLOT"), new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    String[] array = items.stream()
                            .filter(LFFItem::isToggled)
                            .map(LFFItem::getName).toArray(String[]::new);

                    if (array.length == 0) {
                        getPlayer().sendMessage(getLanguageConfig().getString("LFF_COMMAND.NONE_CHOSEN"));
                        return;
                    }

                    cooldown.applyCooldown(getPlayer(), getConfig().getInt("TIMERS_COOLDOWN.LFF_COMMAND"));

                    for (String s : getLanguageConfig().getStringList("LFF_COMMAND.BROADCAST_MESSAGE")) {
                        Bukkit.broadcastMessage(s
                                .replace("%player%", getPlayer().getName())
                                .replace("%classes%", String.join(getLanguageConfig().getString("LFF_COMMAND.SEPARATOR"), array))
                        );
                    }

                    e.setCancelled(true);
                    player.closeInventory();
                }

                @Override
                public ItemStack getItemStack() {
                    return new ItemBuilder(ItemUtils.getMat(getLanguageConfig().getString("LFF_COMMAND.LFF_MENU.CONFIRM_BUTTON.MATERIAL")))
                            .setName(getLanguageConfig().getString("LFF_COMMAND.LFF_MENU.CONFIRM_BUTTON.NAME"))
                            .setLore(getLanguageConfig().getStringList("LFF_COMMAND.LFF_MENU.CONFIRM_BUTTON.LORE"))
                            .data(getManager(), getLanguageConfig().getInt("LFF_COMMAND.LFF_MENU.CONFIRM_BUTTON.DATA")).toItemStack();
                }
            });

            return buttons;
        }

        @Override
        public void onClose() {
            items.clear();
        }
    }
}