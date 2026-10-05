package me.keano.azurite.modules.versions.type;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
public class Version1_8_R3 extends Module<VersionManager> implements Version {

    private static final Field ENTITY_ID = ReflectionUtils.accessField(PacketPlayOutEntityEquipment.class, "a");
    private static final Field SLOT = ReflectionUtils.accessField(PacketPlayOutEntityEquipment.class, "b");
    private static final Field DATA = ReflectionUtils.accessField(PacketPlayOutPlayerInfo.class, "b");
    private static final Field ACTION = ReflectionUtils.accessField(PacketPlayOutPlayerInfo.class, "a");
    private static final Field UUID = ReflectionUtils.accessField(PacketPlayOutNamedEntitySpawn.class, "b");
    private static final Field POSITION = ReflectionUtils.accessField(PacketPlayOutBlockChange.class, "a");

    public Version1_8_R3(VersionManager manager) {
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
        WorldServer worldServer = ((CraftPlayer) player).getHandle().getWorld().getWorld().getHandle();
        EntityTrackerEntry tracker = worldServer.getTracker().trackedEntities.get(player.getEntityId());

        if (tracker != null) {
            Set<Player> players = new HashSet<>();
            List<EntityPlayer> trackedPlayers = new ArrayList<>(tracker.trackedPlayers);

            for (EntityPlayer trackedPlayer : trackedPlayers) {
                players.add(trackedPlayer.getBukkitEntity());
            }

            return players;
        }

        return Collections.emptySet();
    }

    @Override
    public boolean isNotGapple(ItemStack item) {
        return item.getType() != Material.GOLDEN_APPLE || item.getDurability() != 1;
    }

    @Override
    public int getPing(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        return craftPlayer.getHandle().ping;
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getItemInHand();
    }

