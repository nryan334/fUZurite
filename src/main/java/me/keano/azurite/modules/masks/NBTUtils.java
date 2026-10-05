package me.keano.azurite.modules.masks;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class NBTUtils {

    private static String CB_PACKAGE;
    private static String NMS_PACKAGE;
    private static Method asNMSCopy;
    private static Method asBukkitCopy;
    private static Method getTag;
    private static Method setTag;
    private static Method hasKey;
    private static Method getString;
    private static Method setString;
    private static Class<?> nbtTagCompoundClass;

    static {
        try {
            CB_PACKAGE = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
            NMS_PACKAGE = CB_PACKAGE.replace("org.bukkit.craftbukkit", "net.minecraft.server");
            Class<?> craftItemStack = Class.forName(CB_PACKAGE + ".inventory.CraftItemStack");
            Class<?> nmsItemStack = Class.forName(NMS_PACKAGE + ".ItemStack");
            nbtTagCompoundClass = Class.forName(NMS_PACKAGE + ".NBTTagCompound");

            asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
            asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", nmsItemStack);
            getTag = nmsItemStack.getMethod("getTag");
            setTag = nmsItemStack.getMethod("setTag", nbtTagCompoundClass);
            hasKey = nbtTagCompoundClass.getMethod("hasKey", String.class);
            getString = nbtTagCompoundClass.getMethod("getString", String.class);
            setString = nbtTagCompoundClass.getMethod("setString", String.class, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getMaskId(ItemStack item) {
        if (item == null) return null;
        try {
            Object nmsItem = asNMSCopy.invoke(null, item);
            Object tag = getTag.invoke(nmsItem);
            if (tag == null) return null;
            if (!(boolean) hasKey.invoke(tag, "mask_id")) return null;
            return (String) getString.invoke(tag, "mask_id");
        } catch (Exception e) {
            return null;
        }
    }

    public static ItemStack setMaskId(ItemStack item, String maskId) {
        if (item == null) return null;
        try {
            Object nmsItem = asNMSCopy.invoke(null, item);
            Object tag = getTag.invoke(nmsItem);
            if (tag == null) {
                tag = nbtTagCompoundClass.newInstance();
            }
            setString.invoke(tag, "mask_id", maskId);
            setTag.invoke(nmsItem, tag);
            return (ItemStack) asBukkitCopy.invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
            return item;
        }
    }

    public static boolean hasMaskId(ItemStack item) {
        return getMaskId(item) != null;
    }
}
