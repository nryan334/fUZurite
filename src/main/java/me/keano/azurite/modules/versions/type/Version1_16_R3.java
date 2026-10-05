package me.keano.azurite.modules.versions.type;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.type.InvisibilityAbility;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.loggers.Logger;
import me.keano.azurite.modules.nametags.Nametag;
import me.keano.azurite.modules.nametags.extra.NameVisibility;
import me.keano.azurite.modules.pvpclass.type.ghost.GhostClass;
import me.keano.azurite.modules.tablist.extra.TablistSkin;
import me.keano.azurite.modules.versions.Version;
import me.keano.azurite.modules.versions.VersionManager;
import me.keano.azurite.modules.walls.Wall;
import me.keano.azurite.utils.ReflectionUtils;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("all")
public class Version1_16_R3 extends Module<VersionManager> implements Version {

    private static final Field ENTITY_ID = ReflectionUtils.accessField(PacketPlayOutEntityEquipment.class, "a");
    private static final Field SLOT = ReflectionUtils.accessField(PacketPlayOutEntityEquipment.class, "b");
    private static final Field ACTION = ReflectionUtils.accessField(PacketPlayOutPlayerInfo.class, "a");
    private static final Field DATA = ReflectionUtils.accessField(PacketPlayOutPlayerInfo.class, "b");

    private static final Method method = ReflectionUtils.accessMethod(
            CraftWorld.class, "spawnParticle", Particle.class, Location.class, int.class, Object.class
    );

    public Version1_16_R3(VersionManager manager) {
        super(manager);
    }

