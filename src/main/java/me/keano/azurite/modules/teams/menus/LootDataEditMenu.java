package me.keano.azurite.modules.teams.menus;

import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.framework.menu.paginated.PaginatedMenu;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.extra.LootData;
import me.keano.azurite.modules.teams.extra.LootItem;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class LootDataEditMenu extends PaginatedMenu {

    private final LootData lootData;
    private final Team team;

    public LootDataEditMenu(MenuManager manager, Player player, LootData lootData, Team team) {
        super(
                manager,
                player,
                manager.getConfig().getString("MOUNTAINS.EDIT_LOOT_MENU.TITLE"),
                manager.getConfig().getInt("MOUNTAINS.EDIT_LOOT_MENU.SIZE"),
                true
        );
        this.lootData = lootData;
        this.team = team;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> map = new HashMap<>();
        int i = 1;

        for (LootItem lootItem : lootData.getLootItems()) {
            map.put(i++, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);

                    if (e.getClick() == ClickType.MIDDLE) {
                        lootData.getLootItems().remove(lootItem);
                        ItemUtils.giveItem(player, lootItem.getItem().clone(), player.getLocation());
                        update();
                        playSound(player, true);
                        return;
                    }

                    int modify = 0;

                    switch (e.getClick()) {
                        case SHIFT_LEFT:
                            modify = 10;
                            break;

                        case SHIFT_RIGHT:
                            modify = -10;
                            break;

                        case LEFT:
                            modify = 1;
                            break;

                        case RIGHT:
                            modify = -1;
                            break;
                    }

                    if (modify != 0) {
                        lootItem.setPercentage(lootItem.getPercentage() + modify);
                        team.save();
                        update();
                        playSound(player, false);
                    }
                }

                @Override
                public ItemStack getItemStack() {
                    ItemBuilder builder = new ItemBuilder(lootItem.getItem().clone());
                    List<String> newLore = new ArrayList<>();

                    for (String s : getConfig().getStringList("MOUNTAINS.EDIT_LOOT_MENU.LORE")) {
                        if (s.equalsIgnoreCase("%lore%")) {
                            newLore.addAll(builder.getLore());
                            continue;
                        }

                        newLore.add(s.replace("%chance%", Formatter.formatDtr(lootItem.getPercentage())));
                    }

                    return builder.setLore(newLore).toItemStack();
                }
            });
        }

        return map;
    }

    @Override
    public void onClickOwn(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        ItemStack click = e.getCurrentItem();

        if (click != null && click.getType() != Material.AIR) {
            e.setCancelled(true);
            e.setCurrentItem(null);
            lootData.getLootItems().add(new LootItem(click.clone(), 1.0D));
            update();
            playSound(player, false);
        }
    }

    private void playSound(Player player, boolean remove) {
        String sound = (remove ?
                getConfig().getString("MOUNTAINS.EDIT_LOOT_MENU.SOUND_REMOVE") :
                getConfig().getString("MOUNTAINS.EDIT_LOOT_MENU.SOUND_CLICK"));

        getManager().playSound(player, sound, false);
    }
}