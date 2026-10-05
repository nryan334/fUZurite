package me.keano.azurite.modules.signs.subclaim;

import lombok.Getter;
import lombok.SneakyThrows;
import me.keano.azurite.modules.signs.CustomSign;
import me.keano.azurite.modules.signs.CustomSignManager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SubclaimSign extends CustomSign {

    @Getter
    private final int subclaimIndex, memberIndex;

    private final List<BlockFace> around; // Used to check around a sign for a chest
    private static Method GET_BLOCK_DATA = null;

    public SubclaimSign(CustomSignManager manager) {
        super(
                manager,
                manager.getConfig().getStringList("SIGNS_CONFIG.SUBCLAIM_SIGN.LINES")
        );
        this.subclaimIndex = getIndex("subclaim");
        this.memberIndex = getIndex("%member%");
        this.around = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
    }

    @Override
    public void onClick(Player player, Sign sign) {
    }

    @EventHandler
    @SneakyThrows
    public void onSign(SignChangeEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        Team atSign = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (!e.getLine(subclaimIndex).toLowerCase().contains("[subclaim]") ||
                e.getLine(memberIndex).isEmpty()) return;

        if (pt == null || pt != atSign) {
            block.breakNaturally();
            player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.SUBCLAIM_SIGNS.NOT_CREATABLE"));
            return;
        }

        Block attached;

        if (Utils.isModernVer()) {
            if (GET_BLOCK_DATA == null) GET_BLOCK_DATA = ReflectionUtils.accessMethod(BlockState.class, "getBlockData");
            WallSign wallSign = (WallSign) GET_BLOCK_DATA.invoke(block.getState());
            attached = block.getRelative(wallSign.getFacing().getOppositeFace());

        } else {
            org.bukkit.material.Sign bukkitSign = (org.bukkit.material.Sign) block.getState().getData();
            attached = block.getRelative(bukkitSign.getAttachedFace());
        }

        if (!attached.getType().name().contains("CHEST")) {
            block.breakNaturally();
            player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.SUBCLAIM_SIGNS.NOT_CHEST"));
            return;
        }

        if (getSubclaim(attached) != null) {
            block.breakNaturally();
            player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.SUBCLAIM_SIGNS.ALREADY_SUBCLAIMED"));
            return;
        }

        e.setLine(subclaimIndex, lines.get(subclaimIndex));
        player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.SUBCLAIM_SIGNS.CREATED_SUBCLAIM"));
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!block.getType().name().contains("CHEST")) return;

        Sign sign = getSubclaim(block);

        // Deny opening of subclaims
        if (sign != null && cannotUse(player, sign)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.SUBCLAIM_SIGNS.DENIED_OPEN"));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (block.getType().name().contains("CHEST") || block.getType().name().contains("SIGN")) {
            Sign sign = getSubclaim(block);

            // Deny breaking of subclaims
            if (sign != null && cannotUse(player, sign)) {
                e.setCancelled(true);
                player.sendMessage(getLanguageConfig().getString("CUSTOM_SIGNS.SUBCLAIM_SIGNS.DENIED_BREAK"));
            }
        }
    }

    @EventHandler
    public void onMove(InventoryMoveItemEvent e) {
        if (e.getSource().getType() != InventoryType.CHEST) return;
        if (e.getDestination().getType() != InventoryType.HOPPER) return;

        InventoryHolder holder = e.getSource().getHolder();
        Location loc = (holder instanceof DoubleChest ? ((DoubleChest) holder).getLocation() :
                holder instanceof BlockState ? ((BlockState) holder).getLocation() : null);

        // Deny movement of items if chest is subclaimed.
        if (loc != null && getSubclaim(loc.getBlock()) != null) {
            e.setCancelled(true);
        }
    }

    public boolean cannotUse(Player player, Sign sign) {
        Team team = getInstance().getTeamManager().getClaimManager().getTeam(sign.getLocation());

        // These hardly matter.
        if (team == null) return false;
        if (!(team instanceof PlayerTeam)) return false;
        if (!((PlayerTeam) team).getPlayers().contains(player.getUniqueId())) return false;
        if (sign.getLine(memberIndex).equals(player.getName())) return false;

        PlayerTeam pt = (PlayerTeam) team;

        // Allow opening if raidable.
        if (pt.isRaidable()) return false;

        // Co-Leaders can open any.
        return !pt.checkRole(player, Role.CO_LEADER);
    }

    private Sign getSubclaim(Block block) {
        Sign first = checkSign(block);

        // Use this to refrain checking below.
        if (first != null) return first;

        // Check for other chests.
        for (BlockFace blockFace : around) {
            Block next = block.getRelative(blockFace);

            // They are the same, now check that block
            if (next.getType() == block.getType()) {
                Sign sign = checkSign(next);

                if (sign != null) return sign;
            }
        }

        return null;
    }

    @SneakyThrows
    private Sign checkSign(Block block) {
        if (block.getType().name().contains("SIGN")) {
            Sign sign = (Sign) block.getState();
            return (sign.getLine(subclaimIndex).equals(lines.get(subclaimIndex)) ? sign : null);
        }

        for (BlockFace blockFace : around) {
            Block next = block.getRelative(blockFace);

            if (next.getType().name().contains("SIGN")) {
                if (Utils.isModernVer()) {
                    if (GET_BLOCK_DATA == null)
                        GET_BLOCK_DATA = ReflectionUtils.accessMethod(BlockState.class, "getBlockData");
                    Sign sign = (Sign) next.getState();
                    WallSign wallSign = (WallSign) GET_BLOCK_DATA.invoke(next.getState());
                    Block attached = next.getRelative(wallSign.getFacing().getOppositeFace());

                    // The sign is attached to this block and the sign does have subclaim message.
                    if (attached.getLocation().equals(block.getLocation()) &&
                            sign.getLine(subclaimIndex).equals(lines.get(subclaimIndex))) return sign;
                    return null;
                }

                Sign sign = (Sign) next.getState();
                org.bukkit.material.Sign bukkitSign = (org.bukkit.material.Sign) sign.getData();
                Block attached = next.getRelative(bukkitSign.getAttachedFace());

                // The sign is attached to this block and the sign does have subclaim message.
                if (attached.getLocation().equals(block.getLocation()) &&
                        sign.getLine(subclaimIndex).equals(lines.get(subclaimIndex))) return sign;
            }
        }

        return null;
    }
}