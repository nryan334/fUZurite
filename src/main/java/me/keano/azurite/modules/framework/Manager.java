package me.keano.azurite.modules.framework;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.extra.Configs;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public abstract class Manager extends Configs {

    private static Method SET_DATA;
    private final HCF instance;

    public Manager(HCF instance) {
        this.instance = instance;
        this.instance.getManagers().add(this);
    }

    public void registerListener(Listener listener) {
        instance.getServer().getPluginManager().registerEvents(listener, instance);
    }

    public void setData(ItemStack item, int data) {
        if (Utils.isModernVer()) {
            if (item.getItemMeta() == null) return;

            Damageable damageable = (Damageable) item.getItemMeta();
            damageable.setDamage(data);
            item.setItemMeta((ItemMeta) damageable);

        } else {
            item.setDurability((short) data);
        }
    }

    public void playSound(Player player, String sound, boolean world) {
        if (world) {
            playSound(player.getLocation(), sound);

        } else {
            Tasks.execute(this, () -> player.playSound(player.getLocation(), getSound(sound), 1.0F, 1.0F));
        }
    }

    public void playSound(Location location, String sound) {
        Tasks.execute(this, () -> location.getWorld().playSound(location, getSound(sound), 1.0F, 1.0F));
    }

    public Sound getSound(String sound) {
        Sound toCreate = null;

        if (Utils.isModernVer()) {
            if (sound.equalsIgnoreCase("LEVEL_UP")) {
                toCreate = Sound.valueOf("ENTITY_PLAYER_LEVELUP");
            }

            if (sound.equalsIgnoreCase("ORB_PICKUP")) {
                toCreate = Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP");
            }

            if (sound.equalsIgnoreCase("ITEM_BREAK")) {
                toCreate = Sound.valueOf("ENTITY_ITEM_BREAK");
            }

            if (sound.equalsIgnoreCase("NOTE_BASS_DRUM")) {
                toCreate = Sound.valueOf("BLOCK_NOTE_BLOCK_BASEDRUM");
            }

            if (sound.equalsIgnoreCase("CLICK")) {
                toCreate = Sound.valueOf("UI_BUTTON_CLICK");
            }
        }

        if (toCreate == null) {
            try {

                toCreate = Sound.valueOf(sound);

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Sound " + sound + " is wrong!");
            }
        }

        return toCreate;
    }

    public void setData(Block block, int data) {
        if (!Utils.isModernVer()) {
            if (SET_DATA == null) {
                SET_DATA = ReflectionUtils.accessMethod(Block.class, "setData", byte.class);
            }

            try {

                SET_DATA.invoke(block, (byte) data);

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int getData(ItemStack item) {
        if (Utils.isModernVer()) {
            if (item.getItemMeta() == null) return 0;

            Damageable damageable = (Damageable) item.getItemMeta();
            return damageable.getDamage();

        } else {
            return item.getDurability();
        }
    }

    public boolean isGapple(ItemStack item) {
        return !getInstance().getVersionManager().getVersion().isNotGapple(item);
    }

    public void setItemInHand(Player player, ItemStack item) {
        getInstance().getVersionManager().getVersion().setItemInHand(player, item);
        player.updateInventory();
    }

    public void takeItemInHand(Player player, int amount) {
        ItemStack hand = getItemInHand(player);

        if (hand == null) return;

        if (hand.getAmount() <= 1) {
            setItemInHand(player, new ItemStack(Material.AIR));

        } else {
            hand.setAmount(hand.getAmount() - amount);
        }

        player.updateInventory();
    }

    public ItemStack getItemInHand(Player player) {
        ItemStack hand = getInstance().getVersionManager().getVersion().getItemInHand(player);

        if (hand != null && hand.getType() == Material.AIR) {
            return null;
        }

        return hand;
    }

    public void enable() {
    }

    public void disable() {
    }

    public void reload() {
    }
}