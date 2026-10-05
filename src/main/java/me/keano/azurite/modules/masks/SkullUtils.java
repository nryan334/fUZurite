package me.keano.azurite.modules.masks;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class SkullUtils {

    private static String CB_PACKAGE;
    private static Field profileField;

    static {
        try {
            CB_PACKAGE = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
            Class<?> skullMetaClass = Class.forName(CB_PACKAGE + ".inventory.CraftMetaSkull");
            profileField = skullMetaClass.getDeclaredField("profile");
            profileField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ItemStack applyTexture(ItemStack item, String base64Texture) {
        if (item == null || item.getType() != Material.SKULL_ITEM) return item;
        if (!(item.getItemMeta() instanceof SkullMeta)) return item;

        try {
            SkullMeta meta = (SkullMeta) item.getItemMeta();

            Object profile = Class.forName("com.mojang.authlib.GameProfile")
                    .getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), "mask");

            Object property = Class.forName("com.mojang.authlib.properties.Property")
                    .getConstructor(String.class, String.class)
                    .newInstance("textures", base64Texture);

            Object properties = profile.getClass().getMethod("getProperties").invoke(profile);
            properties.getClass().getMethod("put", Object.class, Object.class).invoke(properties, "textures", property);

            profileField.set(meta, profile);
            item.setItemMeta(meta);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    public static ItemStack createSkull(String base64Texture) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        return applyTexture(item, base64Texture);
    }
}
