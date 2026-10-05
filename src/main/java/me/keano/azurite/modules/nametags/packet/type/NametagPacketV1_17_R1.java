package me.keano.azurite.modules.nametags.packet.type;

import lombok.SneakyThrows;
import me.keano.azurite.modules.nametags.NametagManager;
import me.keano.azurite.modules.nametags.extra.NameInfo;
import me.keano.azurite.modules.nametags.extra.NameVisibility;
import me.keano.azurite.modules.nametags.packet.NametagPacket;
import me.keano.azurite.utils.ReflectionUtils;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("unused") // Created using reflection
public class NametagPacketV1_17_R1 extends NametagPacket {

    private static final Field a = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.b.class, "a");
    private static final Field b = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.b.class, "b");
    private static final Field c = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.b.class, "c");
    private static final Field d = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.b.class, "d");
    private static final Field f = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.b.class, "f");
    private static final Field g = ReflectionUtils.accessField(PacketPlayOutScoreboardTeam.b.class, "g");
    private static final Constructor<?> PACKET_SCORE_BOARD_CONSTRUCTOR = ReflectionUtils.accessConstructor(PacketPlayOutScoreboardTeam.class, 1);
    private static final ScoreboardTeam EMPTY_TEAM = new ScoreboardTeam(new Scoreboard(), "empty");

    private static final Map<String, EnumChatFormat> formats = Arrays
            .stream(EnumChatFormat.values())
            .collect(Collectors.toMap(EnumChatFormat::toString, e -> e));

    private final Map<String, NameInfo> teams;

    public NametagPacketV1_17_R1(NametagManager manager, Player player) {
        super(manager, player);
        this.teams = new ConcurrentHashMap<>();
    }

    private void sendPacket(Packet<?> packet) { // Packet type is unknown 1.8+
        PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
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
            PacketPlayOutScoreboardTeam.b subPacket = null;

            if (action == 0 || action == 2) {
                subPacket = new PacketPlayOutScoreboardTeam.b(EMPTY_TEAM);
                a.set(subPacket, CraftChatMessage.fromString(info.getName())[0]);
                b.set(subPacket, CraftChatMessage.fromString(info.getPrefix())[0]);
                c.set(subPacket, CraftChatMessage.fromString(info.getSuffix())[0]);
                d.set(subPacket, info.getVisibility().getName());
                f.set(subPacket, getFormat(info.getColor()));
                g.set(subPacket, info.isFriendlyInvis() ? 3 : 0);
            }

            return (PacketPlayOutScoreboardTeam) PACKET_SCORE_BOARD_CONSTRUCTOR.newInstance(
                    info.getName(), action,
                    (subPacket == null ? Optional.empty() : Optional.of(subPacket)),
                    (target != null ? Collections.singleton(target) : Collections.emptySet())
            );
        }

        public EnumChatFormat getFormat(String string) {
            EnumChatFormat format = formats.get(string);
            return format == null ? EnumChatFormat.v : format;
        }
    }
}