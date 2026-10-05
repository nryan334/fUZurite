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
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
// Created using reflection.
@SuppressWarnings({"unused"})
public class TablistPacketV1_17_R1 extends TablistPacket {

    private final Triple<Integer, Integer, EntityPlayer> fakePlayers;
    private final int maxColumns;

    // Cache them to see if we don't have to send a packet again.
    private String header;
    private String footer;

    public TablistPacketV1_17_R1(TablistManager manager, Player player) {
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

                if (fake.e != entry.getPing()) {
                    fake.e = entry.getPing();
                    sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.c, fake));
                }

                this.updateSkin(tablist, fake, newSkin, col, row);
                IChatBaseComponent component = new ChatComponentText(entry.getText());

                if (fake.listName == null || !fake.listName.equals(component)) {
                    fake.listName = component;
                    sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.d, fake));
                }
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
        sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.b, fake));
        sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, fake));
    }

    private void loadFakes() {
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer worldServer = server.getWorlds().iterator().next();

        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 4; col++) {
                GameProfile profile = new GameProfile(UUID.randomUUID(), getName(col, row));
                EntityPlayer fake = new EntityPlayer(server, worldServer, profile);
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
                sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, fake));
                sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.d, fake));
            }
        }
    }

    private void sendPacket(Packet<?> packet) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
        if (connection != null) connection.sendPacket(packet);
    }

    private void sendHeaderFooter() {
        String headerJoined = String.join("\n", getManager().getAdapter().getHeader(player));
        String footerJoined = String.join("\n", getManager().getAdapter().getFooter(player));

        // Refrain sending a packet again if same.
        if (footer.equals(headerJoined) && header.equals(headerJoined)) return;

        this.header = headerJoined;
        this.footer = footerJoined;

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(
                CraftChatMessage.fromJSONOrString(headerJoined, true),
                CraftChatMessage.fromJSONOrString(footerJoined, true)
        );

        sendPacket(packet);
    }
}