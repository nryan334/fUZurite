package me.keano.azurite.modules.blockshop.actions.type;

import me.keano.azurite.modules.blockshop.BlockshopManager;
import me.keano.azurite.modules.blockshop.actions.BlockshopAction;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BlockshopSellAllAction extends BlockshopAction {

    public BlockshopSellAllAction(BlockshopManager manager, String path) {
        super(manager, path);
    }

    @Override
    public boolean handleClick(Player player, InventoryClickEvent event) {
        Map<Material, Integer> priceMappings = new HashMap<>();
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        for (String key : getBlockshopConfig().getConfigurationSection(path.substring(0, path.length() - 1)).getKeys(false)) {
            Material material = ItemUtils.getMat(key);
            int price = getBlockshopConfig().getInt(path + key);
            priceMappings.put(material, price);
        }

        int sold = 0;

        for (Map.Entry<Material, Integer> entry : priceMappings.entrySet()) {
            sold += handleSell(player, user, new ItemStack(entry.getKey()), entry.getValue());
        }

        if (sold == 0) {
            player.sendMessage(getLanguageConfig().getString("BLOCKSHOP.SOLD_ALL_NOTHING"));
            return false;
        }

        player.sendMessage(getLanguageConfig().getString("BLOCKSHOP.SOLD_ALL")
                .replace("%price%", String.valueOf(sold))
        );
        return false;
    }

    private int handleSell(Player player, User user, ItemStack item, int price) {
        int amountItems = Utils.getAmountItems(getManager(), player, item);
        int finalPrice = (int) (price * (double) amountItems);

        if (amountItems == 0) {
            return 0;
        }

        user.setBalance(user.getBalance() + finalPrice);
        Utils.takeItems(getManager(), player, item, amountItems);
        return finalPrice;
    }
}