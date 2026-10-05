package me.keano.azurite.utils;

import lombok.SneakyThrows;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.extra.Configs;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class ItemUtils {

    private static final Map<String, Material> MATERIALS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static Method SET_POTION_DATA = null;

    private final Configs configs;

    public ItemUtils(Configs configs) {
        this.configs = configs;
        this.load();
    }

    public void load() {
        for (Material material : Material.values()) {
            String path = (Utils.isModernVer() ? "BLOCKS_NEW." : "BLOCKS_LEGACY.") + material.name();

            if (!configs.getItemsConfig().contains(path)) {
                configs.getItemsConfig().set(path, material.name());
                MATERIALS.put(material.name(), material);
                continue;
            }

            String get = configs.getItemsConfig().getString(path);
            String[] names = (get.contains(";") ? get.split(";") : new String[]{get});

            for (String name : names) {
                MATERIALS.put(name, material);
            }
        }

        configs.getItemsConfig().save();
        configs.getItemsConfig().reloadCache(); // clear the cache, no use now.
    }

    @SneakyThrows
    public static ItemStack tryGetPotion(Manager manager, Material material, int id) {
        ItemStack itemStack = new ItemStack(material);

        if (Utils.isModernVer()) {
            if (SET_POTION_DATA == null) {
                SET_POTION_DATA = ReflectionUtils.accessMethod(PotionMeta.class, "setBasePotionData", PotionData.class);
            }

            boolean pot = material == getMat("SPLASH_POTION") ||
                    material == getMat("POTION") ||
                    material == getMat("LINGERING_POTION");

            if (pot) {
                PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

                switch (id) {
                    case 16421:
                    case 8229:
                        SET_POTION_DATA.invoke(potionMeta, new PotionData(PotionType.INSTANT_HEAL, false, true));
                        break;

                    case 8197:
                        SET_POTION_DATA.invoke(potionMeta, new PotionData(PotionType.INSTANT_HEAL, false, false));
                        break;

                    case 8198:
                        SET_POTION_DATA.invoke(potionMeta, new PotionData(PotionType.NIGHT_VISION, false, false));
                        break;

                    case 8270:
                        SET_POTION_DATA.invoke(potionMeta, new PotionData(PotionType.INVISIBILITY, true, false));
                        break;

                    case 8206:
                        SET_POTION_DATA.invoke(potionMeta, new PotionData(PotionType.INVISIBILITY, false, false));
                        break;
                }

                itemStack.setItemMeta(potionMeta);
            }
        } else {
            manager.setData(itemStack, id);
        }

        return itemStack;
    }

    public static String getItemName(ItemStack item) {
        if (item == null) {
            return "Hand";
        }

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }

        return Utils.capitalize(item.getType().name().toLowerCase().replace('_', ' '));
    }

    public static String getEntityName(Entity entity) {
        return Utils.capitalize(entity.getType().name().toLowerCase().replace('_', ' '));
    }

    public static void giveItem(Player player, ItemStack itemStack, Location drop) {
        if (itemStack.getType() == Material.AIR) return;

        if (player.getInventory().firstEmpty() == -1) {
            // This will try adding it into the stack in the players inventory
            for (ItemStack stack : player.getInventory().getContents()) {
                if (!itemStack.isSimilar(stack)) continue;
                if (stack.getAmount() >= stack.getMaxStackSize()) continue;

                int added = stack.getAmount() + itemStack.getAmount();

                if (added <= stack.getMaxStackSize()) {
                    stack.setAmount(added);
                    return;

                } else {
                    stack.setAmount(stack.getMaxStackSize());
                    itemStack.setAmount(added - stack.getMaxStackSize());
                }
            }

            player.getWorld().dropItemNaturally(drop, itemStack);

        } else {
            player.getInventory().addItem(itemStack);
        }
    }

    public static void setData(ItemStack item, int data) {
        if (Utils.isModernVer()) {
            if (item.getItemMeta() == null) return;

            Damageable damageable = (Damageable) item.getItemMeta();
            damageable.setDamage(data);
            item.setItemMeta((ItemMeta) damageable);

        } else {
            item.setDurability((short) data);
        }
    }

    public static ItemStack getMatItem(String string) {
        if (string.contains(":")) {
            String[] split = string.split(":");
            ItemStack item = new ItemStack(getMat(split[0]));
            setData(item, Integer.parseInt(split[1]));
            return item;
        }

        if (string.toLowerCase().startsWith("head_")) {
            ItemStack item = new ItemStack(Material.SKULL_ITEM);
            setData(item, 3);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(string.substring(string.indexOf("_") + 1));
            item.setItemMeta(meta);
            return item;
        }

        return new ItemStack(getMat(string));
    }

    public static Material getMat(String string) {
        Material material = MATERIALS.get(string);

        if (material == null) {
            throw new IllegalArgumentException("The material : " + string + " is incorrect!");
        }

        return material;
    }
}