package me.keano.azurite.modules.staff.menu;

import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.framework.menu.paginated.PaginatedMenu;
import me.keano.azurite.modules.staff.extra.StaffReport;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ReportsMenu extends PaginatedMenu {

    public ReportsMenu(MenuManager manager, Player player) {
        super(
                manager,
                player,
                manager.getConfig().getString("STAFF_MODE.REPORTS_MENU.TITLE"),
                manager.getConfig().getInt("STAFF_MODE.REPORTS_MENU.SIZE"),
                true
        );
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int i = 1;

        for (StaffReport report : getInstance().getStaffManager().getReports()) {
            String reported = getInstance().getUserManager().getByUUID(report.getReported()).getName();
            String reporter = getInstance().getUserManager().getByUUID(report.getPlayer()).getName();

            buttons.put(i, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                    player.chat(getConfig().getString("STAFF_MODE.REPORTS_MENU.REPORT_FORMAT.COMMAND")
                            .replace("%reported%", reported)
                            .replace("%player%", reporter)
                            .replace("%reason%", report.getReason())
                    );
                }

                @Override
                public ItemStack getItemStack() {
                    List<String> lore = getConfig().getStringList("STAFF_MODE.REPORTS_MENU.REPORT_FORMAT.LORE");
                    String name = getConfig().getString("STAFF_MODE.REPORTS_MENU.REPORT_FORMAT.NAME");

                    lore.replaceAll(s -> s
                            .replace("%reported%", reported)
                            .replace("%player%", reporter)
                            .replace("%reason%", report.getReason())
                    );

                    name = name
                            .replace("%reported%", reported)
                            .replace("%player%", reporter)
                            .replace("%reason%", report.getReason());

                    return new ItemBuilder(ItemUtils.getMat(getConfig().getString("STAFF_MODE.REPORTS_MENU.REPORT_FORMAT.MATERIAL")))
                            .setName(name)
                            .setLore(lore)
                            .data(getManager(), (byte) getConfig().getInt("STAFF_MODE.REPORTS_MENU.REPORT_FORMAT.DATA"))
                            .toItemStack();
                }
            });
            i++;
        }

        return buttons;
    }
}