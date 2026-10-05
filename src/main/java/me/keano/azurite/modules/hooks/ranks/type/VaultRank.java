package me.keano.azurite.modules.hooks.ranks.type;

import me.keano.azurite.modules.hooks.ranks.Rank;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class VaultRank implements Rank {

    private final Chat chat;

    public VaultRank() {
        RegisteredServiceProvider<Chat> provider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        this.chat = (provider != null ? provider.getProvider() : null);
    }

    @Override
    public String getRankName(Player player) {
        return (chat != null ? chat.getPrimaryGroup(player) : "");
    }

    @Override
    public String getRankPrefix(Player player) {
        return (chat != null ? chat.getPlayerPrefix(player) : "");
    }

    @Override
    public String getRankSuffix(Player player) {
        return (chat != null ? chat.getPlayerSuffix(player) : "");
    }

    @Override
    public String getRankColor(Player player) {
        return "";
    }
}