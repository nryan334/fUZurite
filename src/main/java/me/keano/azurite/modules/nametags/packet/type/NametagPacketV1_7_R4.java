package me.keano.azurite.modules.nametags.packet.type;

import lombok.Getter;
import lombok.SneakyThrows;
import me.keano.azurite.modules.nametags.NametagManager;
import me.keano.azurite.modules.nametags.extra.NameInfo;
import me.keano.azurite.modules.nametags.extra.NameVisibility;
import me.keano.azurite.modules.nametags.packet.NametagPacket;
import me.keano.azurite.utils.ReflectionUtils;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("unused")
public class NametagPacketV1_7_R4 extends NametagPacket {

    private static final Field a = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.class, "a");
    private static final Field b = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.class, "b");
    private static final Field c = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.class, "c");
    private static final Field d = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.class, "d");
    private static final Field e = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.class, "e");
    private static final Field f = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.class, "f");
    private static final Field g = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.class, "g");

    private final Map<String, NameInfo> teams;

    public NametagPacketV1_7_R4(NametagManager manager, Player player) {
        super(manager, player);
        this.teams = new ConcurrentHashMap<>();
    }

    private void sendPacket(Packet packet) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        if (connection != null) connection.sendPacket(packet);
    }

    @Override
    public void addToTeam(String player, String name) {
        NameInfo info = teams.get(name);

        if (info != null) {
            sendPacket(new ScoreboardPacket(info, 3, player).toPacket());
        }
    }

    @Override
    public void delete() {
        for (NameInfo info : teams.values()) {
            sendPacket(new ScoreboardPacket(info, 1).toPacket());
        }
        teams.clear();
    }

    @Override
    public void create(String name, String color, String prefix, String suffix, boolean friendlyInvis, NameVisibility visibility) {
        NameInfo current = teams.get(name);

        if (current != null) {
            if (!current.getColor().equals(color) || !current.getPrefix().equals(prefix) || !current.getSuffix().equals(suffix)) {
                NameInfo newInfo = new NameInfo(name, color, prefix, suffix, visibility, friendlyInvis);
                teams.put(name, newInfo);
                sendPacket(new ScoreboardPacket(newInfo, 2).toPacket());
            }
            return;
        }

        NameInfo info = new NameInfo(name, color, prefix, suffix, visibility, friendlyInvis);
        teams.put(name, info);
        sendPacket(new ScoreboardPacket(info, 0).toPacket());
    }

    @Getter
    private static class ScoreboardPacket {

        private final NameInfo info;
        private final String target;
        private final int action;

        public ScoreboardPacket(NameInfo info, int action) {
            this.info = info;
            this.action = action;
            this.target = null;
        }

        public ScoreboardPacket(NameInfo info, int action, String target) {
            this.info = info;
            this.action = action;
            this.target = target;
        }

        @SneakyThrows
        public PacketPlayOutScoreboardTeam toPacket() {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

            a.set(packet, info.getName());
            f.set(packet, action);

            if (action == 0 || action == 2) {
                b.set(packet, info.getName());
                c.set(packet, info.getPrefix() + info.getColor());
                d.set(packet, info.getSuffix());
                g.set(packet, info.isFriendlyInvis() ? 3 : 0);
            }

            if (action == 3 || action == 4) {
                if (target != null) e.set(packet, Collections.singletonList(target));
            }

            return packet;
        }
    }
}