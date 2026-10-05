package me.keano.azurite.modules.teams.type;

import lombok.Getter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class WarzoneTeam extends Team {

    public WarzoneTeam(TeamManager manager) {
        super(
                manager,
                "Warzone",
                UUID.randomUUID(),
                true,
                TeamType.WARZONE
        );
    }

    @Override
    public String getDisplayName(Player player) {
        return Config.DISPLAY_NAME_WARZONE.replace("%team%", super.getDisplayName(player));
    }

    public boolean canInteract(Location location) {
        Block block = location.getBlock();
        return block.getType() == ItemUtils.getMat("WOOD_PLATE") || block.getType() == ItemUtils.getMat("STONE_PLATE") ||
                block.getType().name().contains("FENCE_GATE");
    }

    public boolean canBreak(Location location) {
        int x = Math.abs(location.getBlockX());
        int z = Math.abs(location.getBlockZ());

        // basically just checks if it is in a cube of warzone break.
        if (location.getWorld().getEnvironment() == World.Environment.NORMAL) {
            return x > Config.WARZONE_BREAK || z > Config.WARZONE_BREAK;

        } else if (location.getWorld().getEnvironment() == World.Environment.NETHER) {
            return x > Config.WARZONE_BREAK_NETHER || z > Config.WARZONE_BREAK_NETHER;
        }

        return false;
    }
}