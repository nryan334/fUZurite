package me.keano.azurite.modules.balance;

import me.keano.azurite.HCF;
import me.keano.azurite.modules.balance.type.VaultBalance;
import me.keano.azurite.modules.framework.Manager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BalanceManager extends Manager {

    public BalanceManager(HCF instance) {
        super(instance);

        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");

        if (vault != null && vault.isEnabled()) {
            Bukkit.getServicesManager().register(Economy.class, new VaultBalance(this), getInstance(), ServicePriority.Normal);
        }
    }
}