package me.keano.azurite.modules.masks;

import me.keano.azurite.modules.framework.Module;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MaskEquipListener extends Module<MaskManager> {

    private static String CB_PACKAGE;
    private static String NMS_PACKAGE;

    private static Class<?> craftItemStackClass;
    private static Class<?> nmsItemStackClass;
    private static Class<?> packetClass;
    private static Class<?> craftPlayerClass;
    private static Class<?> entityPlayerClass;
    private static Class<?> playerConnectionClass;
    private static Class<?> worldServerClass;
    private static Class<?> entityTrackerClass;
    private static Class<?> trackerEntryClass;

    private static Method asNMSCopy;
    private static Method sendPacket;
    private static Method getHandle;
    private static Method getTracker;
    private static Method trackedEntities;
    private static Method getBukkitEntity;

    private static Constructor<?> packetConstructor;

    static {
        try {
            CB_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
            NMS_PACKAGE = CB_PACKAGE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

            craftItemStackClass = Class.forName(CB_PACKAGE + ".inventory.CraftItemStack");
            nmsItemStackClass = Class.forName(NMS_PACKAGE + ".ItemStack");
            packetClass = Class.forName(NMS_PACKAGE + ".PacketPlayOutEntityEquipment");
            craftPlayerClass = Class.forName(CB_PACKAGE + ".entity.CraftPlayer");
            entityPlayerClass = Class.forName(NMS_PACKAGE + ".EntityPlayer");
            playerConnectionClass = Class.forName(NMS_PACKAGE + ".PlayerConnection");
            worldServerClass = Class.forName(NMS_PACKAGE + ".WorldServer");
            entityTrackerClass = Class.forName(NMS_PACKAGE + ".EntityTracker");
            trackerEntryClass = Class.forName(NMS_PACKAGE + ".EntityTrackerEntry");

            asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            sendPacket = playerConnectionClass.getMethod("sendPacket", Class.forName(NMS_PACKAGE + ".Packet"));
            getHandle = craftPlayerClass.getMethod("getHandle");

            packetConstructor = packetClass.getConstructor(int.class, int.class, nmsItemStackClass);

            try {
                getTracker = worldServerClass.getMethod("getTracker");
                trackedEntities = entityTrackerClass.getMethod("getTrackedEntities");
            } catch (NoSuchMethodException ex) {
                // 1.7 compatibility
            }

            getBukkitEntity = entityPlayerClass.getMethod("getBukkitEntity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Map<UUID, String> activeVisualMasks = new HashMap<>();

    public MaskEquipListener(MaskManager manager) {
        super(manager);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
            checkAndUpdateVisual(player);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
            String savedMask = getManager().getEquippedMask(player.getUniqueId());
            ItemStack helmet = player.getInventory().getHelmet();

            if (savedMask != null && helmet != null && helmet.getType() != Material.AIR) {
                String helmetMaskId = getManager().getMaskIdFromItem(helmet);
                if (helmetMaskId != null && helmetMaskId.equalsIgnoreCase(savedMask)) {
                    sendMaskVisual(player, savedMask);
                }
            } else if (savedMask != null) {
                if (helmet == null || helmet.getType() == Material.AIR ||
                        getManager().getMaskIdFromItem(helmet) == null) {
                    getManager().clearEquippedMask(player.getUniqueId());
                }
            }

            checkAndUpdateVisual(player);
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        activeVisualMasks.remove(player.getUniqueId());
    }

    public void checkAndUpdateVisual(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();

        if (helmet == null || helmet.getType() == Material.AIR) {
            String activeMask = activeVisualMasks.get(player.getUniqueId());
            if (activeMask != null) {
                activeVisualMasks.remove(player.getUniqueId());
                getManager().clearEquippedMask(player.getUniqueId());
            }
            return;
        }

        String maskId = getManager().getMaskIdFromItem(helmet);
        if (maskId == null) {
            String activeMask = activeVisualMasks.get(player.getUniqueId());
            if (activeMask != null) {
                sendRealHelmetVisual(player);
                activeVisualMasks.remove(player.getUniqueId());
                getManager().clearEquippedMask(player.getUniqueId());
            }
            return;
        }

        String currentActive = activeVisualMasks.get(player.getUniqueId());
        if (currentActive == null || !currentActive.equalsIgnoreCase(maskId)) {
            sendMaskVisual(player, maskId);
        }
    }

    public void sendMaskVisual(Player player, String maskId) {
        MaskManager.Mask mask = getManager().getMask(maskId);
        if (mask == null) return;

        ItemStack fakeHelmet = SkullUtils.createSkull(mask.getTexture());

        sendEquipmentPacket(player, 4, fakeHelmet);
        activeVisualMasks.put(player.getUniqueId(), maskId);
        getManager().setEquippedMask(player.getUniqueId(), maskId);
    }

    public void sendRealHelmetVisual(Player player) {
        ItemStack realHelmet = player.getInventory().getHelmet();
        if (realHelmet == null || realHelmet.getType() == Material.AIR) {
            sendEquipmentPacket(player, 4, null);
        } else {
            sendEquipmentPacket(player, 4, realHelmet);
        }
    }

    private void sendEquipmentPacket(Player player, int slot, ItemStack item) {
        try {
            Object entityPlayer = getHandle.invoke(player);
            int entityId = player.getEntityId();

            Object nmsItem = null;
            if (item != null) {
                nmsItem = asNMSCopy.invoke(null, item);
            }

            Object packet = packetConstructor.newInstance(entityId, slot, nmsItem);

            Object connection = entityPlayerClass.getField("playerConnection").get(entityPlayer);
            sendPacket.invoke(connection, packet);

            for (Player nearby : Bukkit.getOnlinePlayers()) {
                if (nearby.equals(player)) continue;
                if (nearby.getWorld().equals(player.getWorld()) &&
                        nearby.getLocation().distanceSquared(player.getLocation()) < 4096) {
                    Object nearbyHandle = getHandle.invoke(nearby);
                    Object nearbyConn = entityPlayerClass.getField("playerConnection").get(nearbyHandle);
                    sendPacket.invoke(nearbyConn, packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
