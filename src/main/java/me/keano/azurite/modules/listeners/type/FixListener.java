package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Random;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class FixListener extends Module<ListenerManager> {

    private final Random foodRandom;
    private final boolean lowerHungerLevel;

    public FixListener(ListenerManager manager) {
        super(manager);
        this.foodRandom = new Random();
        this.lowerHungerLevel = getConfig().getBoolean("FIXES.LOWER_HUNGER_LEVEL");
        this.load();
    }

    private void load() {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();

        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next().getResult();
            Material type = itemStack.getType();

            if (!getConfig().getBoolean("FIXES.ENDERCHEST_CRAFTING") && type == Material.ENDER_CHEST) {
                iterator.remove();
            }

            if (!getConfig().getBoolean("FIXES.GOLDEN_APPLE_CRAFTING") && type == Material.GOLDEN_APPLE) {
                iterator.remove();
            }

            if (!getConfig().getBoolean("FIXES.GOD_APPLE_CRAFTING") && getManager().isGapple(itemStack)) {
                iterator.remove();
            }
        }

        if (getConfig().getBoolean("FIXES.EASIER_GLISTERING_CRAFT")) {
            ItemStack itemStack = new ItemStack(ItemUtils.getMat("SPECKLED_MELON"));
            ShapelessRecipe recipe = null;

            if (Utils.isModernVer()) {
                try {

                    Constructor<ShapelessRecipe> constructor = ReflectionUtils.accessConstructor(ShapelessRecipe.class, NamespacedKey.class, ItemStack.class);
                    recipe = constructor.newInstance(new NamespacedKey(getInstance(), "easyglistering"), itemStack);

                } catch (Exception e) {
                    // Ignored
                }
            } else recipe = new ShapelessRecipe(itemStack);

            if (recipe != null && Bukkit.getRecipesFor(itemStack).size() == 1) {
                Material goldNugget = ItemUtils.getMat("GOLD_NUGGET");
                Material melonSlice = ItemUtils.getMat("MELON_SLICE");
                recipe.addIngredient(goldNugget);
                recipe.addIngredient(melonSlice);
                Bukkit.addRecipe(recipe);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.MILK_BUCKET && Config.MILK_FIX) {
            Player player = e.getPlayer();
            getManager().setItemInHand(player, new ItemStack(Material.BUCKET));
            player.updateInventory();
            e.setCancelled(true);

            for (PotionEffectType debuff : Utils.DEBUFFS) {
                player.removePotionEffect(debuff);
            }
        }
    }

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();
        World to = player.getWorld();

        if (from == to) return;
        if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) return;

        if (to.getEnvironment() == World.Environment.THE_END) {
            player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        }
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true) // Taken from iHCF fixes
    public void onFood(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!lowerHungerLevel) return;

        Player player = (Player) e.getEntity();

        if (e.getFoodLevel() < player.getFoodLevel() && foodRandom.nextInt(100 + 1) > 4) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreeper(EntityTargetEvent e) {
        if (!(e.getEntity() instanceof Creeper)) return;

        e.setTarget(null);
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Block block = e.getBlockPlaced();

        if (block.getType() == ItemUtils.getMat("BEDROCK")) return;

        if (getConfig().getBoolean("DISABLE_BEDS") && block.getType().name().contains("BED")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(VehicleCreateEvent e) {
        Vehicle vehicle = e.getVehicle();

        if (!(vehicle instanceof Boat)) return;

        if (!getConfig().getBoolean("FIXES.BOAT_PLACING")) {
            vehicle.remove(); // remove it if it's not enabled
            return;
        }

        // If it's enabled but not placed in water remove it
        if (!vehicle.getLocation().subtract(0, 1, 0).getBlock().isLiquid()) {
            vehicle.remove();
        }
    }
}