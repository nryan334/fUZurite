package me.keano.azurite.modules.versions;

import me.keano.azurite.modules.loggers.Logger;
import me.keano.azurite.modules.tablist.extra.TablistSkin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public interface Version {

    CommandMap getCommandMap();

    Set<Player> getTrackedPlayers(Player player);

    ItemStack getItemInHand(Player player);

    ItemStack addGlow(ItemStack itemStack);

    String getTPSColored();

    boolean isNotGapple(ItemStack item);

    int getPing(Player player);

    void setItemInHand(Player player, ItemStack item);

    void handleLoggerDeath(Logger logger);

    void playEffect(Location location, String effect, Object data);

    void hideArmor(Player player);

    void showArmor(Player player);

    void handleNettyListener(Player player);

    void sendActionBar(Player player, String string);

    void sendToServer(Player player, String server);

    void damageItemDefault(Player player, ItemStack hand);

    void clearArrows(Player player);

    List<ItemStack> getBlockDrops(Player player, Block bl, ItemStack hand);

    TablistSkin getSkinData(Player player);
}