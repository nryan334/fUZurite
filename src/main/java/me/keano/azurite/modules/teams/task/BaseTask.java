package me.keano.azurite.modules.teams.task;

import lombok.Getter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.users.settings.ActionBar;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.cuboid.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class BaseTask extends BukkitRunnable {

    private final Manager manager;

    private final UUID player;
    private final Claim claim;
    private final ItemStack wall;
    private final ItemStack outline;
    private final List<Block> outlineBlocks;
    private final List<Block> outlineWalls;

    private int outlineIndex;
    private int wallIndex;

    public BaseTask(Manager manager, UUID player, Claim claim, ItemStack wall, ItemStack outline) {
        this(manager, player, claim, wall, outline, 0, 0);
    }

    public BaseTask(Manager manager, UUID player, Claim claim, ItemStack wall, ItemStack outline, int outlineIndex, int wallIndex) {
        this.manager = manager;
        this.player = player;
        this.claim = claim;
        this.wall = wall;
        this.outline = outline;
        this.outlineBlocks = new ArrayList<>();
        this.outlineWalls = new ArrayList<>();
        this.outlineIndex = outlineIndex;
        this.wallIndex = wallIndex;
        this.load();
        this.runTaskTimerAsynchronously(manager.getInstance(), 0L, manager.getTeamConfig().getInt("BASE_CONFIG.TASK_DELAY"));
    }

    private void load() {
        Block[] cornerBlocks = claim.getCornerBlocks();

        // Outlines
        for (Block cornerBlock : cornerBlocks) {
            for (int i = 1; i <= claim.getMaximumY() - claim.getMinimumY(); i++) {
                Block block = claim.getWorld().getBlockAt(cornerBlock.getX(), cornerBlock.getY() + i, cornerBlock.getZ());
                outlineBlocks.add(block);
            }
        }

        // Outlines/Walls
        for (Block wall : claim.getWalls(claim.getMinimumY(), claim.getMaximumY())) {
            if (wall.getY() == claim.getMinimumY() && !outlineBlocks.contains(wall)) {
                outlineBlocks.add(wall);
                continue;
            }

            if (wall.getY() == claim.getMaximumY() && !outlineBlocks.contains(wall)) {
                outlineBlocks.add(wall);
                continue;
            }

            if (!outlineBlocks.contains(wall)) {
                outlineWalls.add(wall);
            }
        }

        // Floor
        if (manager.getTeamConfig().getBoolean("BASE_CONFIG.SET_FLOOR_TO_OUTLINE")) {
            Cuboid floor = new Cuboid(claim.getWorldName(), claim.getX1(), claim.getMinimumY(), claim.getZ1(), claim.getX2(), claim.getMinimumY(), claim.getZ2());

            for (Block block : floor) {
                if (!outlineWalls.contains(block)) {
                    outlineBlocks.add(block);
                }
            }
        }

        // Roof
        Cuboid roof = new Cuboid(claim.getWorldName(), claim.getX1(), claim.getMaximumY(), claim.getZ1(), claim.getX2(), claim.getMaximumY(), claim.getZ2());

        for (Block block : roof) {
            if (!outlineBlocks.contains(block)) {
                outlineWalls.add(block);
            }
        }
    }

    public void cancelTask() {
        cancel();
        outlineWalls.clear();
        outlineBlocks.clear();

        User user = manager.getInstance().getUserManager().getByUUID(player);

        if (user.getActionBar() == ActionBar.BASE_TASK) {
            user.setActionBar(null);
        }
    }

    @Override
    public void run() {
        if (outlineIndex == outlineBlocks.size() && wallIndex == outlineWalls.size()) {
            cancelTask();
            PlayerTeam pt = manager.getInstance().getTeamManager().getPlayerTeam(claim.getTeam());
            pt.getBaseTasks().remove(this);
            pt.save();
            return;
        }

        boolean shouldWalls = false;

        if (outlineIndex < outlineBlocks.size()) {
            Block block = outlineBlocks.get(outlineIndex);
            String name = block.getType().name();

            if (!name.contains("CHEST") && !name.contains("FENCE") && !name.contains("BEDROCK")) {
                // We can't set blocks/data async
                Tasks.execute(getManager(), () -> {
                    block.setType(outline.getType());
                    getManager().setData(block, outline.getDurability());
                });

                // Sounds/Particles
                Config.BASE_PARTICLE.spawn(block.getWorld(), block.getLocation(), Config.BASE_PARTICLE_AMOUNT);
                getManager().playSound(block.getLocation(), Config.BASE_SOUND);
            }

            // Index
            outlineIndex++;
        } else {
            shouldWalls = true;
        }

        if (shouldWalls) {
            if (wallIndex < outlineWalls.size()) {
                Block block = outlineWalls.get(wallIndex);
                String name = block.getType().name();

                if (!name.contains("CHEST") && !name.contains("FENCE") && !name.contains("BEDROCK")) {
                    // We can't set blocks/data async
                    Tasks.execute(getManager(), () -> {
                        block.setType(wall.getType());
                        getManager().setData(block, wall.getDurability());
                    });

                    // Sounds/Particles
                    Config.BASE_PARTICLE.spawn(block.getWorld(), block.getLocation(), Config.BASE_PARTICLE_AMOUNT);
                    getManager().playSound(block.getLocation(), Config.BASE_SOUND);
                }

                // Index
                wallIndex++;
            }
        }

        Player toSend = Bukkit.getPlayer(player);

        if (toSend != null) {
            User user = manager.getInstance().getUserManager().getByUUID(toSend.getUniqueId());
            ActionBar actionBar = user.getActionBar();

            if (manager.getInstance().getStaffManager().isStaffEnabled(toSend)) {
                if (actionBar == ActionBar.BASE_TASK) user.setActionBar(null);
                return;
            }

            if (actionBar == null) user.setActionBar(ActionBar.BASE_TASK);

            if (actionBar == ActionBar.BASE_TASK) {
                double cur = outlineIndex + wallIndex;
                double total = outlineBlocks.size() + outlineWalls.size();

                String bar = Utils.getTaskProgressBar((long) cur, (long) total,
                        Config.BASE_ACTION_BAR_BARS, Config.BASE_ACTION_BAR_SYMBOL,
                        Config.BASE_ACTION_BAR_NO_COLOR, Config.BASE_ACTION_BAR_YES_COLOR
                );

                manager.getInstance().getVersionManager().getVersion().sendActionBar(toSend, Config.BASE_ACTION_BAR_STRING
                        .replace("%actionbar%", bar)
                        .replace("%percent%", Formatter.formatBardEnergy((cur / total) * 100))
                );
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseTask baseTask = (BaseTask) o;
        return claim.getTeam().equals(baseTask.getClaim().getTeam());
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim.getTeam());
    }
}