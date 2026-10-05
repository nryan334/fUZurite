package me.keano.azurite.modules.events.conquest.command.args;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.conquest.Conquest;
import me.keano.azurite.modules.events.conquest.extra.Capzone;
import me.keano.azurite.modules.events.conquest.extra.ConquestType;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Argument;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.cuboid.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class ConquestClaimArg extends Argument {

    private final Map<UUID, ZoneClaim> claimCache;
    private final ItemStack claimWand;

    public ConquestClaimArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "claimzone",
                        "claim"
                )
        );
        this.setPermissible("azurite.conquest.claim");
        this.claimCache = new HashMap<>();
        this.claimWand = new ItemBuilder(ItemUtils.getMat(manager.getTeamConfig().getString("CLAIMING.CLAIM_WAND.TYPE")))
                .setName(manager.getTeamConfig().getString("CLAIMING.CLAIM_WAND.NAME") + " &7(Capture Zone)")
                .setLore(manager.getTeamConfig().getStringList("CLAIMING.CLAIM_WAND.LORE"))
                .toItemStack();
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_CLAIM.USAGE");
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
        ConquestType type;

        try {

            type = ConquestType.valueOf(args[0].toUpperCase());

        } catch (IllegalArgumentException e) {
            type = null;
        }

        if (type == null) {
            sendMessage(sender, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_CLAIM.TYPE_NOT_FOUND")
                    .replace("%type%", args[0])
            );
            return;
        }

        claimCache.put(player.getUniqueId(), new ZoneClaim(type));
        player.getInventory().addItem(claimWand);
        sendMessage(sender, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_CLAIM.STARTED_PROCESS"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return Stream.of(ConquestType.values())
                    .map(ConquestType::getName)
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (!claimCache.containsKey(player.getUniqueId())) return;
        if (e.getItem() == null) return;
        if (!e.getItem().isSimilar(claimWand)) return;

        ZoneClaim zoneClaim = claimCache.get(player.getUniqueId());
        Location location1 = zoneClaim.getLocation1();
        Location location2 = zoneClaim.getLocation2();
        ConquestType type = zoneClaim.getType();
        Block block = e.getClickedBlock();

        // Make sure its cancelled no matter what.
        e.setCancelled(true);

        // Cancel claim selection
        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            claimCache.remove(player.getUniqueId());
            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            return;
        }

        // Purchase claim selection
        if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {
            if (location1 == null || location2 == null) {
                sendMessage(player, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_CLAIM.NEVER_CLAIM_ALL_POS"));
                return;
            }

            Cuboid cuboid = new Cuboid(location1, location2);
            Conquest conquest = getInstance().getConquestManager().getConquest();
            Capzone capzone = new Capzone(getInstance().getConquestManager(), cuboid, type);

            capzone.checkZone(true);
            capzone.setZone(cuboid);
            conquest.getCapzones().put(type, capzone);
            conquest.save();

            claimCache.remove(player.getUniqueId());

            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            sendMessage(player, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_CLAIM.CLAIMED"));
            return;
        }

        // 1st Location
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block == null) return;
            Location clicked = block.getLocation();
            if (location1 == clicked) return;

            zoneClaim.setLocation1(clicked);

            sendMessage(player, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_CLAIM.UPDATE_LOC1")
                    .replace("%loc%", Utils.formatLocation(clicked))
            );
            return;
        }

        // 2nd Location
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (block == null) return;
            Location clicked = block.getLocation();
            if (location2 == clicked) return;

            zoneClaim.setLocation2(clicked);
            sendMessage(player, getLanguageConfig().getString("CONQUEST_COMMAND.CONQUEST_CLAIM.UPDATE_LOC2")
                    .replace("%loc%", Utils.formatLocation(clicked))
            );
        }
    }

    @Getter
    @Setter
    private static class ZoneClaim {

        private ConquestType type;
        private Location location1;
        private Location location2;

        public ZoneClaim(ConquestType type) {
            this.type = type;
            this.location1 = null;
            this.location2 = null;
        }
    }
}