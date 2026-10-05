package me.keano.azurite.modules.events.koth.command.args;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.Koth;
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

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothClaimArg extends Argument {

    private final Map<UUID, ZoneClaim> claimCache;
    private final ItemStack claimWand;

    public KothClaimArg(CommandManager manager) {
        super(
                manager,
                Arrays.asList(
                        "claimzone",
                        "setzone",
                        "claim"
                )
        );
        this.setPermissible("azurite.koth.claim");
        this.claimCache = new HashMap<>();
        this.claimWand = new ItemBuilder(ItemUtils.getMat(manager.getTeamConfig().getString("CLAIMING.CLAIM_WAND.TYPE")))
                .setName(manager.getTeamConfig().getString("CLAIMING.CLAIM_WAND.NAME") + " &7(Capture Zone)")
                .setLore(manager.getTeamConfig().getStringList("CLAIMING.CLAIM_WAND.LORE"))
                .toItemStack();
    }

    @Override
    public String usage() {
        return getLanguageConfig().getString("KOTH_COMMAND.KOTH_CLAIMZONE.USAGE");
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
        Koth koth = getInstance().getKothManager().getKoth(args[0]);

        if (koth == null) {
            sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_NOT_FOUND")
                    .replace("%koth%", args[0])
            );
            return;
        }

        claimCache.put(player.getUniqueId(), new ZoneClaim(koth));
        player.getInventory().addItem(claimWand);

        sendMessage(sender, getLanguageConfig().getString("KOTH_COMMAND.KOTH_CLAIMZONE.CLAIMING_STARTED")
                .replace("%koth%", koth.getName())
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            String string = args[args.length - 1];
            return getInstance().getKothManager().getKoths().values()
                    .stream()
                    .map(Koth::getName)
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
        Koth koth = zoneClaim.getKoth();
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
                sendMessage(player, getLanguageConfig().getString("KOTH_COMMAND.KOTH_CLAIMZONE.NEED_BOTH_LOCS"));
                return;
            }

            Cuboid cuboid = new Cuboid(location1, location2);
            koth.checkZone(true); // we delete the old one.
            koth.setCaptureZone(cuboid);
            koth.save(); // save the zone etc...

            claimCache.remove(player.getUniqueId());

            getManager().setItemInHand(player, new ItemStack(Material.AIR));
            sendMessage(player, getLanguageConfig().getString("KOTH_COMMAND.KOTH_CLAIMZONE.SET_ZONE"));
            return;
        }

        // 1st Location
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block == null) return;
            Location clicked = block.getLocation();
            if (location1 == clicked) return;

            zoneClaim.setLocation1(clicked);
            sendMessage(player, getLanguageConfig().getString("KOTH_COMMAND.KOTH_CLAIMZONE.UPDATED_LOC1")
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
            sendMessage(player, getLanguageConfig().getString("KOTH_COMMAND.KOTH_CLAIMZONE.UPDATED_LOC2")
                    .replace("%loc%", Utils.formatLocation(clicked))
            );
        }
    }

    @Getter
    @Setter
    private static class ZoneClaim {

        private Koth koth;
        private Location location1;
        private Location location2;

        public ZoneClaim(Koth koth) {
            this.koth = koth;
            this.location1 = null;
            this.location2 = null;
        }
    }
}