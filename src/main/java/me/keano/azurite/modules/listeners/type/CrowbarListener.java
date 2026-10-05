package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.spawners.Spawner;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class CrowbarListener extends Module<ListenerManager> {

    private static final Pattern PATTERN = Pattern.compile("\\d+"); // number

    private final String name;
    private final Material material;

    public CrowbarListener(ListenerManager manager) {
        super(manager);
        this.name = getConfig().getString("CROWBARS.NAME");
        this.material = ItemUtils.getMat(getConfig().getString("CROWBARS.MATERIAL"));
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);
        Block clicked = e.getClickedBlock();

        if (clicked == null) return;
        if (hand == null) return;
        if (!isCrowbar(hand)) return;

        e.setCancelled(true); // just in case they try hoeing the ground
        World.Environment environ = clicked.getWorld().getEnvironment();

        if (environ == World.Environment.NETHER) {
            player.sendMessage(getLanguageConfig().getString("CROWBAR_LISTENER.CANNOT_USE_NETHER"));
            return;
        }

        if (environ == World.Environment.THE_END) {
            player.sendMessage(getLanguageConfig().getString("CROWBAR_LISTENER.CANNOT_USE_END"));
            return;
        }

        if (!getInstance().getTeamManager().canBuild(player, clicked.getLocation())) {
            player.sendMessage(getLanguageConfig().getString("CROWBAR_LISTENER.CANNOT_USE"));
            return;
        }

        if (clicked.getType() == ItemUtils.getMat("MOB_SPAWNER")) {
            int spawnerAmount = getSpawnerAmount(hand);
            CreatureSpawner spawner = (CreatureSpawner) clicked.getState();

            if (spawnerAmount == 0) {
                player.sendMessage(getLanguageConfig().getString("CROWBAR_LISTENER.INSUFFICIENT_SPAWNER"));
                return;
            }

            Spawner customSpawner = getInstance().getSpawnerManager().getSpawners().get(spawner.getSpawnedType());

            if (customSpawner == null) {
                player.sendMessage(getLanguageConfig().getString("CROWBAR_LISTENER.SPAWNER_NO_EXIST"));
                return;
            }

            // Bukkit.broadcastMessage("Spawner: " + spawnerAmount);
            e.setCancelled(true);
            clicked.setType(Material.AIR);
            clicked.getState().update();
            clicked.getWorld().dropItemNaturally(clicked.getLocation(), customSpawner.getItemStack());
            getInstance().getVersionManager().getVersion().playEffect(clicked.getLocation(), "STEP_SOUND", ItemUtils.getMat("MOB_SPAWNER"));
            getManager().setItemInHand(player, setSpawnerAmount(hand, spawnerAmount - 1));
            return;
        }

        if (clicked.getType() == ItemUtils.getMat("ENDER_PORTAL_FRAME")) {
            int endFrameAmount = getEndFrameAmount(hand);

            if (endFrameAmount == 0) {
                player.sendMessage(getLanguageConfig().getString("CROWBAR_LISTENER.INSUFFICIENT_END_FRAME"));
                return;
            }

            e.setCancelled(true);
            clicked.setType(Material.AIR);
            clicked.getState().update();
            clicked.getWorld().dropItemNaturally(clicked.getLocation(), new ItemStack(ItemUtils.getMat("ENDER_PORTAL_FRAME")));
            getInstance().getVersionManager().getVersion().playEffect(clicked.getLocation(), "STEP_SOUND", ItemUtils.getMat("ENDER_PORTAL_FRAME"));
            getManager().setItemInHand(player, setEndFrameAmount(hand, endFrameAmount - 1));
        }
    }

    private boolean isCrowbar(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return item.getType() == material && item.hasItemMeta() && meta.getDisplayName().equals(name) && meta.hasLore();
    }

    private ItemStack setSpawnerAmount(ItemStack item, int amount) {
        String spawnerLine = getConfig().getString("CROWBARS.SPAWNER_LINE");
        ItemBuilder builder = new ItemBuilder(item);
        builder.addLoreLine(spawnerLine.replace("%spawner%", String.valueOf(amount)), 0);
        return builder.toItemStack();
    }

    private ItemStack setEndFrameAmount(ItemStack item, int amount) {
        String spawnerLine = getConfig().getString("CROWBARS.END_FRAME_LINE");
        ItemBuilder builder = new ItemBuilder(item);
        builder.addLoreLine(spawnerLine.replace("%endframe%", String.valueOf(amount)), 1);
        return builder.toItemStack();
    }

    private int getSpawnerAmount(ItemStack item) {
        String line = item.getItemMeta().getLore().get(0);
        Matcher matcher = PATTERN.matcher(ChatColor.stripColor(line));
        int amount = 0;

        while (matcher.find()) {
            amount = Integer.parseInt(matcher.group());
        }

        return amount;
    }

    private int getEndFrameAmount(ItemStack item) {
        String line = item.getItemMeta().getLore().get(1);
        Matcher matcher = PATTERN.matcher(ChatColor.stripColor(line));
        int amount = 0;

        while (matcher.find()) {
            amount = Integer.parseInt(matcher.group());
        }

        return amount;
    }
}