package me.keano.azurite.modules.tablist.packet.type;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.keano.azurite.modules.tablist.Tablist;
import me.keano.azurite.modules.tablist.TablistManager;
import me.keano.azurite.modules.tablist.extra.TablistEntry;
import me.keano.azurite.modules.tablist.extra.TablistSkin;
import me.keano.azurite.modules.tablist.packet.TablistPacket;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Triple;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
// Created using reflection.
@SuppressWarnings({"unused", "deprecation"})
public class TablistPacketV1_16_R3 extends TablistPacket {

    private final Triple<Integer, Integer, EntityPlayer> fakePlayers;
    private final int maxColumns;

    // Cache them to see if we don't have to send a packet again.
    private String header;
    private String footer;

    public TablistPacketV1_16_R3(TablistManager manager, Player player) {
        super(manager, player);

        this.fakePlayers = new Triple<>();
        this.header = "";
        this.footer = "";
        this.maxColumns = (Utils.getProtocolVersion(player) <= 5 ? 3 : 4);

        this.loadFakes();
        this.init();
    }

    @Override
    public void update() {
        this.sendHeaderFooter();

        Tablist tablist = getManager().getAdapter().getInfo(player);

        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < maxColumns; col++) {
                TablistEntry entry = tablist.getEntry(col, row);
                TablistSkin newSkin = entry.getSkin();
                EntityPlayer fake = fakePlayers.get(col, row);

                if (fake.ping != entry.getPing()) {
                    fake.ping = entry.getPing();
                    sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, fake));
                }

                this.updateSkin(tablist, fake, newSkin, col, row);
                handleTeams(fake.getBukkitEntity(), entry.getText(), calcSlot(col, row));
            }
        }
    }

    private void updateSkin(Tablist tablist, EntityPlayer fake, TablistSkin skin, int col, int row) {
        GameProfile profile = fake.getProfile();
        TablistSkin old = tablist.getOldSkin(col, row);

        // Not found
        if (skin == null) return;
        // Verify if we should update
        if (old != null && old == skin) return;

        profile.getProperties().clear();
        profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fake));
        sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fake));
    }

    private void loadFakes() {
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer worldServer = server.getWorlds().iterator().next();

        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 4; col++) {
                GameProfile profile = new GameProfile(UUID.randomUUID(), getName(col, row));
                EntityPlayer fake = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));
                TablistSkin skin = getManager().getDefaultSkins().get(col, row);
                profile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
                fakePlayers.put(col, row, fake);
            }
        }
    }

    private void init() {
        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < maxColumns; col++) {
                EntityPlayer fake = fakePlayers.get(col, row);
                sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fake));
            }
        }
    }

    private void sendPacket(Packet<?> packet) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        if (connection != null) connection.sendPacket(packet);
    }

    private void sendHeaderFooter() {
        String headerJoined = String.join("\n", getManager().getAdapter().getHeader(player));
        String footerJoined = String.join("\n", getManager().getAdapter().getFooter(player));

        // Refrain sending a packet again if same.
        if (footer.equals(headerJoined) && header.equals(headerJoined)) return;

        this.header = headerJoined;
        this.footer = footerJoined;

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        packet.header = CraftChatMessage.fromJSONOrString(headerJoined, true);
        packet.footer = CraftChatMessage.fromJSONOrString(footerJoined, true);

        sendPacket(packet);
    }
}