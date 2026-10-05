package me.keano.azurite.modules.blockshop.actions.type;

import me.keano.azurite.modules.blockshop.BlockshopManager;
import me.keano.azurite.modules.blockshop.actions.BlockshopAction;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BlockshopBuyAction extends BlockshopAction {

    public BlockshopBuyAction(BlockshopManager manager, String path) {
        super(manager, path);
    }

    @Override
    public boolean handleClick(Player player, InventoryClickEvent event) {
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        int price = getBlockshopConfig().getInt(path + "PRICE");

        if (user.getBalance() < price) {
            player.sendMessage(getLanguageConfig().getString("BLOCKSHOP.INSUFFICIENT_MONEY")
                    .replace("%price%", String.valueOf(price))
            );

            if (getBlockshopConfig().contains(path + "SOUND_NO_MONEY")) {
                getManager().playSound(player, getBlockshopConfig().getString(path + "SOUND_NO_MONEY"), false);
                return false;
            }
            return true;
        }

        user.setBalance(user.getBalance() - price);
        user.save();

        ItemStack buying = getManager().loadItem(player, path);
        ItemUtils.giveItem(player, buying, player.getLocation());

        player.sendMessage(getLanguageConfig().getString("BLOCKSHOP.BOUGHT_BLOCK")
                .replace("%price%", String.valueOf(price))
                .replace("%type%", ItemUtils.getItemName(buying))
        );
        return true;
    }
}