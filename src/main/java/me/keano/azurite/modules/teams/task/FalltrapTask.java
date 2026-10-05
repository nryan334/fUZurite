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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class FalltrapTask extends BukkitRunnable {

    private final Manager manager;

    private final UUID player;
    private final Claim claim;
    private final ItemStack wall;
    private final List<Block> toChange;
    private final List<Block> toChangeWalls;
    private final int amountPerTick;

    private int outlineIndex;
    private int wallIndex;

    public FalltrapTask(Manager manager, UUID player, Claim claim, ItemStack wall) {
        this(manager, player, claim, wall, 0, 0);
    }

    public FalltrapTask(Manager manager, UUID player, Claim claim, ItemStack wall, int outlineIndex, int wallIndex) {
        this.manager = manager;
        this.player = player;
        this.claim = claim;
        this.wall = wall;
        this.toChange = new ArrayList<>();
        this.toChangeWalls = new ArrayList<>();
        this.amountPerTick = manager.getTeamConfig().getInt("FALLTRAP_CONFIG.AMOUNT_PER_TICK");
        this.outlineIndex = outlineIndex;
        this.wallIndex = wallIndex;
        this.load();
        this.runTaskTimerAsynchronously(manager.getInstance(), 0L, manager.getTeamConfig().getInt("FALLTRAP_CONFIG.TASK_DELAY"));
    }

    private void load() {
        for (Block block : claim) {
            toChange.add(block);
        }

        toChange.sort(Comparator.comparingInt(Block::getY).reversed());

        if (wall != null) {
            toChangeWalls.addAll(claim.getWalls(claim.getMinimumY(), claim.getMaximumY()));
            toChangeWalls.sort(Comparator.comparingInt(Block::getY).reversed());
        }
    }

    public void cancelTask() {
        this.cancel();
        toChangeWalls.clear();
        toChange.clear();

        User user = manager.getInstance().getUserManager().getByUUID(player);

        if (user.getActionBar() == ActionBar.FALLTRAP_TASK) {
            user.setActionBar(null);
        }
    }

    @Override
    public void run() {
        if (outlineIndex == toChange.size() && wallIndex == toChangeWalls.size()) {
            cancelTask();
            PlayerTeam pt = manager.getInstance().getTeamManager().getPlayerTeam(claim.getTeam());
            pt.getFalltrapTasks().remove(this);
            pt.save();
            return;
        }

        boolean shouldWalls = false;

        for (int i = 0; i < amountPerTick; i++) {
            if (outlineIndex < toChange.size()) {
                Block block = toChange.get(outlineIndex);

                if (block.getType() == Material.BEDROCK) continue;

                // Can't set blocks/data async
                Tasks.execute(getManager(), () -> block.setType(Material.AIR));

                // Sounds/Particles
                Config.FALLTRAP_PARTICLE.spawn(block.getWorld(), block.getLocation(), Config.FALLTRAP_PARTICLE_AMOUNT);
                getManager().playSound(block.getLocation(), Config.FALLTRAP_SOUND);

                // Index
                outlineIndex++;
                continue;
            }

            shouldWalls = true;
        }

        if (shouldWalls) {
            for (int i = 0; i < amountPerTick; i++) {
                if (wallIndex < toChangeWalls.size()) {
                    Block block = toChangeWalls.get(wallIndex);

                    if (block.getType() == Material.BEDROCK) continue;

                    // Can't set blocks/data async
                    Tasks.execute(getManager(), () -> {
                        block.setType(wall.getType());
                        getManager().setData(block, wall.getDurability());
                    });

                    // Sounds/Particles
                    Config.FALLTRAP_PARTICLE.spawn(block.getWorld(), block.getLocation(), Config.FALLTRAP_PARTICLE_AMOUNT);
                    getManager().playSound(block.getLocation(), Config.FALLTRAP_SOUND);

                    // Index
                    wallIndex++;
                }
            }
        }

        Player toSend = Bukkit.getPlayer(player);

        if (toSend != null) {
            User user = manager.getInstance().getUserManager().getByUUID(toSend.getUniqueId());
            ActionBar actionBar = user.getActionBar();

            if (manager.getInstance().getStaffManager().isStaffEnabled(toSend)) {
                if (actionBar == ActionBar.FALLTRAP_TASK) user.setActionBar(null);
                return;
            }

            if (actionBar == null) user.setActionBar(ActionBar.FALLTRAP_TASK);

            if (actionBar == ActionBar.FALLTRAP_TASK) {
                double cur = outlineIndex + wallIndex;
                double total = toChange.size() + toChangeWalls.size();

                String bar = Utils.getTaskProgressBar((long) cur, (long) total,
                        Config.FALLTRAP_ACTION_BAR_BARS, Config.FALLTRAP_ACTION_BAR_SYMBOL,
                        Config.FALLTRAP_ACTION_BAR_NO_COLOR, Config.FALLTRAP_ACTION_BAR_YES_COLOR
                );

                manager.getInstance().getVersionManager().getVersion().sendActionBar(toSend, Config.FALLTRAP_ACTION_BAR_STRING
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
        FalltrapTask that = (FalltrapTask) o;
        return claim.getTeam().equals(that.getClaim().getTeam());
    }

    @Override
    public int hashCode() {
        return Objects.hash(claim.getTeam());
    }
}