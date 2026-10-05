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
public class ElevatorUpSign extends CustomSign {

    private final int elevatorIndex, upIndex;

    public ElevatorUpSign(CustomSignManager manager) {
        super(
                manager,
                manager.getConfig().getStringList("SIGNS_CONFIG.UP_SIGN.LINES")
        );

        this.elevatorIndex = getIndex("elevator");
        this.upIndex = getIndex("up");
    }

    @Override
    public void onClick(Player player, Sign sign) {
        Location start = sign.getLocation();
        Location playerLoc = player.getLocation();
        Block startBlock = start.getBlock();
        Block targetBlock = (getInstance().getVersionManager().getVersion() instanceof Version1_7_R4 ?
                player.getTargetBlock((HashSet<Byte>) null, 10) :
                player.getTargetBlock((Set<Material>) null, 10));

        // Deny sign glitching
        if (getInstance().getGlitchListener().getHitCooldown().hasCooldown(player) &&
                targetBlock != null && !targetBlock.getType().name().contains("SIGN")) return;

        if (startBlock.getRelative(BlockFace.UP).getType() == Material.AIR &&
                startBlock.getRelative(BlockFace.UP, 2).getType() == Material.AIR) {
            player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.ELEVATOR_SIGNS.INVALID_SIGN"));
            return;
        }

        for (int i = start.getBlockY() + 1; i <= start.getWorld().getMaxHeight(); i++) {
            if (i == start.getWorld().getMaxHeight()) {
                player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.ELEVATOR_SIGNS.CANNOT_FIND_LOCATION"));
                break;
            }

            Block block = start.getWorld().getBlockAt(start.getBlockX(), i, start.getBlockZ());
            Block top = block.getRelative(BlockFace.UP);

            if (isPassable(block.getType()) && isPassable(top.getType())) {
                Location topLoc = block.getLocation().add(0.5, 0, 0.5);

                topLoc.setYaw(playerLoc.getYaw());
                topLoc.setPitch(playerLoc.getPitch()); // set the yaw so they don't change direction.

                player.teleport(topLoc);
                break; // cancel looping
            }
        }
    }
}