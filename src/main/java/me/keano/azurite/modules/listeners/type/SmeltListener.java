package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SmeltListener extends Module<ListenerManager> {

    private final Map<Location, Furnace> furnaces;
    private final Map<Location, BrewingStand> brewingStands;

    public SmeltListener(ListenerManager manager) {
        super(manager);

        this.furnaces = new HashMap<>();
        this.brewingStands = new HashMap<>();

        getManager().getTasks().add(Bukkit.getScheduler().runTaskTimer(getInstance(), this::tick, 0L, 2L));
    }

    private void tick() {
        if (Utils.isModernVer()) return;

        Iterator<Furnace> furnaceIterator = furnaces.values().iterator();

        while (furnaceIterator.hasNext()) {
            Furnace furnace = furnaceIterator.next();
            furnace.setCookTime((short) (furnace.getCookTime() + Config.SMELT_MULTIPLIER));
            furnace.update();

            if (furnace.getBurnTime() <= 1) {
                furnaceIterator.remove(); // remove if there's 1 second or less left remove
            }
        }

        Iterator<BrewingStand> brewingIterator = brewingStands.values().iterator();

        while (brewingIterator.hasNext()) {
            BrewingStand brewingStand = brewingIterator.next();

            // Will bug out if chunk isn't loaded
            if (!brewingStand.getChunk().isLoaded()) {
                brewingIterator.remove();
                continue;
            }

            // more than 1 second
            if (brewingStand.getBrewingTime() > 1) {
                brewingStand.setBrewingTime(Math.max(1, brewingStand.getBrewingTime() - Config.SMELT_MULTIPLIER));
            }
        }
    }

    @EventHandler
    public void onBrew(BrewEvent e) {
        Block block = e.getBlock();

        if (!brewingStands.containsKey(block.getLocation())) {
            brewingStands.put(block.getLocation(), (BrewingStand) block.getState());
        }
    }

    @EventHandler
    public void onBurn(FurnaceBurnEvent e) {
        Block block = e.getBlock();

        if (!furnaces.containsKey(block.getLocation())) {
            furnaces.put(block.getLocation(), (Furnace) block.getState());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Block block = e.getClickedBlock();

        if (block.getType().name().contains("FURNACE")) {
            Furnace furnace = (Furnace) block.getState();
            furnaces.put(block.getLocation(), furnace);

        } else if (block.getState().getType().name().contains("BREWING_STAND")) {
            BrewingStand brewingStand = (BrewingStand) block.getState();
            brewingStands.put(block.getLocation(), brewingStand);
        }
    }
}