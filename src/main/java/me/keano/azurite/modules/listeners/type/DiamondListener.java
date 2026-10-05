package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.pvpclass.type.miner.MinerClass;
import me.keano.azurite.modules.users.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class DiamondListener extends Module<ListenerManager> {

    private final List<BlockFace> faces;

    public DiamondListener(ListenerManager manager) {
        super(manager);
        this.faces = Arrays.asList(
                BlockFace.NORTH,
                BlockFace.SOUTH,
                BlockFace.EAST,
                BlockFace.WEST,
                BlockFace.NORTH_EAST,
                BlockFace.NORTH_WEST,
                BlockFace.SOUTH_EAST,
                BlockFace.SOUTH_WEST,
                BlockFace.UP,
                BlockFace.DOWN
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();

        if (block.getType() == Material.DIAMOND_ORE) {
            User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
            user.setDiamonds(user.getDiamonds() + 1); // increment diamonds
            user.save();

            MinerClass minerClass = getInstance().getClassManager().getMinerClass();

            if (minerClass != null && minerClass.getPlayers().contains(player.getUniqueId())) {
                minerClass.addEffects(player); // update the effects if they are in miner.
            }

            if (block.hasMetadata("exception")) {
                block.removeMetadata("exception", getInstance()); // remove metadata to clear ram
                return;
            }

            // Make sure we add it to the mined block aswell
            block.setMetadata("exception", new FixedMetadataValue(getInstance(), true));

            String message = Config.FOUND_DIAMOND
                    .replace("%player%", e.getPlayer().getName())
                    .replace("%amount%", String.valueOf(count(block)));

            for (Player online : Bukkit.getOnlinePlayers()) {
                User onlineUser = getInstance().getUserManager().getByUUID(online.getUniqueId());

                if (onlineUser.isFoundDiamondAlerts()) {
                    online.sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Block block = e.getBlockPlaced();

        if (block.getType() == Material.DIAMOND_ORE) {
            block.setMetadata("exception", new FixedMetadataValue(getInstance(), true));
        }
    }

    private int count(Block block) {
        int i = 1; // count the one mined

        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);

            if (relative.hasMetadata("exception")) continue;

            if (relative.getType() == Material.DIAMOND_ORE) {
                relative.setMetadata("exception", new FixedMetadataValue(getInstance(), true));
                i += count(relative); // count the relative of that block too and add and equal it.
            }
        }

        return i;
    }
}