    @Override
    public ItemStack addGlow(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound compound = nmsStack.getTag();

        if (compound == null) {
            nmsStack.setTag(new NBTTagCompound());
        }

        if (!compound.hasKey("ench")) compound.set("ench", new NBTTagList());
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    @Override
    public void setItemInHand(Player player, ItemStack item) {
        player.setItemInHand(item);
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

            Effect effect = Effect.valueOf(name);
            location.getWorld().playEffect(location, effect, data);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Particle " + name + " does not exist.");
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

        for (int i = 1; i <= 4; i++) {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), i, null);
            entityPlayer.getWorld().getWorld().getHandle().getTracker().a(entityPlayer, packet);
        }
    }

    @Override
    public void showArmor(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        for (int i = 1; i <= 4; i++) {
            net.minecraft.server.v1_8_R3.ItemStack armor = CraftItemStack.asNMSCopy(player.getInventory().getArmorContents()[i - 1]);
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), i, armor);
            entityPlayer.getWorld().getWorld().getHandle().getTracker().a(entityPlayer, packet);
        }
    }

    @Override
    public void handleNettyListener(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        ChannelPipeline pipeline = entityPlayer.playerConnection.networkManager.channel.pipeline();
        AbilityManager abilityManager = getInstance().getAbilityManager();
        InvisibilityAbility ability = (InvisibilityAbility) abilityManager.getAbility("Invisibility");
        GhostClass ghostClass = getInstance().getClassManager().getGhostClass();

        if (pipeline == null) return;

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
                    int slot = (int) SLOT.get(packet);

                    if (slot >= 1 && slot < 5) {
                        if (ghostClass != null && ghostClass.getInvisible().containsKey(id)) return;
                        if (ability.getInvisible().containsKey(id)) return;
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

                                Tasks.executeLaterAsync(getManager(), 10, () -> {
                                    if (Utils.isVer1_7(nametag.getProtocolVersion())) {
                                        EntityPlayer targetEntity = ((CraftPlayer) targetPlayer).getHandle();
                                        entityPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                                                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, targetEntity)
                                        );
                                    }
                                });
                            }
                        }
                    }

                } else if (msg instanceof PacketPlayOutNamedEntitySpawn) {
                    PacketPlayOutNamedEntitySpawn packet = (PacketPlayOutNamedEntitySpawn) msg;
                    UUID uuid = (UUID) UUID.get(packet);
                    Player target = Bukkit.getPlayer(uuid);

                    if (target != null) {
                        EntityPlayer targetEntity = ((CraftPlayer) target).getHandle();
                        entityPlayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, targetEntity
                        ));
                    }
                }

                super.write(ctx, msg, promise);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof PacketPlayInBlockDig) {
                    PacketPlayInBlockDig packet = (PacketPlayInBlockDig) msg;
                    PacketPlayInBlockDig.EnumPlayerDigType type = packet.c();
                    Wall wall = getInstance().getWallManager().getWalls().get(player.getUniqueId());

                    if (wall != null && type.name().contains("_BLOCK")) {
                        BlockPosition position = packet.a();
                        Location location = new Location(player.getWorld(), position.getX(), position.getY(), position.getZ());
                        if (wall.getWalls().contains(location)) return;
                    }

                } else if (msg instanceof PacketPlayInBlockPlace) {
                    PacketPlayInBlockPlace packet = (PacketPlayInBlockPlace) msg;
                    Wall wall = getInstance().getWallManager().getWalls().get(player.getUniqueId());

                    if (wall != null) {
                        BlockPosition position = packet.a();
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
        Nametag nametag = getInstance().getNametagManager().getNametags().get(player.getUniqueId());

        if (nametag == null) return;
        if (Utils.isVer1_7(nametag.getProtocolVersion())) return;

        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(string), (byte) 2);
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
            net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(hand);
            nmsCopy.damage(1, ((CraftPlayer) player).getHandle());
            setItemInHand(player, CraftItemStack.asBukkitCopy(nmsCopy));
        }
    }

    @Override
    public void clearArrows(Player player) {
        ((CraftPlayer) player).getHandle().o(0);
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player bukkitPlayer, org.bukkit.block.Block block, ItemStack item) {
        List<org.bukkit.inventory.ItemStack> drops = new LinkedList<>();

        EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        World world = entityPlayer.world;
        IBlockData blockData = world.getType(blockPosition);
        Block nmsBlock = blockData.getBlock();

        int fortuneLevel = EnchantmentManager.getBonusBlockLootEnchantmentLevel(entityPlayer);
        boolean silk = item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);

        if (nmsBlock instanceof BlockCrops) {
            // Age
            int j = blockData.get(BlockCrops.AGE);

            if (nmsBlock instanceof BlockPotatoes) {
                // Potato drop
                if (world.random.nextFloat() < 1.0F) {
                    Item toDrop = Items.POTATO;
                    if (toDrop != null)
                        drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_8_R3.ItemStack(toDrop, 1, 0)));
                }

                // Seeds
                if (j >= 7) {
                    int k = 3 + fortuneLevel;

                    for (int l = 0; l < k; ++l) {
                        if (world.random.nextInt(15) <= j) {
                            drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_8_R3.ItemStack(Items.POTATO, 1, 0)));
                        }
                    }
                }

                // Posionus Potato drop
                if (blockData.get(BlockPotatoes.AGE) >= 7 && world.random.nextInt(50) == 0) {
                    drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_8_R3.ItemStack(Items.POISONOUS_POTATO)));
                }

            } else if (nmsBlock instanceof BlockCarrots) {
                // Carrot drop
                if (world.random.nextFloat() < 1.0F) {
                    Item toDrop = Items.CARROT;
                    if (toDrop != null)
                        drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_8_R3.ItemStack(toDrop, 1, 0)));
                }

                // Seeds
                if (j >= 7) {
                    int k = 3 + fortuneLevel;

                    for (int l = 0; l < k; ++l) {
                        if (world.random.nextInt(15) <= j) {
                            drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_8_R3.ItemStack(Items.CARROT, 1, 0)));
                        }
                    }
                }
            } else {
                // Wheat drop
                if (world.random.nextFloat() < 1.0F) {
                    Item toDrop = j == 7 ? Items.WHEAT : Items.WHEAT_SEEDS;
                    if (toDrop != null)
                        drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_8_R3.ItemStack(toDrop, 1, 0)));
                }

                // Seeds
                if (j >= 7) {
                    int k = 3 + fortuneLevel;

                    for (int l = 0; l < k; ++l) {
                        if (world.random.nextInt(15) <= j) {
                            drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_8_R3.ItemStack(Items.WHEAT_SEEDS, 1, 0)));
                        }
                    }
                }
            }

            return drops;
        }

        if (nmsBlock instanceof BlockCocoa) {
            int j = blockData.get(BlockCocoa.AGE);
            byte b0 = 1;

            if (j >= 2) {
                b0 = 3;
            }

            ItemStack cocoa = CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_8_R3.ItemStack(Items.DYE, 1, EnumColor.BROWN.getInvColorIndex()));
            cocoa.setAmount(b0);
            drops.add(cocoa);
            return drops;
        }

        if (nmsBlock instanceof BlockNetherWart) {
            int j = 1;
            int i = fortuneLevel;

            if (blockData.get(BlockNetherWart.AGE) >= 3) {
                j = 2 + world.random.nextInt(3);
                if (i > 0) {
                    j += world.random.nextInt(i + 1);
                }
            }

            drops.add(new ItemStack(Material.NETHER_STALK, j));
            return drops;
        }

        if (!entityPlayer.b(nmsBlock) || entityPlayer.playerInteractManager.isCreative())
            return drops;

        TileEntity tileEntity = world.getTileEntity(blockPosition);

        if (tileEntity instanceof TileEntitySkull) {
            TileEntitySkull tileEntitySkull = (TileEntitySkull) tileEntity;
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = new net.minecraft.server.v1_8_R3.ItemStack(Items.SKULL, 1, tileEntitySkull.getSkullType());

            if (tileEntitySkull.getSkullType() == 3) {
                NBTTagCompound nbtTagCompound = nmsStack.getTag();

                if (nbtTagCompound == null) {
                    nbtTagCompound = new NBTTagCompound();
                    nmsStack.setTag(nbtTagCompound);
                }

                NBTTagCompound skullOwnerTag = new NBTTagCompound();
                GameProfileSerializer.serialize(skullOwnerTag, tileEntitySkull.getGameProfile());
                nbtTagCompound.set("SkullOwner", skullOwnerTag);
                nmsStack.setTag(nbtTagCompound);
            }

            drops.add(CraftItemStack.asBukkitCopy(nmsStack));
            return drops;
        }

        if ((nmsBlock.d() && !nmsBlock.isTileEntity()) && (silk || EnchantmentManager.hasSilkTouchEnchantment(entityPlayer))) {
            Item itemstack = Item.getItemOf(nmsBlock);
            if (itemstack != null) {
                int data = itemstack.k() ? nmsBlock.toLegacyData(blockData) : 0;
                drops.add(CraftItemStack.asCraftMirror(new net.minecraft.server.v1_8_R3.ItemStack(itemstack, 1, data)));
            }
        } else {
            int dropCount = nmsBlock.getDropCount(fortuneLevel, world.random);
            Item itemstack = nmsBlock.getDropType(blockData, world.random, fortuneLevel);
            if (itemstack != null)
                drops.add(CraftItemStack.asCraftMirror(new net.minecraft.server.v1_8_R3.ItemStack(itemstack, dropCount, nmsBlock.getDropData(blockData))));
        }

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