package me.keano.azurite.modules.versions.type;

import lombok.SneakyThrows;
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
import net.minecraft.server.v1_7_R4.*;
import net.minecraft.util.com.google.common.io.ByteArrayDataOutput;
import net.minecraft.util.com.google.common.io.ByteStreams;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import net.minecraft.util.io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlock;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
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
public class Version1_7_R4 extends Module<VersionManager> implements Version {

    private static final Field ENTITY_ID = ReflectionUtils.accessField(PacketPlayOutEntityEquipment.class, "a");
    private static final Field SLOT = ReflectionUtils.accessField(PacketPlayOutEntityEquipment.class, "b");
    private static final Field CHANNEL = ReflectionUtils.accessField(NetworkManager.class, "m");
    private static final Field TARGET = ReflectionUtils.accessField(PacketPlayOutPlayerInfo.class, "player");
    private static final Field ACTION = ReflectionUtils.accessField(PacketPlayOutPlayerInfo.class, "action");
    private static final Field HANDLE = ReflectionUtils.accessField(CraftItemStack.class, "handle");

    public Version1_7_R4(VersionManager manager) {
        super(manager);
    }

    @Override
    public CommandMap getCommandMap() {
        try {

            CraftServer server = (CraftServer) Bukkit.getServer();
            Method method = server.getClass().getMethod("getCommandMap");
            return (CommandMap) method.invoke(server);

        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Set<Player> getTrackedPlayers(Player player) {
        WorldServer worldServer = ((CraftPlayer) player).getHandle().getWorld().getWorld().getHandle();
        EntityTrackerEntry tracker = (EntityTrackerEntry) worldServer.getTracker().trackedEntities.get(player.getEntityId());

        if (tracker != null) {
            Set<Player> players = new HashSet<>();

            for (Object trackedPlayer : new ArrayList<>(tracker.trackedPlayers)) {
                players.add(((EntityPlayer) trackedPlayer).getBukkitEntity());
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
        net.minecraft.server.v1_7_R4.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = null;
        if (!nmsStack.hasTag()) {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }
        if (tag == null) tag = nmsStack.getTag();
        if (!tag.hasKey("ench")) tag.set("ench", new NBTTagList());
        nmsStack.setTag(tag);
        return CraftItemStack.asCraftMirror(nmsStack);
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

        for (int i = 1; i < 5; i++) {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), i, null);
            entityPlayer.getWorld().getWorld().getHandle().getTracker().a(entityPlayer, packet);
        }
    }

    @Override
    public void showArmor(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        for (int i = 1; i < 5; i++) {
            net.minecraft.server.v1_7_R4.ItemStack armor = CraftItemStack.asNMSCopy(player.getInventory().getArmorContents()[i - 1]);
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), i, armor);
            entityPlayer.getWorld().getWorld().getHandle().getTracker().a(entityPlayer, packet);
        }
    }

    @Override
    @SneakyThrows
    public void handleNettyListener(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        ChannelPipeline pipeline = ((Channel) CHANNEL.get(entityPlayer.playerConnection.networkManager)).pipeline();
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
                    int slot = (int) SLOT.get(packet);

                    if (slot >= 1 && slot < 5) {
                        if (ghostClass != null && ghostClass.getInvisible().containsKey(id)) return;
                        if (ability.getInvisible().containsKey(id)) return;
                    }

                } else if (msg instanceof PacketPlayOutPlayerInfo) {
                    PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) msg;
                    Nametag nametag = getInstance().getNametagManager().getNametags().get(player.getUniqueId());
                    int action = (int) ACTION.get(packet);

