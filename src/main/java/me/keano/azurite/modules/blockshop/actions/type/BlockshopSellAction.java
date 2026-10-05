package me.keano.azurite.modules.blockshop.actions.type;

import me.keano.azurite.modules.blockshop.BlockshopManager;
import me.keano.azurite.modules.blockshop.actions.BlockshopAction;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BlockshopSellAction extends BlockshopAction {

    public BlockshopSellAction(BlockshopManager manager, String path) {
        super(manager, path);
    }

    @Override
    public boolean handleClick(Player player, InventoryClickEvent event) {
        ItemStack selling = getManager().loadItem(player, path);
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        int amountItems = Utils.getAmountItems(getManager(), player, selling);
        int price = getBlockshopConfig().getInt(path + "PRICE");
        int amount = getBlockshopConfig().getInt(path + "AMOUNT");
        int finalPrice = (int) (((double) price / (double) amount) * (double) (Math.min(amountItems, amount)));

        if (amountItems == 0) {
            player.sendMessage(getLanguageConfig().getString("BLOCKSHOP.NOT_CARRYING")
                    .replace("%type%", ItemUtils.getItemName(selling))
            );
            return false;
        }

        user.setBalance(user.getBalance() + finalPrice);
        user.save();

        Utils.takeItems(getManager(), player, selling, amount);

        player.sendMessage(getLanguageConfig().getString("BLOCKSHOP.SOLD")
                .replace("%amount%", String.valueOf(Math.min(amountItems, amount)))
                .replace("%price%", String.valueOf(finalPrice))
                .replace("%type%", ItemUtils.getItemName(selling))
        );
        return true;
    }
}