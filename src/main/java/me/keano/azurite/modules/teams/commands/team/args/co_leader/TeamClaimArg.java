package me.keano.azurite.modules.teams.commands.team.args.co_leader;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.claims.ClaimManager;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.WarzoneTeam;
import me.keano.azurite.modules.teams.type.WildernessTeam;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class TeamClaimArg extends Argument {

    private final Map<UUID, PlayerClaim> claimMap;
    private final List<BlockFace> faces;
    private final Cooldown cooldown;
    private final ItemStack claimWand;

    public TeamClaimArg(CommandManager manager) {
        super(
                manager,
                Collections.singletonList(
                        "claim"
                )
        );

        this.claimMap = new HashMap<>();
        this.cooldown = new Cooldown(manager);
        this.faces = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
        this.claimWand = new ItemBuilder(ItemUtils.getMat(manager.getTeamConfig().getString("CLAIMING.CLAIM_WAND.TYPE")))
                .setName(manager.getTeamConfig().getString("CLAIMING.CLAIM_WAND.NAME"))
                .setLore(manager.getTeamConfig().getStringList("CLAIMING.CLAIM_WAND.LORE"))
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

        if (pt.getClaims().size() == getTeamConfig().getInt("TEAMS.MAX_CLAIMS")) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.MAX_CLAIMS"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            sendMessage(sender, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.INVENTORY_FULL"));
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

        // Made for youtubers to bypass and able to claim in other worlds.
        if (!player.hasPermission("azurite.claim.bypass") && player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.WRONG_WORLD"));
            return;
        }

        Block clicked = e.getClickedBlock();
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        PlayerClaim claim = claimMap.get(player.getUniqueId());
        Location location1 = claim.getLocation1();
        Location location2 = claim.getLocation2();
        Action action = e.getAction();

        // Make sure its cancelled no matter what.
        e.setCancelled(true);

        // Purchase & Claim need to be above the rest - keqno

        // Cancel claim selection
        if (action == Action.RIGHT_CLICK_AIR) {
            clearSelection(player);
            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.CANCELLED_SELECTION"));
            return;
        }

        // Purchase claim selection
        if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {
            if (location1 == null || location2 == null) {
                sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.INSUFFICIENT_SELECTIONS"));
                return;
            }

            Claim claimObject = new Claim(pt.getUniqueID(), location1, location2);
            boolean bypass = player.hasPermission("azurite.claim.nomoney");
            int price = getInstance().getTeamManager().getClaimManager().getPrice(claimObject, false);

            // make sure we check once more before confirming claim
            if (cannotClaim(player, location1.getBlock()) || cannotClaim(player, location2.getBlock())) return;
            if (cannotClaim(player, claimObject, false)) return;

            if (!pt.getClaims().isEmpty()) {
                // Uses && just to check that both are not touching another claim. One can still be!
                if (cannotResize(player, location1.getBlock()) && cannotResize(player, location2.getBlock())) {
                    sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.CLAIMS_TOUCHING"));
                    return;
                }
            }

            if (pt.getClaims().size() == getTeamConfig().getInt("TEAMS.MAX_CLAIMS")) {
                sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.MAX_CLAIMS"));
                return;
            }

            Claim cloned = new Claim(claimObject);
            cloned.setY1(1);
            cloned.setY2(1);

            for (Block block : cloned) {
                if (cannotClaim(player, block)) return;
            }

            if (pt.getBalance() < price && !bypass) {
                sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.INSUFFICIENT_BALANCE"));
                return;
            }

            if (!bypass) {
                pt.setBalance(pt.getBalance() - price);
            }

            pt.getClaims().add(claimObject);

            for (Claim ptClaim : pt.getClaims()) {
                getInstance().getTeamManager().getClaimManager().saveClaim(ptClaim);
            }

            if (getInstance().getWallManager().getWallType(claimObject, pt, player) != null) {
                getInstance().getTeamManager().getClaimManager().teleportSafe(player);
            }

            for (Player inClaim : claimObject.getPlayers()) {
                if (getInstance().getWallManager().getWallType(claimObject, pt, inClaim) != null) {
                    getInstance().getTeamManager().getClaimManager().teleportSafe(inClaim);
                }
            }

            clearSelection(player);
            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.PURCHASED_CLAIM")
                    .replace("%balance%", String.valueOf(pt.getBalance()))
                    .replace("%price%", String.valueOf(price))
            );
            return;
        }

        // 1st Location
        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (clicked == null) return;
            if (cooldown.hasCooldown(player)) return;

            Location blockClick = clicked.getLocation();
            Team atBlock = getInstance().getTeamManager().getClaimManager().getTeam(blockClick);

            if (!(atBlock instanceof WildernessTeam)) {
                sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.CANNOT_CLAIM_HERE"));
                return;
            }

            if (cannotClaim(player, clicked)) return;
            if (location1 != null && location1.getBlockX() == blockClick.getBlockX() &&
                    location1.getBlockZ() == blockClick.getBlockZ()) return;

            if (!pt.getClaims().isEmpty() && location2 == null) {
                if (cannotResize(player, clicked)) {
                    sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.CLAIMS_TOUCHING"));
                    return;
                }
            }

            // Make sure we check if both locations are set and if so make sure it's not a 5x5
            if (location2 != null) {
                Claim cuboid = new Claim(pt.getUniqueID(), location2, blockClick);
                if (cannotClaim(player, cuboid, true)) return;
            }

            getInstance().getWallManager().clearPillar(player, claim.getLocation1());

            // Allow a delay
            Tasks.execute(getManager(), () -> getInstance().getWallManager().sendPillar(player, blockClick));
            cooldown.applyCooldownTicks(player, 25);
            claim.setLocation1(blockClick);
            return;
        }

        // 2nd Location
        if (action == Action.LEFT_CLICK_BLOCK) {
            if (clicked == null) return;
            if (cooldown.hasCooldown(player)) return;

            Location blockClick = clicked.getLocation();
            Team atBlock = getInstance().getTeamManager().getClaimManager().getTeam(blockClick);

            if (!(atBlock instanceof WildernessTeam)) {
                sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.CANNOT_CLAIM_HERE"));
                return;
            }

            if (cannotClaim(player, clicked)) return;
            if (location2 != null && location2.getBlockX() == blockClick.getBlockX() &&
                    location2.getBlockZ() == blockClick.getBlockZ()) return;

            if (!pt.getClaims().isEmpty() && location1 == null) {
                if (cannotResize(player, clicked)) {
                    sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.CLAIMS_TOUCHING"));
                    return;
                }
            }

            if (location1 != null) {
                Claim cuboid = new Claim(pt.getUniqueID(), location1, blockClick);
                if (cannotClaim(player, cuboid, true)) return;
            }

            getInstance().getWallManager().clearPillar(player, claim.getLocation2());

            // Allow a delay
            Tasks.execute(getManager(), () -> getInstance().getWallManager().sendPillar(player, blockClick));

            cooldown.applyCooldownTicks(player, 25);
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

    private boolean cannotClaim(Player player, Block block) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        Location location = block.getLocation();

        int separator = getTeamConfig().getInt("CLAIMING.CLAIM_SEPARATOR");
        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();

        boolean cannotClaim = false;

        for (int x = blockX - separator; x <= blockX + separator; x++) {
            for (int z = blockZ - separator; z <= blockZ + separator; z++) {
                Team at = getInstance().getTeamManager().getClaimManager().getTeam(location.getWorld(), x, z);

                if (at instanceof WildernessTeam || at instanceof WarzoneTeam) continue;

                if (at != pt) {
                    cannotClaim = true;
                    break;
                }
            }
        }

        if (cannotClaim) {
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.TOO_CLOSE")
                    .replace("%amount%", String.valueOf(separator))
            );
        }

        return cannotClaim;
    }

    private boolean cannotResize(Player player, Block block) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        ClaimManager claimManager = getInstance().getTeamManager().getClaimManager();
        boolean hasTeamNearby = false;
        boolean hasEnemyNearby = false;

        for (BlockFace face : faces) {
            Team team = claimManager.getTeam(block.getRelative(face).getLocation());

            if (team == pt) {
                hasTeamNearby = true;
            }

            if (team != pt && team.getType() != TeamType.WILDERNESS && team.getType() != TeamType.WARZONE) {
                hasEnemyNearby = true;
            }
        }

        return !hasTeamNearby && !hasEnemyNearby;
    }

    private boolean cannotClaim(Player player, Claim claim, boolean message) {
        int minSize = getTeamConfig().getInt("CLAIMING.MIN_SIZE");
        int maxSize = getTeamConfig().getInt("CLAIMING.MAX_SIZE");

        if (claim.getLength() < minSize || claim.getWidth() < minSize) {
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.SIZE_SMALL")
                    .replace("%size%", minSize + "x" + minSize)
            );
            return true;
        }

        if (claim.getLength() > maxSize || claim.getWidth() > maxSize) {
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.SIZE_LARGE")
                    .replace("%size%", maxSize + "x" + maxSize)
            );
            return true;
        }

        if (message) {
            sendMessage(player, getLanguageConfig().getString("TEAM_COMMAND.TEAM_CLAIM.SET_LOCATIONS")
                    .replace("%price%", String.valueOf(getInstance().getTeamManager().getClaimManager().getPrice(claim, false)))
                    .replace("%length%", String.valueOf(claim.getLength()))
                    .replace("%width%", String.valueOf(claim.getWidth()))
                    .replace("%blocks%", String.valueOf(claim.getArea()))
            );
        }
        return false;
    }

    @Getter
    @Setter
    private static class PlayerClaim {

        private Location location1;
        private Location location2;

        public PlayerClaim() {
            this.location1 = null;
            this.location2 = null;
        }
    }
}