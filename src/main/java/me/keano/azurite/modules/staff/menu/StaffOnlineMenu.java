package me.keano.azurite.modules.staff.menu;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.framework.menu.paginated.PaginatedMenu;
import me.keano.azurite.modules.hooks.ranks.RankHook;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class StaffOnlineMenu extends PaginatedMenu {

    public StaffOnlineMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getConfig().getString("STAFF_MODE.STAFF_ONLINE_MENU.TITLE"),
                manager.getConfig().getInt("STAFF_MODE.STAFF_ONLINE_MENU.SIZE"),
                true
        );
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> map = new HashMap<>();
        int i = 1;
        List<Player> onlineStaff = Bukkit.getOnlinePlayers().stream()
                .filter(online -> online.hasPermission("azurite.staff"))
                .collect(Collectors.toList());

        for (Player staff : onlineStaff) {
            map.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                    player.chat(getConfig().getString("STAFF_MODE.STAFF_ONLINE_MENU.STAFF_HEAD.COMMAND")
                            .replace("%player%", staff.getName())
                    );
                }

                @Override
                public ItemStack getItemStack() {
                    RankHook rankHook = getInstance().getRankHook();
                    User user = getInstance().getUserManager().getByUUID(staff.getUniqueId());
                    boolean staffMode = getInstance().getStaffManager().isStaffEnabled(staff);
                    boolean vanish = getInstance().getStaffManager().isVanished(staff);

                    UnaryOperator<String> replacer = s -> s
                            .replace("%playtime%", Formatter.formatDetailed(user.getUpdatedPlaytime())) // Use calculated time as playtime freezes when in staff mode
                            .replace("%staff%", staffMode ? Config.STAFF_TRUE_PLACEHOLDER : Config.STAFF_FALSE_PLACEHOLDER)
                            .replace("%vanish%", vanish ? Config.STAFF_TRUE_PLACEHOLDER : Config.STAFF_FALSE_PLACEHOLDER)
                            .replace("%player%", staff.getName())
                            .replace("%rank-prefix%", rankHook.getRankPrefix(staff))
                            .replace("%rank-suffix%", rankHook.getRankPrefix(staff))
                            .replace("%rank-name%", rankHook.getRankName(staff))
                            .replace("%color%", rankHook.getRankColor(staff));

                    return new ItemBuilder(ItemUtils.getMatItem(getConfig().getString("STAFF_MODE.STAFF_ONLINE_MENU.STAFF_HEAD.MATERIAL")))
                            .setName(replacer.apply(getConfig().getString("STAFF_MODE.STAFF_ONLINE_MENU.STAFF_HEAD.NAME")))
                            .setLore(getConfig().getStringList("STAFF_MODE.STAFF_ONLINE_MENU.STAFF_HEAD.LORE").stream().map(replacer).collect(Collectors.toList()))
                            .data(getManager(), getConfig().getInt("STAFF_MODE.STAFF_ONLINE_MENU.STAFF_HEAD.DATA"))
                            .setSkullOwner(staff.getName())
                            .toItemStack();
                }
            });
            i++;
        }

        return map;
    }
}