                    if (action == 0 && nametag != null) {
                        UUID uuid = ((GameProfile) TARGET.get(packet)).getId();
                        Player targetPlayer = Bukkit.getPlayer(uuid);

                        if (targetPlayer != null) {
                            // Make scoreboard sort instantly
                            nametag.getPacket().create("tablist", "", "", "", false, NameVisibility.ALWAYS);
                            nametag.getPacket().addToTeam(targetPlayer.getName(), "tablist");

                            Tasks.executeLaterAsync(getManager(), 10, () -> {
                                if (isVer1_7(player)) {
                                    EntityPlayer targetEntity = ((CraftPlayer) targetPlayer).getHandle();
                                    entityPlayer.playerConnection.sendPacket(PacketPlayOutPlayerInfo.removePlayer(targetEntity));
                                }
                            });
                        }
                    }
                }

                super.write(ctx, msg, promise);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof PacketPlayInBlockDig) {
                    PacketPlayInBlockDig packet = (PacketPlayInBlockDig) msg;
                    Wall wall = getInstance().getWallManager().getWalls().get(player.getUniqueId());

                    if (wall != null && packet.g() <= 2) {
                        Location location = new Location(player.getWorld(), packet.c(), packet.d(), packet.e());
                        if (wall.getWalls().contains(location)) return;
                    }

                } else if (msg instanceof PacketPlayInBlockPlace) {
                    PacketPlayInBlockPlace packet = (PacketPlayInBlockPlace) msg;
                    Wall wall = getInstance().getWallManager().getWalls().get(player.getUniqueId());

                    if (wall != null) {
                        Location location = new Location(player.getWorld(), packet.c(), packet.d(), packet.e());
                        if (wall.getWalls().contains(location)) return;
                    }
                }

                super.channelRead(ctx, msg);
            }
        });
    }

    @Override
    public void sendActionBar(Player player, String string) {
        if (isVer1_7(player)) return;
        PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + string + "\"}"), (byte) 2);
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
            net.minecraft.server.v1_7_R4.ItemStack nmsCopy = CraftItemStack.asNMSCopy(hand);
            nmsCopy.damage(1, ((CraftPlayer) player).getHandle());
            setItemInHand(player, CraftItemStack.asBukkitCopy(nmsCopy));
        }
    }

    @Override
    public void clearArrows(Player player) {
        ((CraftPlayer) player).getHandle().p(0);
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player bukkitPlayer, org.bukkit.block.Block
            bukkitBlock, ItemStack item) {
        List<org.bukkit.inventory.ItemStack> drops = new LinkedList<>();

        EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        World world = entityPlayer.world;
        net.minecraft.server.v1_7_R4.Block block = world.getType(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        TileEntity tileEntity = world.getTileEntity(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        boolean silk = item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);

        //Checks if player cannot break the block or player in creative mode
        if (!entityPlayer.a(block) || entityPlayer.playerInteractManager.isCreative())
            return drops;

        int fortuneLevel = EnchantmentManager.getBonusBlockLootEnchantmentLevel(entityPlayer);

        if (block instanceof BlockCrops) {
            // Age
            int j = ((CraftBlock) bukkitBlock).getData();

            if (block instanceof BlockPotatoes) {
                // Potato drop
                if (world.random.nextFloat() < 1.0F) {
                    Item toDrop = Items.POTATO;
                    if (toDrop != null)
                        drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_7_R4.ItemStack(toDrop, 1, 0)));
                }

                // Seeds
                if (j >= 7) {
                    int k = 3 + fortuneLevel;

                    for (int l = 0; l < k; ++l) {
                        if (world.random.nextInt(15) <= j) {
                            drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_7_R4.ItemStack(Items.POTATO, 1, 0)));
                        }
                    }
                }

                // Posionus Potato drop
                if (j >= 7 && world.random.nextInt(50) == 0) {
                    drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_7_R4.ItemStack(Items.POTATO_POISON)));
                }

            } else if (block instanceof BlockCarrots) {
                // Carrot drop
                if (world.random.nextFloat() < 1.0F) {
                    Item toDrop = Items.CARROT;
                    if (toDrop != null)
                        drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_7_R4.ItemStack(toDrop, 1, 0)));
                }

                // Seeds
                if (j >= 7) {
                    int k = 3 + fortuneLevel;

                    for (int l = 0; l < k; ++l) {
                        if (world.random.nextInt(15) <= j) {
                            drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_7_R4.ItemStack(Items.CARROT, 1, 0)));
                        }
                    }
                }
            } else {
                // Wheat drop
                if (world.random.nextFloat() < 1.0F) {
                    Item toDrop = j == 7 ? Items.WHEAT : Items.SEEDS;
                    if (toDrop != null)
                        drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_7_R4.ItemStack(toDrop, 1, 0)));
                }

                // Seeds
                if (j >= 7) {
                    int k = 3 + fortuneLevel;

                    for (int l = 0; l < k; ++l) {
                        if (world.random.nextInt(15) <= j) {
                            drops.add(CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_7_R4.ItemStack(Items.SEEDS, 1, 0)));
                        }
                    }
                }
            }

            return drops;
        }

        if (block instanceof BlockCocoa) {
            int j = ((CraftBlock) bukkitBlock).getData();
            byte b0 = 1;

            if (j >= 2) {
                b0 = 3;
            }

            ItemStack cocoa = CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_7_R4.ItemStack(Items.INK_SACK, 1, 3));
            cocoa.setAmount(b0);
            drops.add(cocoa);
            return drops;
        }

        if (block instanceof BlockNetherWart) {
            int age = ((CraftBlock) bukkitBlock).getData();
            int j = 1;
            int i = fortuneLevel;

            if (age >= 3) {
                j = 2 + world.random.nextInt(3);
                if (i > 0) {
                    j += world.random.nextInt(i + 1);
                }
            }

            drops.add(new ItemStack(Material.NETHER_STALK, j));
            return drops;
        }

        if (tileEntity instanceof TileEntitySkull) {
            TileEntitySkull tileEntitySkull = (TileEntitySkull) tileEntity;
            net.minecraft.server.v1_7_R4.ItemStack itemStack = new net.minecraft.server.v1_7_R4.ItemStack(Items.SKULL, 1, tileEntitySkull.getSkullType());

            if (tileEntitySkull.getSkullType() == 3) {
                NBTTagCompound nbtTagCompound = itemStack.getTag();

                if (nbtTagCompound == null) {
                    nbtTagCompound = new NBTTagCompound();
                    itemStack.setTag(nbtTagCompound);
                }

                NBTTagCompound skullOwnerTag = new NBTTagCompound();
                GameProfileSerializer.serialize(skullOwnerTag, tileEntitySkull.getGameProfile());
                nbtTagCompound.set("SkullOwner", skullOwnerTag);
                itemStack.setTag(nbtTagCompound);
            }

            drops.add(CraftItemStack.asCraftMirror(itemStack));
            return drops;
        }

        //Checks if player has silk touch
        if ((block.d() && !block.isTileEntity()) && (silk || EnchantmentManager.hasSilkTouchEnchantment(entityPlayer))) {
            Item itemStack = Item.getItemOf(block);
            if (itemStack != null) {
                int data = itemStack.n() ? net.minecraft.server.v1_7_R4.Block.getId(block) : 0;
                drops.add(CraftItemStack.asCraftMirror(new net.minecraft.server.v1_7_R4.ItemStack(itemStack, 1, data)));
            }
        } else {
            int dropCount = block.getDropCount(fortuneLevel, world.random);
            int blockId = net.minecraft.server.v1_7_R4.Block.getId(block);
            Item itemStack = block.getDropType(blockId, world.random, fortuneLevel);
            if (itemStack != null)
                drops.add(CraftItemStack.asCraftMirror(new net.minecraft.server.v1_7_R4.ItemStack(itemStack, dropCount, block.getDropData(blockId))));
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

    public boolean isVer1_7(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion() <= 5;
    }
}