package me.keano.azurite.modules.teams.commands.team.args.co_leader;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.menus.TeamFalltrapMenu;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamFalltrapCommand extends Argument {

    private final Map<UUID, PlayerClaim> claimMap;
    private final Cooldown cooldown;
    private final ItemStack claimWand;

    public TeamFalltrapCommand(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "falltrap"
                )
        );
        this.claimMap = new HashMap<>();
        this.cooldown = new Cooldown(manager);
        this.claimWand = new ItemBuilder(ItemUtils.getMat(manager.getTeamConfig().getString("FALLTRAP_CONFIG.FALLTRAP_WAND.TYPE")))
                .setName(manager.getTeamConfig().getString("FALLTRAP_CONFIG.FALLTRAP_WAND.NAME"))
                .setLore(manager.getTeamConfig().getStringList("FALLTRAP_CONFIG.FALLTRAP_WAND.LORE"))
                .toItemStack();
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.PLAYER_ONLY);
            return;
        }

        Player player = (Player) sender;
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

        if (pt == null) {
            sendMessage(sender, Config.NOT_IN_TEAM);
            return;
        }

        if (!pt.checkRole(player, Role.CO_LEADER)) {
            sendMessage(sender, Config.INSUFFICIENT_ROLE
                    .replace("%role%", Role.CO_LEADER.getName())
            );
            return;
        }

        if (user.getFalltrapTokens() == 0) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FALLTRAP.NO_TOKENS"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FALLTRAP.INVENTORY_FULL"));
            return;
        }

        claimMap.put(player.getUniqueId(), new PlayerClaim());
        Utils.giveClaimingWand(getManager(), player, claimWand);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (!claimMap.containsKey(player.getUniqueId())) return;
        if (e.getItem() == null) return;
        if (!e.getItem().isSimilar(claimWand)) return;
        if (getInstance().getTeamManager().getByPlayer(player.getUniqueId()) == null) return;

        Block clicked = e.getClickedBlock();
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        PlayerClaim claim = claimMap.get(player.getUniqueId());
        Location location1 = claim.getLocation1();
        Location location2 = claim.getLocation2();
        Action action = e.getAction();

        // Make sure its cancelled no matter what.
        e.setCancelled(true);

        if (action == Action.RIGHT_CLICK_AIR) {
            clearSelection(player);
            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FALLTRAP.CANCELLED_SELECTION"));
            return;
        }

        if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {
            if (location1 == null || location2 == null) {
                sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FALLTRAP.NEVER_SELECT_ALL"));
                return;
            }

            Claim claimObject = new Claim(pt.getUniqueID(), location1, location2);
            claimObject.setY1(getTeamConfig().getInt("FALLTRAP_CONFIG.Y_LEVEL"));
            claimObject.setY2(Math.max(location1.getBlockY(), location2.getBlockY()));
            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            clearSelection(player);
            new TeamFalltrapMenu(getInstance().getMenuManager(), player, claimObject).open();
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (clicked == null) return;
            if (cooldown.hasCooldown(player)) return;

            Location blockClick = clicked.getLocation();
            Team atBlock = getInstance().getTeamManager().getClaimManager().getTeam(blockClick);

            if (atBlock != pt) {
                sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FALLTRAP.CANNOT_CREATE_HERE"));
                return;
            }

            if (location1 != null && location1.getBlockX() == blockClick.getBlockX() &&
                    location1.getBlockZ() == blockClick.getBlockZ()) return;

            cooldown.applyCooldownTicks(player, 25);
            getInstance().getWallManager().clearPillar(player, claim.getLocation1());
            Tasks.execute(getManager(), () -> getInstance().getWallManager().sendPillar(player, blockClick));
            claim.setLocation1(blockClick);
            return;
        }

        if (action == Action.LEFT_CLICK_BLOCK) {
            if (clicked == null) return;
            if (cooldown.hasCooldown(player)) return;

            Location blockClick = clicked.getLocation();
            Team atBlock = getInstance().getTeamManager().getClaimManager().getTeam(blockClick);

            if (atBlock != pt) {
                sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_FALLTRAP.CANNOT_CREATE_HERE"));
                return;
            }

            if (location2 != null && location2.getBlockX() == blockClick.getBlockX() &&
                    location2.getBlockZ() == blockClick.getBlockZ()) return;

            cooldown.applyCooldownTicks(player, 25);
            getInstance().getWallManager().clearPillar(player, claim.getLocation2());
            Tasks.execute(getManager(), () -> getInstance().getWallManager().sendPillar(player, blockClick));
            claim.setLocation2(blockClick);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        if (claimMap.containsKey(player.getUniqueId())) {
            claimMap.remove(player.getUniqueId());
            player.getInventory().remove(claimWand);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        e.getDrops().remove(claimWand);
        clearSelection(player);
    }

    @EventHandler
    public void onItem(PlayerItemDamageEvent e) {
        Player player = e.getPlayer();

        if (e.getItem().isSimilar(claimWand)) {
            clearSelection(player);
            getManager().setItemInHand(player, new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // Should never happen unless server crashed
        if (player.getInventory().contains(claimWand)) {
            player.getInventory().remove(claimWand);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Item item = e.getItemDrop();

        if (item.getItemStack().isSimilar(claimWand)) {
            item.remove();
            clearSelection(player);
        }
    }

    private void clearSelection(Player player) {
        if (claimMap.containsKey(player.getUniqueId())) {
            PlayerClaim claim = claimMap.get(player.getUniqueId());

            Tasks.execute(getManager(), () -> {
                getInstance().getWallManager().clearPillar(player, claim.getLocation1());
                getInstance().getWallManager().clearPillar(player, claim.getLocation2());
            });

            claimMap.remove(player.getUniqueId());
        }
    }

    @Getter
    @Setter
    public static class PlayerClaim {

        private Location location1;
        private Location location2;

        public PlayerClaim() {
            this.location1 = null;
            this.location2 = null;
        }
    }
}