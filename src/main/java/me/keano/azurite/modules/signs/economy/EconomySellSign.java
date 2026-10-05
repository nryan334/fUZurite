package me.keano.azurite.modules.signs.economy;

import lombok.Getter;
import me.keano.azurite.modules.signs.CustomSignManager;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Utils;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class EconomySellSign extends EconomySign {

    private final int materialIndex, amountIndex, priceIndex;

    public EconomySellSign(CustomSignManager manager) {
        super(
                manager,
                manager.getConfig().getStringList("SIGNS_CONFIG.SELL_SIGN.LINES")
        );
        this.materialIndex = getIndex("%material%");
        this.amountIndex = getIndex("%amount%");
        this.priceIndex = getIndex("%price%");
    }

    @Override
    public void onClick(Player player, Sign sign) {
        ItemStack itemStack = getItemStack(sign.getLine(materialIndex));
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
        int amountItems = Utils.getAmountItems(getManager(), player, itemStack);
        int price = Integer.parseInt(sign.getLine(priceIndex).replace("$", ""));
        int amount = Integer.parseInt(sign.getLine(amountIndex));
        int finalPrice = (int) (((double) price / (double) amount) * (double) (Math.min(amountItems, amount)));

        if (amountItems == 0) {
            String[] clone = sign.getLines().clone();
            clone[materialIndex + 1] = getLanguageConfig().getString("CUSTOM_SIGNS.ECONOMY_SIGNS.INSUFFICIENT_BLOCKS");
            sendSignChange(player, sign, clone);
            return;
        }

        String[] clone = sign.getLines().clone();

        clone[materialIndex + 1] = getLanguageConfig().getString("CUSTOM_SIGNS.ECONOMY_SIGNS.SOLD");
        user.setBalance(user.getBalance() + finalPrice);
        user.save();

        sendSignChange(player, sign, clone);
        Utils.takeItems(getManager(), player, itemStack, amount);
    }
}