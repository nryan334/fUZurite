package me.keano.azurite.modules.teams.commands.systeam.args;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SysTeamClaimArg extends Argument {

    private final Map<UUID, SystemClaim> claimMap;
    private final ItemStack claimWand;

    public SysTeamClaimArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "claim"
                )
        );

        this.claimMap = new HashMap<>();
        this.claimWand = new ItemBuilder(ItemUtils.getMat(
                manager.getTeamConfig().getString("CLAIMING.CLAIM_WAND.TYPE")))
                .setName(manager.getTeamConfig().getString("CLAIMING.CLAIM_WAND.NAME") + " &7(System Claim)")
                .setLore(manager.getTeamConfig().getStringList("CLAIMING.CLAIM_WAND.LORE"))
                .toItemStack();
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CLAIM.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Config.PLAYER_ONLY);
            return;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        Player player = (Player) sender;
        Team team = getInstance().getTeamManager().getTeam(args[0]);

        if (team == null) {
            sendMessage(sender, Config.TEAM_NOT_FOUND
                    .replace("%team%", args[0])
            );
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            sendMessage(sender, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CLAIM.INVENTORY_FULL"));
            return;
        }

        claimMap.put(player.getUniqueId(), new SystemClaim(team));
        Utils.giveClaimingWand(getManager(), player, claimWand);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getTeamManager().getSystemTeams().values()
                    .stream()
                    .map(Team::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (!claimMap.containsKey(player.getUniqueId())) return;
        if (e.getItem() == null) return;
        if (!e.getItem().isSimilar(claimWand)) return;

        SystemClaim claim = claimMap.get(player.getUniqueId());
        Team team = claim.getSystemTeam();
        Location location1 = claim.getLocation1();
        Location location2 = claim.getLocation2();

        // Make sure its cancelled no matter what.
        e.setCancelled(true);

        // Cancel claim selection
        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            sendMessage(player, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CLAIM.CANCELLED_SELECTION"));
            clearSelection(player);
            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            return;
        }

        // Purchase claim selection
        if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {
            if (location1 == null || location2 == null) {
                sendMessage(player, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CLAIM.INSUFFICIENT_SELECTIONS"));
                return;
            }

            Claim toAdd = new Claim(team.getUniqueID(), location1, location2);

            getInstance().getTeamManager().getClaimManager().saveClaim(toAdd);
            team.getClaims().add(toAdd);
            team.save();

            if (getInstance().getWallManager().getWallType(toAdd, team, player) != null) {
                getInstance().getTeamManager().getClaimManager().teleportSafe(player);
            }

            for (Player inClaim : toAdd.getPlayers()) {
                if (getInstance().getWallManager().getWallType(toAdd, team, inClaim) != null) {
                    getInstance().getTeamManager().getClaimManager().teleportSafe(inClaim);
                }
            }

            sendMessage(player, getLanguageConfig().getString("SYSTEM_TEAM_COMMAND.SYSTEM_TEAM_CLAIM.CLAIMED_SUCCESSFUL"));

            clearSelection(player);
            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            return;
        }

        // 1st Location
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock() == null) return;

            Location blockClick = e.getClickedBlock().getLocation();

            getInstance().getWallManager().clearPillar(player, claim.getLocation1());
            Tasks.execute(getManager(), () -> getInstance().getWallManager().sendPillar(player, blockClick));

            claim.setLocation1(blockClick);
            return;
        }

        // 2nd Location
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (e.getClickedBlock() == null) return;

            Location blockClick = e.getClickedBlock().getLocation();

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

        // Should never happen unless server crash, and we are unable to do it onQuit
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
            SystemClaim claim = claimMap.get(player.getUniqueId());

            getInstance().getWallManager().clearPillar(player, claim.getLocation1());
            getInstance().getWallManager().clearPillar(player, claim.getLocation2());

            claimMap.remove(player.getUniqueId());
        }
    }

    @Getter
    @Setter
    private static class SystemClaim {

        private Team systemTeam;
        private Location location1;
        private Location location2;

        public SystemClaim(Team team) {
            this.systemTeam = team;
            this.location1 = null;
            this.location2 = null;
        }
    }
}