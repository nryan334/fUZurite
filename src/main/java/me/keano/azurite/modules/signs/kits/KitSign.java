package me.keano.azurite.modules.signs.kits;

import lombok.Getter;
import me.keano.azurite.modules.kits.Kit;
import me.keano.azurite.modules.signs.CustomSign;
import me.keano.azurite.modules.signs.CustomSignManager;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class KitSign extends CustomSign {

    private final int kitIndex, kitTypeIndex;

    public KitSign(CustomSignManager manager) {
        super(
                manager,
                manager.getConfig().getStringList("SIGNS_CONFIG.KIT_SIGN.LINES")
        );
        this.kitIndex = getIndex("kit");
        this.kitTypeIndex = getIndex("%kit%");
    }

    @Override
    public void onClick(Player player, Sign sign) {
        Kit kit = getInstance().getKitManager().getKit(sign.getLine(kitTypeIndex));

        if (kit == null) {
            player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.KIT_SIGNS.KIT_NOT_FOUND"));
            return;
        }

        if (kit.getCooldown().hasCooldown(player)) {
            player.sendMessage(getLanguageConfig().getString("KIT_COMMAND.ON_COOLDOWN")
                    .replace("%time%", kit.getCooldown().getRemaining(player))
            );
            return;
        }

        if (kit.checkConfirmation(player)) return;

        kit.equip(player);
        player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.KIT_SIGNS.EQUIPPED")
                .replace("%kit%", kit.getName())
        );
    }
}