    @Override
    public CommandMap getCommandMap() {
        try {

            CraftServer server = (CraftServer) Bukkit.getServer();
            Method method = server.getClass().getMethod("getCommandMap");
            return (CommandMap) method.invoke(server);

        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Set<Player> getTrackedPlayers(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerChunkMap.EntityTracker tracker = entityPlayer.tracker;

        if (tracker != null) {
            Set<Player> players = new HashSet<>();

            for (EntityPlayer trackedPlayer : new ArrayList<>(tracker.trackedPlayers)) {
                players.add(trackedPlayer.getBukkitEntity());
            }

            return players;
        }

        return Collections.emptySet();
    }

    @Override
    public boolean isNotGapple(ItemStack item) {
        return !item.getType().name().contains("ENCHANTED_GOLDEN_APPLE");
    }

    @Override
    public int getPing(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        return craftPlayer.getHandle().ping;
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        try {

            Method method = player.getInventory().getClass().getMethod("getItemInMainHand");
            return (ItemStack) method.invoke(player.getInventory());

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ItemStack addGlow(ItemStack itemStack) {
        itemStack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public void setItemInHand(Player player, ItemStack item) {
        try {

            Method method = player.getInventory().getClass().getMethod("setItemInMainHand", ItemStack.class);
            method.invoke(player.getInventory(), item);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleLoggerDeath(Logger logger) {
        EntityPlayer player = ((CraftPlayer) logger.getPlayer()).getHandle();
        player.getBukkitEntity().getInventory().clear();
        player.getBukkitEntity().getInventory().setArmorContents(null);
        player.getBukkitEntity().setExp(0.0F);
        player.removeAllEffects();
        player.setHealth(0);
        player.getBukkitEntity().saveData(); // Save the inventory and everything we just modified
    }

    @Override
    public void playEffect(Location location, String name, Object data) {
        try {

            Particle particle = Particle.valueOf(name);
            method.invoke(location.getWorld(), particle, location, 1, data);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Particle " + name + " does not exist.");

        } catch (InvocationTargetException e) {
            e.printStackTrace();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTPSColored() {
        double tps = MinecraftServer.getServer().recentTps[0];
        String color = (tps > 18 ? "§a" : tps > 16 ? "§e" : "§c");
        String asterisk = (tps > 20 ? "*" : "");
        return color + asterisk + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
    }

    @Override
    public void hideArmor(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        if (entityPlayer.tracker != null) {
            List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> items = new ArrayList<>();

            items.add(new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(null)));
            items.add(new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(null)));
            items.add(new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(null)));
            items.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(null)));

            entityPlayer.tracker.broadcast(new PacketPlayOutEntityEquipment(player.getEntityId(), items));
        }
    }

    @Override
    public void showArmor(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        if (entityPlayer.tracker != null) {
            org.bukkit.inventory.PlayerInventory inventory = player.getInventory();
            List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> items = new ArrayList<>();

            items.add(new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(inventory.getBoots())));
            items.add(new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(inventory.getLeggings())));
            items.add(new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(inventory.getChestplate())));
            items.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(inventory.getHelmet())));

            entityPlayer.tracker.broadcast(new PacketPlayOutEntityEquipment(player.getEntityId(), items));
        }
    }

    @Override
    public void handleNettyListener(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        ChannelPipeline pipeline = entityPlayer.playerConnection.networkManager.channel.pipeline();
        AbilityManager abilityManager = getInstance().getAbilityManager();
        InvisibilityAbility ability = (InvisibilityAbility) abilityManager.getAbility("Invisibility");
        GhostClass ghostClass = getInstance().getClassManager().getGhostClass();

        if (pipeline.get("packet_handler") == null) {
            player.kickPlayer(Config.COULD_NOT_LOAD_DATA);
            return;
        }

        pipeline.addBefore("packet_handler", "Azurite", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof PacketPlayOutEntityEquipment) {
                    PacketPlayOutEntityEquipment packet = (PacketPlayOutEntityEquipment) msg;
                    int id = (int) ENTITY_ID.get(packet);

                    if ((ghostClass != null && ghostClass.getInvisible().containsKey(id)) || ability.getInvisible().containsKey(id)) {
                        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> slot =
                                (List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>>) SLOT.get(packet);

                        Iterator<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> iterator = slot.iterator();

                        while (iterator.hasNext()) {
                            Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack> pair = iterator.next();

                            if (pair.getFirst().getType() == EnumItemSlot.Function.ARMOR && pair.getSecond() != net.minecraft.server.v1_16_R3.ItemStack.b) {
                                iterator.remove();
                            }
                        }
                    }
                } else if (msg instanceof PacketPlayOutPlayerInfo) {
                    PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) msg;
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) ACTION.get(packet);
                    Nametag nametag = getInstance().getNametagManager().getNametags().get(player.getUniqueId());

                    if (action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER && nametag != null) {
                        List<PacketPlayOutPlayerInfo.PlayerInfoData> data = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) DATA.get(packet);

                        for (PacketPlayOutPlayerInfo.PlayerInfoData info : data) {
                            Player targetPlayer = Bukkit.getPlayer(info.a().getId());

                            if (targetPlayer != null) {
                                // Make tablist sort instantly
                                nametag.getPacket().create("tablist", "", "", "", false, NameVisibility.ALWAYS);
                                nametag.getPacket().addToTeam(targetPlayer.getName(), "tablist");
                            }
                        }
                    }
                }

                super.write(ctx, msg, promise);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof PacketPlayInBlockDig) {
                    PacketPlayInBlockDig packet = (PacketPlayInBlockDig) msg;
                    PacketPlayInBlockDig.EnumPlayerDigType type = packet.d();
                    Wall wall = getInstance().getWallManager().getWalls().get(player.getUniqueId());

                    if (wall != null && type.name().contains("_BLOCK")) {
                        BlockPosition position = packet.b();
                        Location location = new Location(player.getWorld(), position.getX(), position.getY(), position.getZ());
                        if (wall.getWalls().contains(location)) return;
                    }

                } else if (msg instanceof PacketPlayInUseItem) {
                    PacketPlayInUseItem packet = (PacketPlayInUseItem) msg;
                    Wall wall = getInstance().getWallManager().getWalls().get(player.getUniqueId());

                    if (wall != null) {
                        BlockPosition position = packet.c().getBlockPosition();
                        Location location = new Location(player.getWorld(), position.getX(), position.getY(), position.getZ());
                        if (wall.getWalls().contains(location)) return;
                    }
                }

                super.channelRead(ctx, msg);
            }
        });
    }

    @Override
    public void sendActionBar(Player player, String string) {
        PacketPlayOutChat packet = new PacketPlayOutChat(CraftChatMessage.fromJSONOrString(string, true), ChatMessageType.GAME_INFO, null);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void sendToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(getInstance(), "BungeeCord", out.toByteArray());
    }

    @Override
    public void damageItemDefault(Player player, ItemStack hand) {
        if (hand != null) {
            net.minecraft.server.v1_16_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(hand);
            nmsCopy.damage(1, ((CraftPlayer) player).getHandle(), (entityPlayer -> entityPlayer.broadcastItemBreak(EnumHand.MAIN_HAND)));
            setItemInHand(player, CraftItemStack.asBukkitCopy(nmsCopy));
        }
    }

    @Override
    public void clearArrows(Player player) {
        ((CraftPlayer) player).getHandle().setArrowCount(0);
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock, ItemStack item) {
        List<org.bukkit.inventory.ItemStack> drops = new LinkedList<>();

        EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        WorldServer worldServer = entityPlayer.playerInteractManager.world;
        IBlockData blockData = worldServer.getType(blockPosition);
        net.minecraft.server.v1_16_R3.ItemStack itemStack = entityPlayer.getItemInMainHand();
        itemStack = itemStack.isEmpty() ? net.minecraft.server.v1_16_R3.ItemStack.b : itemStack.cloneItemStack();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);

        net.minecraft.server.v1_16_R3.Block.getDrops(blockData, worldServer, blockPosition, tileEntity, entityPlayer, itemStack).forEach(nmsItem ->
                drops.add(CraftItemStack.asCraftMirror(nmsItem)));

        return drops;
    }

    @Override
    public TablistSkin getSkinData(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        GameProfile profile = entityPlayer.getProfile();

        if (profile.getProperties().get("textures").size() == 0) {
            return null;
        }

        Property property = profile.getProperties().get("textures").iterator().next();
        return new TablistSkin(property.getValue(), property.getSignature());
    }
}