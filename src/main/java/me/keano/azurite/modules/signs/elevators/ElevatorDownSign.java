package me.keano.azurite.modules.signs.elevators;

import lombok.Getter;
import me.keano.azurite.modules.signs.CustomSign;
import me.keano.azurite.modules.signs.CustomSignManager;
import me.keano.azurite.modules.versions.type.Version1_7_R4;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class ElevatorDownSign extends CustomSign {

    public ElevatorDownSign(CustomSignManager manager) {
        super(
                manager,
                manager.getConfig().getStringList("SIGNS_CONFIG.DOWN_SIGN.LINES")
        );
    }

    @Override
    public void onClick(Player player, Sign sign) {
        Location start = sign.getLocation();
        Location playerLoc = player.getLocation();
        Block startBlock = start.getBlock();
        Block targetBlock = (getInstance().getVersionManager().getVersion() instanceof Version1_7_R4 ?
                player.getTargetBlock((HashSet<Byte>) null, 10) :
                player.getTargetBlock((Set<Material>) null, 10));

        // Deny those filthy sign glitchers!
        if (getInstance().getGlitchListener().getHitCooldown().hasCooldown(player)) return;
        if (targetBlock != null && !targetBlock.getType().name().contains("SIGN")) return;

        if (startBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR &&
                startBlock.getRelative(BlockFace.DOWN, 2).getType() == Material.AIR) {
            player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.ELEVATOR_SIGNS.INVALID_SIGN"));
            return;
        }

        for (int i = startBlock.getY(); i >= 0; i--) {
            if (i == 0) {
                player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.ELEVATOR_SIGNS.CANNOT_FIND_LOCATION"));
                break;
            }

            Block block = start.getWorld().getBlockAt(start.getBlockX(), i, start.getBlockZ());
            Block down = block.getRelative(BlockFace.DOWN);

            if (isPassable(block.getType()) && isPassable(down.getType())) {
                Location topLoc = down.getLocation().add(0.5, 0, 0.5);

                topLoc.setYaw(playerLoc.getYaw());
                topLoc.setPitch(playerLoc.getPitch()); // set the yaw so they don't change direction.

                player.teleport(topLoc);
                break; // cancel looping
            }
        }
    }
}