package me.keano.azurite.modules.signs.kitmap;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.signs.CustomSign;
import me.keano.azurite.modules.signs.CustomSignManager;
import me.keano.azurite.modules.signs.kitmap.menu.QuickRefillMenu;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class QuickRefillSign extends CustomSign {

    private Cooldown cooldown;

    public QuickRefillSign(CustomSignManager manager) {
        super(
                manager,
                manager.getConfig().getStringList("SIGNS_CONFIG.QUICK_REFILL_SIGN.LINES")
        );
        this.cooldown = new Cooldown(manager);
    }

    @Override
    public void onClick(Player player, Sign sign) {
        if (cooldown.hasCooldown(player)) {
            player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.QUICK_REFILL_SIGN.COOLDOWN")
                    .replace("%time%", cooldown.getRemaining(player))
            );
            return;
        }

        new QuickRefillMenu(getInstance().getMenuManager(), player).open();
        cooldown.applyCooldown(player, getConfig().getInt("SIGNS_CONFIG.QUICK_REFILL_SIGN.COOLDOWN"));
    }
}