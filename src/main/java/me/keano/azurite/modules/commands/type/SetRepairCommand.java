package me.keano.azurite.modules.commands.type;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.modules.versions.type.Version1_7_R4;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("deprecation")
public class SetRepairCommand extends Command {

    private Location clickAll;
    private Location clickHand;

    public SetRepairCommand(CommandManager manager) {
        super(
                manager,
                "setrepair"
        );
        this.setPermissible("azurite.setrepair");

        this.clickAll = Serializer.fetchLocation(getMiscConfig().getString("REPAIR_LOC_ALL"));
        this.clickHand = Serializer.fetchLocation(getMiscConfig().getString("REPAIR_LOC_HAND"));

        this.completions.add(new TabCompletion(Arrays.asList("all", "hand"), 0));
        this.completions.add(new TabCompletion(Collections.singletonList("clear"), 1));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("SET_REPAIR_COMMAND.USAGE");
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
        Block target = (getInstance().getVersionManager().getVersion() instanceof Version1_7_R4 ?
                player.getTargetBlock((HashSet<Byte>) null, 10) :
                player.getTargetBlock((Set<Material>) null, 10));

        if (target == null || target.getType() == Material.AIR) {
            sendMessage(sender, getLanguageConfig().getString("SET_REPAIR_COMMAND.NO_TARGET_BLOCK"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "all":
                if (args.length > 1 && args[1].equalsIgnoreCase("clear")) {
                    this.clickAll = null;

                    getMiscConfig().set("REPAIR_LOC_ALL", Serializer.serializeLoc(clickAll));
                    getMiscConfig().save();

                    sendMessage(sender, getLanguageConfig().getString("SET_REPAIR_COMMAND.CLEARED"));
                    return;
                }

                this.clickAll = target.getLocation();

                getMiscConfig().set("REPAIR_LOC_ALL", Serializer.serializeLoc(clickAll));
                getMiscConfig().save();

                sendMessage(sender, getLanguageConfig().getString("SET_REPAIR_COMMAND.SET_BLOCK")
                        .replace("%loc%", Utils.formatLocation(clickAll))
                );
                return;

            case "hand":
                if (args.length > 1 && args[1].equalsIgnoreCase("clear")) {
                    this.clickHand = null;

                    getMiscConfig().set("REPAIR_LOC_HAND", Serializer.serializeLoc(clickHand));
                    getMiscConfig().save();

                    sendMessage(sender, getLanguageConfig().getString("SET_REPAIR_COMMAND.CLEARED"));
                    return;
                }

                this.clickHand = target.getLocation();

                getMiscConfig().set("REPAIR_LOC_HAND", Serializer.serializeLoc(clickHand));
                getMiscConfig().save();

                sendMessage(sender, getLanguageConfig().getString("SET_REPAIR_COMMAND.SET_BLOCK")
                        .replace("%loc%", Utils.formatLocation(clickHand))
                );
                return;
        }

        sendUsage(sender);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (clickAll != null && block.getLocation().equals(clickAll)) {
            sendMessage(player, getLanguageConfig().getString("SET_REPAIR_COMMAND.CLEARED")
                    .replace("%loc%", Utils.formatLocation(clickAll))
            );

            this.clickAll = null;

            getMiscConfig().set("REPAIR_LOC_ALL", Serializer.serializeLoc(clickAll));
            getMiscConfig().save();

        } else if (clickHand != null && block.getLocation().equals(clickHand)) {
            sendMessage(player, getLanguageConfig().getString("SET_REPAIR_COMMAND.CLEARED")
                    .replace("%loc%", Utils.formatLocation(clickHand))
            );

            this.clickHand = null;

            getMiscConfig().set("REPAIR_LOC_HAND", Serializer.serializeLoc(clickHand));
            getMiscConfig().save();
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        if (!e.hasBlock()) return;

        Player player = e.getPlayer();
        Block clicked = e.getClickedBlock();

        if (clickAll != null && clicked.getLocation().equals(clickAll)) {
            e.setCancelled(true); // make sure its cancelled
            User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

            if (user.getBalance() < Config.REPAIR_ALL_COST) {
                sendMessage(player, getLanguageConfig().getString("SET_REPAIR_COMMAND.INSUFFICIENT_MONEY")
                        .replace("%amount%", String.valueOf(Config.REPAIR_ALL_COST))
                );
                return;
            }

            user.setBalance(user.getBalance() - Config.REPAIR_ALL_COST);
            user.save();
            repairAll(player);
            sendMessage(player, getLanguageConfig().getString("SET_REPAIR_COMMAND.REPAIRED_ALL")
                    .replace("%amount%", String.valueOf(Config.REPAIR_ALL_COST))
            );

        } else if (clickHand != null && clicked.getLocation().equals(clickHand)) {
            e.setCancelled(true); // make sure its cancelled
            User user = getInstance().getUserManager().getByUUID(player.getUniqueId());

            if (user.getBalance() < Config.REPAIR_HAND_COST) {
                sendMessage(player, getLanguageConfig().getString("SET_REPAIR_COMMAND.INSUFFICIENT_MONEY")
                        .replace("%amount%", String.valueOf(Config.REPAIR_HAND_COST))
                );
                return;
            }

            ItemStack hand = getManager().getItemInHand(player);

            if (hand == null) {
                sendMessage(player, getLanguageConfig().getString("SET_REPAIR_COMMAND.HAND_EMPTY"));
                return;
            }

            user.setBalance(user.getBalance() - Config.REPAIR_HAND_COST);
            user.save();
            repairHand(player, hand);
            sendMessage(player, getLanguageConfig().getString("SET_REPAIR_COMMAND.REPAIRED_HAND")
                    .replace("%amount%", String.valueOf(Config.REPAIR_HAND_COST))
            );
        }
    }

    private void repairHand(Player player, ItemStack hand) {
        getManager().setData(hand, 0);
        player.updateInventory();
    }

    private void repairAll(Player player) {
        for (ItemStack content : player.getInventory().getContents()) {
            if (content == null) continue;
            if (cannotRepair(content.getType().name().toLowerCase())) continue;

            ItemMeta meta = content.getItemMeta();

            if (content.hasItemMeta() && meta.hasLore() && !player.hasPermission("azurite.unrepairable.bypass")) {
                if (meta.getLore().stream().anyMatch(s -> s.toLowerCase().contains("unrepairable"))) {
                    continue;
                }
            }

            getManager().setData(content, 0);
        }

        for (ItemStack armorContent : player.getInventory().getArmorContents()) {
            if (armorContent == null) continue;
            if (cannotRepair(armorContent.getType().name().toLowerCase())) continue;

            ItemMeta meta = armorContent.getItemMeta();

            if (armorContent.hasItemMeta() && meta.hasLore() && !player.hasPermission("azurite.unrepairable.bypass")) {
                if (meta.getLore().stream().anyMatch(s -> s.toLowerCase().contains("unrepairable"))) {
                    continue;
                }
            }

            getManager().setData(armorContent, 0);
        }
    }

    private boolean cannotRepair(String name) {
        return !name.contains("helmet") && !name.contains("chestplate") && !name.contains("leggings") &&
                !name.contains("boots") && !name.contains("sword") && !name.contains("shovel") &&
                !name.contains("pickaxe") && !name.contains("axe") && !name.contains("hoe") && !name.contains("bow");
    }
}