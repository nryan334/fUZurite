package me.keano.azurite.modules.tablist.packet.type;

import me.keano.azurite.modules.tablist.Tablist;
import me.keano.azurite.modules.tablist.TablistManager;
import me.keano.azurite.modules.tablist.extra.TablistEntry;
import me.keano.azurite.modules.tablist.extra.TablistSkin;
import me.keano.azurite.modules.tablist.packet.TablistPacket;
import me.keano.azurite.utils.extra.Triple;
import net.minecraft.server.v1_7_R4.*;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;

import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("unused") // Created using reflection.
public class TablistPacketV1_7_R4 extends TablistPacket {

    private final Triple<Integer, Integer, EntityPlayer> fakePlayers;
    private final int maxColumns;

    // Cache them to see if we don't have to send a packet again.
    private String header;
    private String footer;

    public TablistPacketV1_7_R4(TablistManager manager, Player player) {
        super(manager, player);

        this.fakePlayers = new Triple<>();
        this.header = "";
        this.footer = "";
        this.maxColumns = ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion() <= 5 ? 3 : 4;

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
                    sendPacket(PacketPlayOutPlayerInfo.updatePing(fake));
                }

                if (maxColumns > 3) {
                    this.updateSkin(tablist, fake, newSkin, col, row);

                    // 1.8 list name can be used with a much higher char limit than teams
                    if (!fake.listName.equals(entry.getText())) {
                        fake.listName = entry.getText();
                        sendPacket(PacketPlayOutPlayerInfo.updateDisplayName(fake));
                    }

                    // 1.7 the list name won't work as it wasn't added to the client.
                } else handleTeams(fake.getBukkitEntity(), entry.getText(), calcSlot(col, row));
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
        sendPacket(PacketPlayOutPlayerInfo.removePlayer(fake));
        sendPacket(PacketPlayOutPlayerInfo.addPlayer(fake));
    }

    private void loadFakes() {
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer worldServer = server.getWorldServer(0);

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
                fake.listName = fake.getName(); // 1.7 the list name can cause kicks
                sendPacket(PacketPlayOutPlayerInfo.addPlayer(fake));

                if (maxColumns > 3) {
                    handleTeams(fake.getBukkitEntity(), fake.getName(), calcSlot(col, row));
                }
            }
        }
    }

    private void sendPacket(Packet packet) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        if (connection != null) connection.sendPacket(packet);
    }

    private void sendHeaderFooter() {
        if (maxColumns == 3) return; // 1.7 clients don't have a header/footer

        String headerJoined = String.join("\n", getManager().getAdapter().getHeader(player));
        String footerJoined = String.join("\n", getManager().getAdapter().getFooter(player));

        // Refrain sending a packet again if same.
        if (footer.equals(footerJoined) && header.equals(headerJoined)) return;

        this.header = headerJoined;
        this.footer = footerJoined;

        sendPacket(new ProtocolInjector.PacketTabHeader(new ChatComponentText(header), new ChatComponentText(footer)));
    }
}