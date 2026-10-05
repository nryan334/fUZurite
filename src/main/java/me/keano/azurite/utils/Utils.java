package me.keano.azurite.utils;

import com.viaversion.viaversion.api.Via;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import protocolsupport.api.ProtocolSupportAPI;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Utils {

    public static final List<PotionEffectType> DEBUFFS = Arrays.asList(
            PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM,
            PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.SATURATION,
            PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    );

    private static final Pattern ALPHA_NUMERIC = Pattern.compile("[^a-zA-Z0-9]");
    private static final String NMS_VER = getNMSVer();
    private static final Method SET_COLLIDES = (isModernVer() ? ReflectionUtils.accessMethod(LivingEntity.class, "setCollidable", boolean.class) : null);

    /*
    Credits: https://www.spigotmc.org/threads/progress-bars-and-percentages.276020/
     */
    public static String getProgressBar(long current, long max, int totalBars, String symbol, String completedColor, String notCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        return repeat(completedColor + symbol, totalBars - progressBars) +
                repeat(notCompletedColor + symbol, progressBars);
    }

    public static String getTaskProgressBar(long current, long max, int totalBars, String symbol, String completedColor, String notCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        return repeat(notCompletedColor + symbol, progressBars) + repeat(completedColor + symbol, totalBars - progressBars);
    }

    public static ItemStack splitEnchantAdd(String string, ItemStack item) {
        if (string.isEmpty()) return item;
        String[] split = string.split(":");
        item.addUnsafeEnchantment(Enchantment.getByName(split[0]), Integer.parseInt(split[1]));
        return item;
    }

    public static <T> void iterate(Collection<T> list, Predicate<T> consumer) {
        list.removeIf(consumer);
    }

    public static String getWorldName(World world) {
        switch (world.getEnvironment()) {
            case NORMAL:
                return "Overworld";
            case NETHER:
                return "Nether";
            case THE_END:
                return "End";
            default:
                return "";
        }
    }

    public static String repeat(String str, int repeat) {
        if (str == null) {
            return null;
        } else if (repeat <= 0) {
            return "";
        } else {
            int inputLength = str.length();
            if (repeat != 1 && inputLength != 0) {
                if (inputLength == 1 && repeat <= 8192) {
                    return repeat(str.charAt(0), repeat);
                } else {
                    int outputLength = inputLength * repeat;
                    switch (inputLength) {
                        case 1:
                            return repeat(str.charAt(0), repeat);
                        case 2:
                            char ch0 = str.charAt(0);
                            char ch1 = str.charAt(1);
                            char[] output2 = new char[outputLength];

                            for (int i = repeat * 2 - 2; i >= 0; --i) {
                                output2[i] = ch0;
                                output2[i + 1] = ch1;
                                --i;
                            }

                            return new String(output2);
                        default:
                            StringBuilder buf = new StringBuilder(outputLength);

                            for (int i = 0; i < repeat; ++i) {
                                buf.append(str);
                            }

                            return buf.toString();
                    }
                }
            } else {
                return str;
            }
        }
    }

    private static String repeat(char ch, int repeat) {
        if (repeat <= 0) {
            return "";
        } else {
            char[] buf = new char[repeat];
            Arrays.fill(buf, ch);
            return new String(buf);
        }
    }

    public static String getCardinalDirection(Player player) {
        double rot = (player.getLocation().getYaw() - 90) % 360;
        if (rot < 0) rot += 360.0;
        return getDirection(rot);
    }

    public static String convertName(PotionEffectType potion) {
        switch (potion.getName()) {
            case "INCREASE_DAMAGE":
                return "Strength";

            case "DAMAGE_RESISTANCE":
                return "Resistance";

            case "SLOW":
                return "Slowness";

            case "FAST_DIGGING":
                return "Haste";

            case "SLOW_DIGGING":
                return "Mining Fatigue";

            case "CONFUSION":
                return "Nausea";
        }

        return capitalize(potion.getName().toLowerCase().replace("_", " "));
    }

    public static String capitalize(String name) {
        char[] array = name.toCharArray();
        array[0] = Character.toUpperCase(array[0]);

        for (int i = 1; i < array.length; i++) {
            if (Character.isWhitespace(array[i - 1])) {
                array[i] = Character.toUpperCase(array[i]);
            }
        }

        return new String(array);
    }

    public static String left(String str, int len) {
        if (str == null) {
            return null;
        } else if (len < 0) {
            return "";
        } else {
            return str.length() <= len ? str : str.substring(0, len);
        }
    }

    // https://www.spigotmc.org/threads/player-direction.175482/
    private static String getDirection(double rot) {
        if (0 <= rot && rot < 22.5) {
            return "W";
        } else if (22.5 <= rot && rot < 67.5) {
            return "NW";
        } else if (67.5 <= rot && rot < 112.5) {
            return "N";
        } else if (112.5 <= rot && rot < 157.5) {
            return "NE";
        } else if (157.5 <= rot && rot < 202.5) {
            return "E";
        } else if (202.5 <= rot && rot < 247.5) {
            return "SE";
        } else if (247.5 <= rot && rot < 292.5) {
            return "S";
        } else if (292.5 <= rot && rot < 337.5) {
            return "SW";
        } else if (337.5 <= rot && rot < 360.0) {
            return "W";
        } else {
            return null;
        }
    }

    public static String formatLocation(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    public static void setCollidesWithEntities(Player player, boolean collides) {
        if (Utils.isModernVer()) {
            try {

                SET_COLLIDES.invoke(player, collides);

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        } else player.spigot().setCollidesWithEntities(collides);
    }

    public static String getNMSVer() {
        try {

            String packageName = Bukkit.getServer().getClass().getPackage().getName(); // Craft Server
            return packageName.split("\\.")[3].replaceAll("v", ""); // we don't want the v

        } catch (Exception e) {
            return "Latest";
        }
    }

    public static boolean isModernVer() {
        return NMS_VER.equals("1_16_R3") || NMS_VER.equals("1_17_R1") || NMS_VER.equalsIgnoreCase("1_18_R2")
                || NMS_VER.equals("1_19_R3") || NMS_VER.equals("1_20_R1") || NMS_VER.equalsIgnoreCase("latest");
    }

    public static boolean isAzuriteSpigot() {
        try {
            Class.forName("dev.azurite.spigot.api.SpigotAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isDebuff(ThrownPotion potion) {
        for (PotionEffect effect : potion.getEffects()) {
            if (!DEBUFFS.contains(effect.getType())) continue;
            return true;
        }

        return false;
    }

    public static Block getActualHighestBlock(Block block) {
        block = block.getWorld().getHighestBlockAt(block.getLocation());

        while (block.getType() == Material.AIR && block.getY() > 0) {
            block = block.getRelative(BlockFace.DOWN);
        }

        return block;
    }

    public static boolean isVer1_7(int protocol) {
        return protocol <= 5;
    }

    public static int getProtocolVersion(Player player) {
        PluginManager pluginManager = Bukkit.getPluginManager();

        if (pluginManager.getPlugin("ViaVersion") != null) {
            try {

                return Via.getAPI().getPlayerVersion(player.getUniqueId());

            } catch (Exception e) {
                return us.myles.ViaVersion.api.Via.getAPI().getPlayerVersion(player.getUniqueId());
            }

        } else if (pluginManager.getPlugin("ProtocolSupport") != null) {
            return ProtocolSupportAPI.getProtocolVersion(player).getId();
        }

        return 100;
    }

    public static Double parseDouble(String number) {
        try {

            return Double.parseDouble(number);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseInt(String number) {
        try {

            return Integer.parseInt(number);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean notNumber(String number) {
        return parseInt(number) == null;
    }

    public static int getAmountItems(Manager manager, Player player, ItemStack item) {
        ItemStack[] contents = player.getInventory().getContents();
        int amount = 0;

        for (ItemStack value : contents) {
            if (value == null || value.getType() != item.getType()) continue;
            if (manager.getData(value) != manager.getData(item)) continue;

            amount += value.getAmount();
        }

        return amount;
    }

    public static Color translateChatColorToColor(ChatColor chatColor) {
        switch (chatColor) {
            case GREEN:
                return Color.GREEN;
            case RED:
                return Color.RED;
            case YELLOW:
            case GOLD:
                return Color.YELLOW;
            case BLUE:
                return Color.BLUE;
            case AQUA:
                return Color.CYAN;
            case BLACK:
                return Color.BLACK;
        }

        return Color.WHITE;
    }

    public static void takeItems(Manager manager, Player player, ItemStack item, int amount) {
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack value = contents[i];

            if (value == null || value.getType() != item.getType()) continue;
            if (manager.getData(value) != manager.getData(item)) continue;

            if (amount >= value.getAmount()) {
                amount -= value.getAmount();
                player.getInventory().setItem(i, null);
                player.updateInventory();

            } else {
                value.setAmount(value.getAmount() - amount);
                player.updateInventory();
                break;
            }
        }
    }

    public static boolean isNotAlphanumeric(String string) {
        return ALPHA_NUMERIC.matcher(string).find();
    }

    /*
    Credits iHCF: GenericUtils
     */
    public static <E> List<E> createList(Object object, Class<E> type) {
        List<E> output = new ArrayList<>();

        if (!(object instanceof List)) return output;

        for (Object value : (List<?>) object) {
            if (value == null) continue;
            E e = type.cast(value);
            output.add(e);
        }

        return output;
    }

    public static boolean verifyPlugin(String plugin, HCF instance) {
        PluginManager pm = instance.getServer().getPluginManager();
        return pm.getPlugin(plugin) != null;
    }

    public static Player getDamager(Entity damager) {
        if (damager instanceof Player) return (Player) damager;

        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;

            if (projectile.getShooter() instanceof Player)
                return (Player) projectile.getShooter();
        }

        return null;
    }

    public static Player getDamagerProjectileOnly(Entity damager) {
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;

            if (projectile.getShooter() instanceof Player)
                return (Player) projectile.getShooter();
        }

        return null;
    }

    public static void giveClaimingWand(Manager manager, Player player, ItemStack itemStack) {
        if (player.getInventory().contains(itemStack))
            player.getInventory().remove(itemStack);

        if (manager.getItemInHand(player) == null) {
            manager.setItemInHand(player, itemStack);

        } else {
            player.getInventory().addItem(itemStack);
        }
    }
}